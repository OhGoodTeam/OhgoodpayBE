package com.ohgoodteam.ohgoodpay.shorts.controller.mypage;

import com.ohgoodteam.ohgoodpay.security.util.JWTUtil;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.mypage.ShortsMypageResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.mypage.ShortsMypageResponseDTO.ShelfPageResponse;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.mypage.ShortsMypageResponseDTO.UserCard;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.mypage.ShortsMypageResponseDTO.VideoCard;
import com.ohgoodteam.ohgoodpay.shorts.service.mypage.ShortsMypageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class ShortsMypageController {

    private final ShortsMypageService shortsMypageService;
    private final JWTUtil jwtUtil;

    /**
     * 마이페이지 미리보기
     * @param userId
     * @param limit
     * @return
     */
    @GetMapping("/shorts/mypage/overview")
    public ShortsMypageResponseDTO getOverview(HttpServletRequest request, @RequestParam(defaultValue="8") Integer limit) throws Exception {
        String customerId = jwtUtil.extractCustomerId(request);
        return shortsMypageService.getOverview(Long.parseLong(customerId), limit);
    }

    /**
     * 구독 전체보기
     * @param userId
     * @param cursor
     * @param limit
     * @return
     */
    @GetMapping("/shorts/mypage/subscribe")
    public ShelfPageResponse<UserCard> getSubscription(
        HttpServletRequest request,
        @RequestParam(required=false) String cursor,
        @RequestParam(defaultValue="8") Integer limit
    ) throws Exception {
        String customerId = jwtUtil.extractCustomerId(request);
        return shortsMypageService.getSubscriptions(Long.parseLong(customerId), cursor, limit);
    }

    /**
     * 좋아요 한 영상 전체보기
     * @param userId
     * @param cursor
     * @param limit
     * @return
     */
    @GetMapping("/shorts/mypage/all")
    public ShelfPageResponse<VideoCard> getLikedVideos(
        HttpServletRequest request,
        @RequestParam(required=false) String cursor,
        @RequestParam(defaultValue="8") Integer limit
    ) throws Exception {
        String customerId = jwtUtil.extractCustomerId(request);
        return shortsMypageService.getLikedVideos(Long.parseLong(customerId), cursor, limit);
    }

    /**
     * 댓글 단 영상 전체보기
     * @param userId
     * @param cursor
     * @param limit
     * @return
     */
    @GetMapping("/shorts/mypage/comments")
    public ShelfPageResponse<VideoCard> getCommentedVideos(
        HttpServletRequest request,
        @RequestParam(required=false) String cursor,
        @RequestParam(defaultValue="8") Integer limit
    ) throws Exception {
        String customerId = jwtUtil.extractCustomerId(request);
        return shortsMypageService.getCommentedVideos(Long.parseLong(customerId), cursor, limit);
    }

    /**
     * 구독 취소
     * @param userId
     * @param targetId
     * @return
     */
    @DeleteMapping("/shorts/mypage/subscription")
    public ResponseEntity<Void> deleteSubscription(HttpServletRequest request, @RequestParam Long targetId) throws Exception {
        String customerId = jwtUtil.extractCustomerId(request);
        long result = shortsMypageService.deleteSubscription(Long.parseLong(customerId), targetId);
        if(result > 0) {
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }
}
