package com.ohgoodteam.ohgoodpay.recommend.dto;

import lombok.*;

/**
 * 채팅 요청 DTO
 * 
 * 프론트엔드에서 채팅 시 전달하는 정보 (사용자 메시지 포함)
 */
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatStartRequestDTO {
    private Long customerId;
    private String sessionId; // 프론트에서 관리하는 세션 ID
    private String inputMessage; // 사용자가 보낸 메시지
    private String flow; //플로우 관리를 위한 현재 플로우
}