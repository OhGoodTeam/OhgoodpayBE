package com.ohgoodteam.ohgoodpay.shorts.controller.feed;

import com.ohgoodteam.ohgoodpay.security.util.JWTUtil;
import com.ohgoodteam.ohgoodpay.shorts.dto.request.feed.ShortsCommentRequestDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.request.feed.ShortsPointEarnRequestDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.request.feed.ShortsReactionRequestDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.ApiResponseWrapper;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.feed.*;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.ShortsCommonResponse;
import com.ohgoodteam.ohgoodpay.shorts.service.feed.ShortsFeedService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShortsFeedController {

    private final ShortsFeedService shortsFeedService;
    private final JWTUtil jwtUtil;

    /**
     * 전체 쇼츠 피드 조회
     * @param page 현재 페이지
     * @param size 페이지당 개수
     * @param keyword 검색 키워드
     * @return
     */
    @GetMapping("/public/shorts/feeds")
    public ResponseEntity<ShortsCommonResponse> getAllFeed(
            HttpServletRequest request,
            @RequestParam (value = "page") int page,
            @RequestParam (value = "size", defaultValue = "10", required = false) int size,
            @RequestParam (value = "keyword", required = false) String keyword
    ) throws Exception {
        String customerId = "0";
        if (request.getHeader("Authorization") != null) {
            customerId = jwtUtil.extractCustomerId(request);
            log.info("전체 쇼츠 피드 조회 요청");
        }

        List<ShortsFeedDataDTO> data = shortsFeedService.findAllFeeds(page,size,keyword,Long.parseLong(customerId));
        System.out.println(data);
        ShortsCommonResponse res = ShortsCommonResponse.builder()
                .resultCode("0000")
                .resultMsg("성공")
                .data(data)
                .build();

        return ResponseEntity.ok(res);
    }

    /**
     * 쇼츠 댓글 조회
     * @param shortsId 쇼츠 아이디
     * @return
     */
    @GetMapping("/public/shorts/feeds/{shortsId}/comments")
    public ResponseEntity<ShortsCommonResponse> getAllComments(
            @PathVariable (value = "shortsId") Long shortsId,
            HttpServletRequest request
    ) throws Exception {
        String customerId = "0";
        if(request.getHeader("Authorization") != null) {
            customerId = jwtUtil.extractCustomerId(request);
        }
        log.info("특정 쇼츠 댓글 조회 요청");
        List<ShortsCommentDataDTO> data = shortsFeedService.findAllComments(shortsId, Long.parseLong(customerId));

        ShortsCommonResponse res = ShortsCommonResponse.builder()
                .resultCode("0000")
                .resultMsg("성공")
                .data(data)
                .build();

        return ResponseEntity.ok(res);
    }

    /**
     * 쇼츠 댓글 작성
     * @param requestDto 댓글 요청 DTO
     * @param shortsId 쇼츠 아이디
     * @return
     */
    @PostMapping("/shorts/feeds/{shortsId}/comments")
    public ResponseEntity<Boolean> createComment(
            @RequestBody ShortsCommentRequestDTO requestDto,
            @PathVariable (value = "shortsId") Long shortsId,
            HttpServletRequest request
    ) throws Exception {
        String customerId = jwtUtil.extractCustomerId(request);
        log.info("쇼츠 댓글 작성 요청: shortsId={}, requestDto={}", shortsId, requestDto);
        boolean data = shortsFeedService.createComment(shortsId, requestDto, Long.parseLong(customerId));

        return ResponseEntity.status(HttpStatus.OK).body(data);
    }

    /**
     * 포인트 적립 요청
     * @param requestDto
     * @return
     */
    @PostMapping("/shorts/point/earn")
    public ResponseEntity<ShortsPointEarnResponseDTO> earnPoint(
        HttpServletRequest request,
        @RequestBody ShortsPointEarnRequestDTO requestDto
    ) throws Exception {
        String customerId = jwtUtil.extractCustomerId(request);
        log.info("포인트 적립 요청 : requestDto={}", requestDto);
        ShortsPointEarnResponseDTO response = shortsFeedService.earnPoint(Long.parseLong(customerId), requestDto);
        return ResponseEntity.ok(response);
    }


    /**
     * 쇼츠 반응(좋아요/싫어요) 처리
     * @param shortsId
     * @param requestDto
     * @return
     */
    @PostMapping("/shorts/feeds/{shortsId}/reactions")
    public ApiResponseWrapper<ShortsReactionDataDTO> reactToShorts(
            @PathVariable (value ="shortsId") Long shortsId,
            @RequestBody ShortsReactionRequestDTO requestDto,
            HttpServletRequest request
    ) throws Exception {
        String customerId = jwtUtil.extractCustomerId(request);
        log.info("쇼츠 반응(좋아요/싫어요) 요청: shortsId={}, requestDto={}", shortsId, requestDto );
        try {
            ShortsReactionDataDTO dto = shortsFeedService.reactToShorts(requestDto, Long.parseLong(customerId));
            return ApiResponseWrapper.ok(dto);
        } catch (IllegalArgumentException e){
            // 이미 반응한 쇼츠
            return ApiResponseWrapper.error(400, e.getMessage());
        }
    }

    /**
     * 댓글 삭제
     * @param shortsId
     * @param commentId
     * @return
     */
    @DeleteMapping("/shorts/feeds/{shortsId}/comments/{commentId}/delete")
    public ApiResponseWrapper<ShortsCommentDeleteDataDTO> deleteComment(
            @PathVariable (value="shortsId") Long shortsId,
            @PathVariable (value ="commentId" ) Long commentId,
            HttpServletRequest request
    ) throws Exception {
        String customerId = jwtUtil.extractCustomerId(request);
        log.info("댓글 삭제 요청: shortsId={}, commentId={}", shortsId, commentId);
        try {
            ShortsCommentDeleteDataDTO dto = shortsFeedService.deleteComment(shortsId, commentId, Long.parseLong(customerId));
            return ApiResponseWrapper.ok(dto);
        } catch (IllegalArgumentException e){
            return ApiResponseWrapper.error(400, e.getMessage());
        }
    }

    /**
     * 특정 쇼츠 조회
     * @param shortsId
     * @return
     */
    @GetMapping("/public/shorts/{shortsId}")
    public ResponseEntity<ShortsFeedDataDTO> getSpecificShorts(
        @PathVariable (value = "shortsId") Long shortsId
    ){
        return ResponseEntity.ok(shortsFeedService.getSpecificShorts(shortsId));
    }
}
