package com.ohgoodteam.ohgoodpay.shorts.controller.upload;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ohgoodteam.ohgoodpay.security.util.JWTUtil;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.upload.ShortsUploadResponseDTO;
import com.ohgoodteam.ohgoodpay.shorts.service.upload.ShortsUploadService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ShortsUploadController {
    private final ShortsUploadService s3VideoService;
    private final JWTUtil jwtUtil;

    /**
     * 쇼츠 업로드
     * @param video
     * @param thumbnail
     * @param title
     * @param content
     * @return
     */
    @PostMapping("/upload")
    public ResponseEntity<ShortsUploadResponseDTO> upload(
        HttpServletRequest request,
        @RequestPart("video") MultipartFile video, 
        @RequestPart(value="thumbnail", required = false) MultipartFile thumbnail,
        @RequestParam("title") String title,
        @RequestParam("content") String content
    ) throws Exception {
        try {
            log.info("쇼츠 업로드 요청 video name: {}, size: {}", video.getOriginalFilename(), video.getSize());
            String customerId = jwtUtil.extractCustomerId(request);
            ShortsUploadResponseDTO response = s3VideoService.upload(video, thumbnail, title, content, Long.parseLong(customerId));
            log.info("쇼츠 업로드 성공: {}", response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 공용 error dto 구현되면 그에 맞게
            log.info("쇼츠 업로드 실패: {}", e.getMessage());
            return ResponseEntity.<ShortsUploadResponseDTO>status(500)
                .body(ShortsUploadResponseDTO.builder()
                .success(false)
                .message("업로드 실패: " + e.getMessage())
                .build());
        }
    }
}
