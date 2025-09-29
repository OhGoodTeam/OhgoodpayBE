package com.ohgoodteam.ohgoodpay.recommend.service;

import com.ohgoodteam.ohgoodpay.recommend.dto.datadto.llmdto.BasicChatResponseDTO;

/**
 * 채팅 껍데기 서버 서비스 인터페이스
 *
 * interface 분리로 추후 구현체 변경 용이
 */
public interface ChatService {
    // 채팅 생성
    BasicChatResponseDTO chat(Long customerId, String sessionId, String message, String flow);
}
