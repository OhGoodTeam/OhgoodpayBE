package com.ohgoodteam.ohgoodpay.recommend.controller;

import com.ohgoodteam.ohgoodpay.recommend.dto.ChatStartRequestDTO;
import com.ohgoodteam.ohgoodpay.recommend.dto.datadto.llmdto.BasicChatResponseDTO;
import com.ohgoodteam.ohgoodpay.recommend.service.ChatService;
import com.ohgoodteam.ohgoodpay.recommend.util.ApiResponseWrapper;
import com.ohgoodteam.ohgoodpay.security.util.JWTUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 추천 채팅 컨트롤러
 *
 */
@Tag(name = "Chat")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    private final JWTUtil jwtUtil; //jwtUtil 사용하는 방식으로 변경

    @PostMapping("")
    public ApiResponseWrapper<BasicChatResponseDTO> chat( @RequestBody ChatStartRequestDTO chatRequest, HttpServletRequest request) {
        try {
            Long customerId = Long.parseLong(jwtUtil.extractCustomerId(request));
            BasicChatResponseDTO response = chatService.chat(
                    customerId,
                    chatRequest.getSessionId(),
                    chatRequest.getInputMessage()
            );
            return ApiResponseWrapper.ok(response);
        } catch (IllegalArgumentException e) {
            return ApiResponseWrapper.error(400, e.getMessage());
        } catch (RuntimeException e) {
            // 로깅만 추가해서 추적 가능하도록 설정
            log.warn("Chat service runtime error: {}", e.getMessage());
            return ApiResponseWrapper.error(500, "일시적인 서비스 오류가 발생했습니다");
        } catch (Exception e) {
            log.error("Unexpected error in chat controller", e);
            return ApiResponseWrapper.error(500, "서버 내부 오류가 발생했습니다");
        }
    }
}
