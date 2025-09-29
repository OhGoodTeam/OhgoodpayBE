package com.ohgoodteam.ohgoodpay.shorts.service.profile;

import com.ohgoodteam.ohgoodpay.common.entity.CustomerEntity;
import com.ohgoodteam.ohgoodpay.common.entity.SubscriptionEntity;
import com.ohgoodteam.ohgoodpay.common.repository.CustomerRepository;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.profile.ShortsProfileDataDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.profile.ShortsProfileListDataDTO;
import com.ohgoodteam.ohgoodpay.shorts.enums.profile.SubscriptionStatus;
import com.ohgoodteam.ohgoodpay.shorts.repository.ShortsRepository;
import com.ohgoodteam.ohgoodpay.shorts.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;   
import org.springframework.transaction.annotation.Transactional;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.profile.ShortsProfileEditResponseDTO;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import java.util.ArrayList;
import java.util.List;

import com.ohgoodteam.ohgoodpay.shorts.repository.ShortsProfileRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortsProfileServiceImpl implements ShortsProfileService {

    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ShortsRepository shortsRepository;
    private final ShortsProfileRepository shortsProfileRepository;
    private final S3Client s3;


    /**
     * 특정 유저의 프로필 정보 조회
     * @param targetId
     * @param page
     * @param sortType
     * @return
     */
    @Override
    public ShortsProfileDataDTO getProfile(Long customerId, Long targetId, int page, String sortType) {
        // 1. 사용자 정보 조회
        CustomerEntity customer = customerRepository.findById(targetId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 사용자입니다."));
        // 2. 구독 상태 조회
        Long subscriberCount = subscriptionRepository.countFollwers(targetId);

        SubscriptionStatus subscriptionStatus;
        boolean isSubscribed = subscriptionRepository.existsByFollowerCustomerIdAndFollowingCustomerId(customerId, targetId); // TODO: 인증 로직 후 수정
        subscriptionStatus = isSubscribed ?  SubscriptionStatus.SUBSCRIBED : SubscriptionStatus.NOT_SUBSCRIBED;
        if(customerId.equals(targetId)){
            subscriptionStatus = SubscriptionStatus.SELF;
        }
        // 3. 쇼츠 영상 수
        Long videoCount = shortsRepository.countShorts(targetId);
        // 4. 쇼츠 영상 리스트 조회 (페이징)
        Sort sort = Sort.by(Sort.Direction.DESC, "date");

        switch (sortType){
            case "latest":
                sort = Sort.by(Sort.Direction.DESC, "date");
                break;
            case "like":
                sort = Sort.by(Sort.Direction.DESC, "likeCount");
                break;
            case "oldest":
                sort = Sort.by(Sort.Direction.ASC, "date");
                break;
        }

        Pageable pageable = PageRequest.of(page,10,sort);
        Page<Object[]> shortsPage = shortsRepository.findShortsProfileDataByCustomerId(targetId, pageable);
        // 5. DTO 조립 및 반환
        List<ShortsProfileListDataDTO> shortsList = new ArrayList<>();
        for (Object[] row : shortsPage.getContent()) {  // ✅ getContent() 추가
            ShortsProfileListDataDTO dto = new ShortsProfileListDataDTO(
                    (Long) row[0],        // ✅ shortsId
                    (Long) row[1],     // ✅ likeCount (Integer)
                    (String) row[2]       // ✅ thumbnail
            );
            shortsList.add(dto);
        }

        return ShortsProfileDataDTO.builder()
                .customerId(targetId)
                .customerNickname(customer.getNickname())
                .introduce(customer.getIntroduce())
                .profileImg(customer.getProfileImg())
                .subscriberCount(subscriberCount)
                .isSubscribed(subscriptionStatus)
                .videoCount(videoCount)
                .shortsList(shortsList)
                .hasNext(shortsPage.hasNext())
                .build();
    }

    /**
     * 구독 요청
     * @param customerId
     * @param targetId
     * @return
     */
    @Override
    public long subscribe(Long customerId, Long targetId) {
        // 0. 중복 구독 체크
        boolean alreadySubscribed = subscriptionRepository.existsByFollowerCustomerIdAndFollowingCustomerId(customerId, targetId);
        if(alreadySubscribed){
            throw new IllegalArgumentException("이미 구독한 사용자입니다.");
        }

        // 1. 구독자 정보 조회
        CustomerEntity follower = customerRepository.findById(customerId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 구독 대상 정보 조회
        CustomerEntity following = customerRepository.findById(targetId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 3. SubscriptionEntity 생성
        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .follower(follower)
                .following(following)
                .build();

        SubscriptionEntity subscriptionEntity = subscriptionRepository.save(subscription);

        return subscriptionEntity.getSubscriptionId();
    }

    @Value("${aws.s3.bucket}")
    private String bucket;

    /**
     * 프로필 수정, s3에 이미지 업로드
     * @param customerId
     * @param nickname
     * @param introduce
     * @param profileImg
     * @return
     */
    @Override
    @Transactional
    public ShortsProfileEditResponseDTO editProfile(Long customerId, String nickname, String introduce, MultipartFile profileImg) {
        try {
            if (profileImg != null && !profileImg.isEmpty()) {
                String profileImgKey = uploadProfileImageToS3(profileImg);
                // 새로운 이미지가 있을 때만 이미지 포함하여 업데이트
                int result = shortsProfileRepository.updateProfileWithImage(customerId, nickname, introduce, profileImgKey);
                if (result > 0) {
                    return new ShortsProfileEditResponseDTO("true", "프로필이 성공적으로 수정되었습니다.");
                } else {
                    return new ShortsProfileEditResponseDTO("false", "프로필 수정에 실패했습니다.");
                }
            } else {
                // 이미지가 없으면 닉네임과 소개만 업데이트
                int result = shortsProfileRepository.updateProfileWithoutImage(customerId, nickname, introduce);
                if (result > 0) {
                    return new ShortsProfileEditResponseDTO("true", "프로필이 성공적으로 수정되었습니다.");
                } else {
                    return new ShortsProfileEditResponseDTO("false", "프로필 수정에 실패했습니다.");
                }
            }
            
        } catch (Exception e) {
            log.error("프로필 수정 실패: customerId={}", customerId, e);
            return new ShortsProfileEditResponseDTO("false", "프로필 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 프로필 이미지 s3에 업로드 
    private String uploadProfileImageToS3(MultipartFile profileImg) throws IOException {
        // UUID + "_" + sample.jpg
        String originalFilename = profileImg.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        String profileImgKey = UUID.randomUUID() + "_" + 
            (originalFilename != null ? originalFilename : "profile" + extension);
        
        // S3 업로드 요청 
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(profileImgKey)
            .contentType(profileImg.getContentType() != null ? profileImg.getContentType() : "image/jpeg")
            .build();
        
        // S3에 업로드
        try (InputStream inputStream = profileImg.getInputStream()) {
            s3.putObject(putRequest, RequestBody.fromInputStream(inputStream, profileImg.getSize()));
        }
        
        log.info("프로필 이미지 S3 업로드 완료: {}", profileImgKey);
        return profileImgKey;
    }

}