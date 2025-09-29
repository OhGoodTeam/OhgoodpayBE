package com.ohgoodteam.ohgoodpay.shorts.dto.request.feed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ShortsCommentRequestDTO {
    /**
     * 댓글 작성 요청 DTO
     */
    private String content; // 댓글 내용
    private Long gno; // 그룹 번호 (부모 :0, 자식 :부모 댓글 ID)
}
