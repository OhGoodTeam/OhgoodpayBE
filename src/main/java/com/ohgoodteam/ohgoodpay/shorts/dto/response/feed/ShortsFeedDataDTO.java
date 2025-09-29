package com.ohgoodteam.ohgoodpay.shorts.dto.response.feed;


import com.ohgoodteam.ohgoodpay.common.entity.ShortsEntity;
import com.ohgoodteam.ohgoodpay.shorts.enums.profile.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortsFeedDataDTO {
    /**
     * 쇼츠 피드 정보 DTO
     */
    // 쇼츠 정보
    private Long shortsId;
    private String videoName;
    private String thumbnail;
    private String shortsName;
    private String shortsExplain;
    private LocalDateTime date;

    // 작성자 정보
    private Long customerId;
    private String customerNickname;
    private String profileImg;

    // 통계 정보 (예: 좋아요 수, 댓글 수)
    private int likeCount;
    private int commentCount;

    // 반응한 상태 (예 : "like", "dislike" )
    private String myReaction;

    // 구독 상태
    private SubscriptionStatus subscriptionStatus;

    public static ShortsFeedDataDTO entityToDto(ShortsEntity shortsEntity) {
        return  ShortsFeedDataDTO.builder()
                .shortsId(shortsEntity.getShortsId())
                .videoName(shortsEntity.getVideoName())
                .thumbnail(shortsEntity.getThumbnail())
                .shortsExplain(shortsEntity.getShortsExplain())
                .date(shortsEntity.getDate())
                .customerId(shortsEntity.getCustomer().getCustomerId())
                .customerNickname(shortsEntity.getCustomer().getNickname())
                .profileImg(shortsEntity.getCustomer().getProfileImg())
                .likeCount((int) shortsEntity.getLikeCount())
                .commentCount((int) shortsEntity.getCommentCount())
                .build();
    }

    public ShortsFeedDataDTO(ShortsEntity shortsEntity){
        this.shortsId = shortsEntity.getShortsId();
        this.videoName = shortsEntity.getVideoName();
        this.thumbnail = shortsEntity.getThumbnail();
        this.shortsName = shortsEntity.getShortsName();
        this.shortsExplain = shortsEntity.getShortsExplain();
        this.date = shortsEntity.getDate();
        this.customerId = shortsEntity.getCustomer().getCustomerId();
        this.customerNickname = shortsEntity.getCustomer().getNickname();
        this.profileImg = shortsEntity.getCustomer().getProfileImg();
        this.likeCount = (int) shortsEntity.getLikeCount();
        this.commentCount = (int) shortsEntity.getCommentCount();
        this.myReaction = null; // Entity에는 myReaction 필드가 없으므로 null로 설정
    }



}
