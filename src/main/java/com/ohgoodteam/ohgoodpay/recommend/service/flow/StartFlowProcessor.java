package com.ohgoodteam.ohgoodpay.recommend.service.flow;

import com.ohgoodteam.ohgoodpay.recommend.dto.CustomerContextWrapper;
import com.ohgoodteam.ohgoodpay.recommend.dto.FlowContext;
import com.ohgoodteam.ohgoodpay.recommend.dto.datadto.llmdto.BasicChatResponseDTO;
import com.ohgoodteam.ohgoodpay.recommend.service.ChatCacheService;
import com.ohgoodteam.ohgoodpay.recommend.service.ValidCheckService;
import com.ohgoodteam.ohgoodpay.recommend.service.fastapi.LlmService;
import com.ohgoodteam.ohgoodpay.recommend.util.flow.ChatFlowType;
import com.ohgoodteam.ohgoodpay.recommend.util.flow.FlowConfig;
import com.ohgoodteam.ohgoodpay.recommend.util.flow.FlowConfiguration;
import com.ohgoodteam.ohgoodpay.recommend.util.flow.FlowType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class StartFlowProcessor implements FlowProcessor{
    private final ChatCacheService chatCacheService; // 캐싱 서비스
    private final FlowConfiguration flowConfiguration; // next flow 정의
    private final LlmService llmService; //llm에 요청 보내기 위함

    @Override
    public BasicChatResponseDTO process(FlowContext context) {
        // 1. 다음 플로우로 전환
        FlowConfig config = flowConfiguration.getFlowConfig(context.getCurrentFlow());
        ChatFlowType nextFlow = config.getNextFlow();

        chatCacheService.saveFlowBySession(context.getSessionId(), nextFlow.getValue()); //사용을 위해 다음 플로우를 저장
        chatCacheService.saveCntBySession(context.getSessionId(), 1); //카운트가 있었다면 초기화

        // 2. 다음 플로우에 대한 질문 생성
        CustomerContextWrapper contextWrapper = collectCacheData(context);

        return llmService.generateChat(
                context.getSessionId(),
                contextWrapper.getCustomerInfo(),
                contextWrapper.getMood(),
                contextWrapper.getHobby(),
                contextWrapper.getBalance(),
                context.getMessage(),
                contextWrapper.getSummary(),
                nextFlow.getValue()
        );
    }

    @Override
    public boolean canHandle(FlowType flowType) {
        return FlowType.START.equals(flowType);
    }

    /**
     * 캐시된 데이터 수집
     *
     * 유효성 검증된 후 진행하므로 context에서 주입해준다.
     */
    private CustomerContextWrapper collectCacheData(FlowContext context){
        return CustomerContextWrapper.builder()
                .customerInfo(chatCacheService.getCustomerInfo(context.getCustomerId()))
                .hobby(chatCacheService.getHobby(context.getCustomerId()))
                .mood(chatCacheService.getMoodBySession(context.getSessionId()))
                .balance(chatCacheService.getBalance(context.getCustomerId()))
                .summary(chatCacheService.getSummaryBySession(context.getSessionId()))
                .build();
    }
}
