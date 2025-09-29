package com.ohgoodteam.ohgoodpay.shorts.service.feed;

import com.ohgoodteam.ohgoodpay.shorts.dto.request.feed.ShortsCommentRequestDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.request.feed.ShortsPointEarnRequestDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.request.feed.ShortsReactionRequestDTO;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.feed.*;
import java.time.LocalDateTime;
import java.util.List;

public interface ShortsFeedService {

    // 전체 쇼츠 피드 조회
    List<ShortsFeedDataDTO> findAllFeeds(int page, int size, String keyword, Long customerId);

    // 댓글 조회
    List<ShortsCommentDataDTO> findAllComments(Long shortsId, Long customerId);

    // 댓글 작성
    boolean createComment(Long shortsId, ShortsCommentRequestDTO shortsCommentRequestDto, Long customerId);

    // 좋아요 반응 처리
    ShortsReactionDataDTO reactToShorts(ShortsReactionRequestDTO shortsReactionRequestDto, Long customerId);

    // 공유기능 -> 특정 영상에 대한 정보 반환
    ShortsFeedDataDTO getSpecificShorts(Long shortsId);

    // 포인트 게이지 포인트 적립
    ShortsPointEarnResponseDTO earnPoint(Long customerId, ShortsPointEarnRequestDTO requestDto);

    // 댓글 삭제
    ShortsCommentDeleteDataDTO deleteComment(Long shortsId, Long commentId, Long customerId);
}
