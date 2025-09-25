package com.ohgoodteam.ohgoodpay.recommend.service;

import com.ohgoodteam.ohgoodpay.common.repository.CustomerRepository;
import com.ohgoodteam.ohgoodpay.recommend.dto.FlowContext;
import com.ohgoodteam.ohgoodpay.recommend.dto.datadto.llmdto.*;
import com.ohgoodteam.ohgoodpay.recommend.service.flow.FlowProcessor;
import com.ohgoodteam.ohgoodpay.recommend.service.flow.FlowProcessorFactory;
import com.ohgoodteam.ohgoodpay.recommend.util.flow.ChatFlowType;
import com.ohgoodteam.ohgoodpay.recommend.util.flow.FlowConfig;
import com.ohgoodteam.ohgoodpay.recommend.util.flow.FlowConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅 껍데기 서버 서비스 구현체
 *
 * 고객과의 실시간 채팅, LLM 응답 생성, 추천 서비스 연동을 담당
 * Redis 캐시를 통한 성능 최적화 및 외부 FastAPI 서버와의 통신 처리
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatCacheService chatCacheService;
    private final FlowProcessorFactory processorFactory;
    private final FlowConfiguration flowConfiguration;
    private final CustomerRepository customerRepository;

    /**
     * 채팅 처리
     */
    @Override
    @Transactional(readOnly = true)
    public BasicChatResponseDTO chat(Long customerId, String sessionId, String message) {
        log.info("세션 아이디와 고객 아이디 체크 sessionId: {} for customerId: {}", sessionId, customerId);

        // 1. 현재 플로우 조회
        String currentFlowStr = chatCacheService.getFlowBySession(sessionId);
        ChatFlowType currentFlow = ChatFlowType.fromValue(currentFlowStr);

        // 2. 플로우 설정 조회
        FlowConfig config = flowConfiguration.getFlowConfig(currentFlow);
        if (config == null) {
            throw new IllegalStateException("플로우 설정을 찾을 수 없습니다: " + currentFlow);
        }

        // 3. 적절한 프로세서 선택 - 팩토리 패턴
        FlowProcessor processor = processorFactory.getProcessor(config.getProcessingType());

        // 4. 플로우를 주입할 컨텍스트 생성
        FlowContext context = FlowContext.builder()
                .customerId(customerId)
                .sessionId(sessionId)
                .message(message)
                .currentFlow(currentFlow)
                .build();

        BasicChatResponseDTO response = processor.process(context);

        // 프로세스 후에 할 일을 하기
        processAfterResponse(response, customerId, sessionId);

        // 추천일 경우. 상품 가져오는 로직을 추가
        if("recommendation".equals(response.getFlow()) || "re-recommendation".equals(response.getFlow())){
            response = handleRecommendationFlow(response, sessionId);
        }

        return response;
    }

    /**
     * 응답 후처리 (DB 업데이트, 캐싱)
     */
    private void processAfterResponse(BasicChatResponseDTO response, Long customerId, String sessionId) {
        // 취미의 경우는 db에 저장하는 값으로, 차후 llm이 한 번 처리한 다음 저장할 수 있도록 리팩터링 하기 위해 요청 후 처리하도록 하였다.
        if (!response.getNewHobby().equals("")) {
            customerRepository.updateHobbyByCustomerId(customerId, response.getNewHobby());
            chatCacheService.saveHobby(customerId, response.getNewHobby()); //취미 저장
            log.info("취미 DB 업데이트 완료 - customerId: {}, hobby: {}", customerId, response.getNewHobby());
        }
        // 캐싱
        // 요약본 저장
        if (response.getSummary() != null && !response.getSummary().isEmpty()) {
            chatCacheService.saveSummaryBySession(sessionId, response.getSummary());
            // TODO : 요약본 DB에 저장하는 로직 필요 (우선순위 낮음)
        }
    }

    /**
     * 추천 플로우 처리 (상품 캐싱 및 응답 재생성)
     */
    private BasicChatResponseDTO handleRecommendationFlow(BasicChatResponseDTO response, String sessionId) {
//        if ("recommendation".equals(response.getFlow())) {
//            // 새 상품 캐싱
//            chatCacheService.saveProductsBySession(sessionId, response.getProducts());
//        }

        // 현재 버전은 캐싱된거 가져오는게 아닌, 새로 llm에 넣는거라 일단 조건 삭제..
        // 차후 확장을 위해 redis에 상품 주입하는건 남겨둠.
        chatCacheService.saveProductsBySession(sessionId, response.getProducts());

        // product url보기 위한 로그
        log.info("상품 이미지 url : {}", response.getProducts().get(0).getImage());

        return BasicChatResponseDTO.builder()
                .sessionId(response.getSessionId())
                .message(response.getMessage())
                .newHobby(response.getNewHobby())
                .products(chatCacheService.getProductsBySession(sessionId)) // 3개 제한
                .summary(response.getSummary())
                .flow(response.getFlow())
                .build();
    }
}
