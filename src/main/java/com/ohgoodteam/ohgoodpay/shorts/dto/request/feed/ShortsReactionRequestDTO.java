package com.ohgoodteam.ohgoodpay.shorts.dto.request.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortsReactionRequestDTO {
    private Long shortsId; // 반응 남긴 쇼츠 ID
    private String type; // 반응 타입 (like, dislike)
}
