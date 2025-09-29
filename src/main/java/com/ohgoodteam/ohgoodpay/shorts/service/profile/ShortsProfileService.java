package com.ohgoodteam.ohgoodpay.shorts.service.profile;

import com.ohgoodteam.ohgoodpay.shorts.dto.response.profile.ShortsProfileDataDTO;
import org.springframework.web.multipart.MultipartFile;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.profile.ShortsProfileEditResponseDTO;

public interface ShortsProfileService {

    /**
     * 특정 유저의 프로필 정보 조회
     * @param targetId
     * @param page
     * @param sortBy
     * @return
     */
    ShortsProfileDataDTO getProfile(Long customerId, Long targetId, int page, String sortBy);

    /**
     * 구독 요청
     * @param customerId
     * @param targetId
     * @return
     */
    long subscribe(Long customerId, Long targetId);


    /**
     * 프로필 편집
     * @param customerId
     * @param nickname
     * @param introduce
     * @param profileImg
     * @return
     */
    ShortsProfileEditResponseDTO editProfile(Long customerId, String nickname, String introduce, MultipartFile profileImg);
}
