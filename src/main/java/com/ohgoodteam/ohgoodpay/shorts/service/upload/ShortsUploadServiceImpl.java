package com.ohgoodteam.ohgoodpay.shorts.service.upload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ohgoodteam.ohgoodpay.common.entity.CustomerEntity;
import com.ohgoodteam.ohgoodpay.common.entity.ShortsEntity;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.upload.ShortsUploadResponseDTO;
import com.ohgoodteam.ohgoodpay.shorts.repository.upload.ShortsUploadRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortsUploadServiceImpl implements ShortsUploadService {
    private final S3Client s3;
    private final ShortsUploadRepository videoUploadRepository;
    private final EntityManager entityManager;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    // 변환 타임아웃
    private static final int FFMPEG_TIMEOUT_SEC = 1200;

    /**
     * 비디오 업로드
     * @param file
     * @param thumbnail
     * @param title
     * @param content
     * @return
     * @throws IOException
     */
    @Override
    @Transactional
    public ShortsUploadResponseDTO upload(MultipartFile file, MultipartFile thumbnail, String title, String content, Long customerId) throws IOException {
        log.info("쇼츠 업로드 서비스 시작: file={}, thumbnail={}, title={}, content={}, customerId={}",
            file.getOriginalFilename(), (thumbnail == null ? "null" : thumbnail.getOriginalFilename()), title, content, customerId);
        if (file.isEmpty() || thumbnail.isEmpty()) throw new IllegalArgumentException("파일을 다시 업로드해주세요.");
        if (file.getSize() > 200L * 1024 * 1024) throw new IllegalArgumentException("허용용량이 초과되었습니다.");

        // 임시 파일 생성해서 변환 작업 수행
        Path tmpVid = Files.createTempFile("upload-", ".mp4");
        Path tmpImg = Files.createTempFile("thumb-", ".jpg");

        String videoKey = null;
        String thumbKey = null;

        try {
            // 업로드 파일을 임시 경로로 복사
            file.transferTo(tmpVid.toFile());
            thumbnail.transferTo(tmpImg.toFile());
            log.info("임시 파일 생성: {}, {}", tmpVid, tmpImg);
            // 메타 분석
            String[] probe = runFfprobe(tmpVid);
            int width  = Integer.parseInt(probe[0]);
            int height = Integer.parseInt(probe[1]);
            double duration = Double.parseDouble(probe[2]);
            log.info("ffprobe 결과: {}x{}, {} sec", width, height, String.format("%.1f", duration));

            if (duration > 60.0) throw new IllegalArgumentException("영상 길이가 60초 이상이면 업로드할 수 없습니다.");

            // 1080p 이상이면 720p로 트랜스코딩
            if (Math.max(width, height) >= 1080) {
                Path tmp720 = Files.createTempFile("upload-720p-", ".mp4");
                try {
                    transcodeTo720p(tmpVid, tmp720);
                    Files.deleteIfExists(tmpVid); // 원본 임시파일 삭제
                    tmpVid = tmp720;              // 업로드 대상 링크를 tmp720으로 변경함
                } catch (Exception e) {
                    log.info("720p 변환 실패: {}", e.getMessage());
                    try { Files.deleteIfExists(tmp720); } catch (Exception ignore) {}
                    throw new IOException("720p 변환 실패: " + e.getMessage(), e);
                }
            }

            // 키 생성
            String vidName = (file.getOriginalFilename() == null ? "video.mp4" : file.getOriginalFilename());
            String thmName = (thumbnail.getOriginalFilename() == null ? "thumb.jpg" : thumbnail.getOriginalFilename());
            videoKey = UUID.randomUUID() + "_" + vidName;
            thumbKey = UUID.randomUUID() + "_" + thmName;

            // S3 업로드
            PutObjectRequest videoPut = PutObjectRequest.builder()
                .bucket(bucket).key(videoKey)
                .contentType(file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType())
                .build();

            try (InputStream vis = Files.newInputStream(tmpVid)) {
                log.info("S3 비디오 업로드 시작: s3://{}/{} ({}x{}, {} bytes, {} sec, {})",
                    bucket, videoKey, width, height, Files.size(tmpVid), String.format("%.1f", duration), region);
                s3.putObject(videoPut, RequestBody.fromInputStream(vis, Files.size(tmpVid)));
            }
            log.info("S3 비디오 업로드 성공: s3://{}/{} ({}x{}, {} bytes, {} sec, {})",
                bucket, videoKey, width, height, Files.size(tmpVid), String.format("%.1f", duration), region);
            PutObjectRequest thumbPut = PutObjectRequest.builder()
                .bucket(bucket).key(thumbKey)
                .contentType(thumbnail.getContentType() == null ? "image/jpeg" : thumbnail.getContentType())
                .build();
            log.info("S3 썸네일 업로드 시작: s3://{}/{} ({})", bucket, thumbKey, region);
            try (InputStream tis = Files.newInputStream(tmpImg)) {
                s3.putObject(thumbPut, RequestBody.fromInputStream(tis, Files.size(tmpImg)));
            }
            log.info("S3 썸네일 업로드 성공: s3://{}/{} ({} bytes, {})",
                bucket, thumbKey, Files.size(tmpImg), region);
            // DB 저장 
            CustomerEntity ref = entityManager.getReference(CustomerEntity.class, customerId);
            log.info("customer entity: {}", ref);

            ShortsEntity shortsEntity = ShortsEntity.builder()
                .videoName(videoKey)
                .thumbnail(thumbKey)
                .shortsName(title)
                .shortsExplain(content)
                .date(LocalDateTime.now())
                .likeCount(0)
                .commentCount(0)
                .customer(ref) 
                .build();
            videoUploadRepository.save(shortsEntity);

            return ShortsUploadResponseDTO.builder()
                .success(true)
                .message("업로드가 성공적으로 완료되었습니다")
                .build();

        } catch (SdkClientException e) {
            throw new IOException("S3 클라이언트 오류(자격증명/네트워크): " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("비디오 분석 중 중단되었습니다.", e);
        } catch (RuntimeException e) {
            // 기타 런타임 예외 래핑
            throw new IOException("업로드 실패: " + e.getMessage(), e);
        } finally {
            try { Files.deleteIfExists(tmpVid); } catch (Exception ignore) {}
            try { Files.deleteIfExists(tmpImg); } catch (Exception ignore) {}
        }
    }

    /**
     * 영상 해상도, 길이 추출
     * @param videoPath
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private String[] runFfprobe(Path videoPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
            "ffprobe", "-v", "error",
            "-select_streams", "v:0",
            "-show_entries", "stream=width,height:format=duration",
            "-of", "csv=p=0",
            videoPath.toAbsolutePath().toString()
        );
        log.info("ffprobe 실행: {}", String.join(" ", pb.command()));
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String out;
        try (InputStream is = p.getInputStream()) {
            out = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            log.info("ffprobe 출력 읽기 실패: {}", e.getMessage());
            p.destroyForcibly();
            throw e;
        }
        int code = p.waitFor();
        if (code != 0 || out.isBlank()) throw new IOException("ffprobe 실패 code=" + code + ", out=" + out);

        String[] lines = out.split("\\R+");
        String width, height, duration;
        if (lines.length >= 2) {
            String[] wh = lines[0].split(",");
            if (wh.length < 2) throw new IOException("해상도 파싱 실패: " + lines[0]);
            width = wh[0].trim(); height = wh[1].trim();
            duration = lines[1].trim();
        } else {
            String[] all = out.split(",");
            if (all.length < 3) throw new IOException("ffprobe 결과 형식 예외: " + out);
            width = all[0].trim(); height = all[1].trim(); duration = all[2].trim();
        }
        return new String[]{width, height, duration};
    }

    /**
     * 해상도 변환 로직
     * @param src
     * @param dst
     * @throws IOException
     * @throws InterruptedException
     */
    private void transcodeTo720p(Path src, Path dst) throws IOException, InterruptedException {
        // 가로,세로 자동 판단, 긴 변을 720으로 맞춤, 2의 배수
        String scaleExpr = "scale='if(gt(iw,ih),-2,720)':'if(gt(iw,ih),720,-2)'";

        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg", "-y", // 출력 파일 덮어쓰기
            "-i", src.toAbsolutePath().toString(), // 입력 파일 받기
            "-vf", scaleExpr, // scale 필터 적용
            "-c:v", "libx264", // 비디오 코덱
            "-preset", "medium", // 입출력 속도 균형
            "-crf", "23", // 화질, 용량 밸런스
            "-c:a", "aac", // 코덱
            "-b:a", "128k", // 비트레이트
            "-movflags", "+faststart",
            dst.toAbsolutePath().toString() // 출력 파일일
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        String log;
        try (InputStream is = p.getInputStream()) {
            log = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        // 변환 타임아웃 처리
        boolean finished = p.waitFor(FFMPEG_TIMEOUT_SEC, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new IOException("ffmpeg 변환 타임아웃");
        }
        int code = p.exitValue();
        if (code != 0) {
            throw new IOException("ffmpeg 실패(code=" + code + "): " + log);
        }
    }
}
