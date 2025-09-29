package com.ohgoodteam.ohgoodpay.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * redis에 저장된 세션 기반 정보들을 제거하기 위해 받는 sessionId
 */
@Getter
@NoArgsConstructor
public class ClearSessionRequestDTO {
    private String sessionId;
}
