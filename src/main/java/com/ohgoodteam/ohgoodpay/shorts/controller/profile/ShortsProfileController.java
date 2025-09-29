package com.ohgoodteam.ohgoodpay.shorts.controller.profile;
import com.ohgoodteam.ohgoodpay.security.util.JWTUtil;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.ApiResponseWrapper;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.profile.ShortsProfileDataDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.profile.ShortsProfileEditResponseDTO;
import com.ohgoodteam.ohgoodpay.shorts.service.profile.ShortsProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ShortsProfileController {
    private final ShortsProfileService shortsProfileService;
    private final JWTUtil jwtUtil;

    /**
     * 프로필 편집
     * @param nickname
     * @param introduce
     * @param profileImg
     * @return
     */
    @PostMapping("/shorts/profile/edit")
    public ResponseEntity<ShortsProfileEditResponseDTO> editProfile(
        HttpServletRequest request,
        @RequestParam("nickname") String nickname,
        @RequestParam("introduce") String introduce,
        @RequestPart(value = "profileImg", required = false) MultipartFile profileImg
    ) throws Exception {
        String customerId = jwtUtil.extractCustomerId(request);
        return ResponseEntity.ok(shortsProfileService.editProfile(Long.parseLong(customerId), nickname, introduce, profileImg));
    }


    /**
     * 특정 유저의 프로필 정보 조회
     * @param targetId
     * @param page
     * @param sortBy
     * @return
     */
    @GetMapping("/shorts/profile")
    public ApiResponseWrapper<ShortsProfileDataDTO> getProfile(
            @RequestParam (value = "targetId") Long targetId,
            @RequestParam (value = "page") int page,
            @RequestParam (value = "sortBy", defaultValue = "latest") String sortBy,
            HttpServletRequest request
    ) throws Exception {
        try {
            String customerId = jwtUtil.extractCustomerId(request);
            ShortsProfileDataDTO dto =  shortsProfileService.getProfile(Long.parseLong(customerId),targetId,page,sortBy);
            return ApiResponseWrapper.ok(dto);
        }
        catch (IllegalArgumentException e){
            return ApiResponseWrapper.error(400, e.getMessage());
        }
    }

    /**
     * 구독 요청
     */
    @PostMapping("/shorts/subscribe/{targetId}")
    public ApiResponseWrapper<String> subscribe(
        @PathVariable Long targetId,
        HttpServletRequest request
    ) throws Exception {
        String cutomerId = jwtUtil.extractCustomerId(request);
        // + 자기 자신 구독 방지
        if(targetId.equals(Long.parseLong(cutomerId))){
            return ApiResponseWrapper.error(400, "자기 자신은 구독할 수 없습니다.");
        }

        try {
            long result = shortsProfileService.subscribe(Long.parseLong(cutomerId), targetId);

            if(result == 0){
                return ApiResponseWrapper.error(400, "이미 구독한 사용자입니다.");
            }
            return ApiResponseWrapper.ok("구독 성공");
        } catch (IllegalArgumentException e){
            return ApiResponseWrapper.error(400, e.getMessage());
        }
    }
}
