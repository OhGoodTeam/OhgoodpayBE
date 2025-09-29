package com.ohgoodteam.ohgoodpay.recommend.service.flow;

import com.ohgoodteam.ohgoodpay.recommend.dto.CustomerContextWrapper;
import com.ohgoodteam.ohgoodpay.recommend.dto.FlowContext;
import com.ohgoodteam.ohgoodpay.recommend.dto.datadto.llmdto.BasicChatResponseDTO;
import com.ohgoodteam.ohgoodpay.recommend.dto.datadto.llmdto.ValidInputResponseDTO;
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
public class UnifiedFlowProcessor implements FlowProcessor{
    private final ValidCheckService validCheckService; // 입력 검증 서비스
    private final ChatCacheService chatCacheService; // 캐싱 서비스
    private final FlowConfiguration flowConfiguration; // next flow 정의
    private final LlmService llmService; //llm에 요청 보내기 위함

    // 검증을 위한 객체인 context에서 값을 뽑아서 request 생성 > response 받기
    // 이를 통해 코어 로직이 변했어도 api 명세를 바꾸지 않을 수 있다.
    @Override
    public BasicChatResponseDTO process(FlowContext context) {
        // 1. 현재 플로우에 대한 입력 검증
        ValidInputResponseDTO validated = validCheckService.validateInput(
                context.getCustomerId(),
                context.getSessionId(),
                context.getMessage(),
                context.getCurrentFlow().getValue());

        // 2. 검증 실패 처리
        // valid하지 않아서 해당 플로우를 스킵해야 할 경우, 정보를 저장하면 안 되기 때문이다.
        if (!validated.isValid()) {
            return handleValidationFailure(context, validated);
        }

        // 3. 검증 성공시 입력값 캐싱 (mood)
        // 여기는 validated에서 값을 뽑아야 하므로 validated를 같이 보낸다.
        saveFlowSpecificData(context.getSessionId(), context.getCurrentFlow(), validated);

        // 4. 데이터 캐싱
        CustomerContextWrapper contextWrapper = collectCacheData(context);

        // 5. 다음 플로우로 전환, 이 값은 LLM 요청에는 쓰이지 않음
        FlowConfig config = flowConfiguration.getFlowConfig(context.getCurrentFlow());
        ChatFlowType nextFlow = config.getNextFlow();
        chatCacheService.saveFlowBySession(context.getSessionId(), nextFlow.getValue()); //사용을 위해 다음 플로우를 저장
        chatCacheService.saveCntBySession(context.getSessionId(), 1); //카운트가 있었다면 초기화

        // 6. QUESTION vs RESPONSE에 따라서 LLM에 주입할 플로우 분기, 지금은 QUESTION만 존재.
        String flow = getTargetFlow(context);

        log.info("-------여기 플로우 전환 어케 되는지 봐야함!!!! 반드시 다음 플로우가 나와야 하는데.. : {}---------",flow);

        return llmService.generateChat(
                context.getSessionId(),
                contextWrapper.getCustomerInfo(),
                contextWrapper.getMood(),
                contextWrapper.getHobby(),
                contextWrapper.getBalance(),
                context.getMessage(),
                contextWrapper.getSummary(),
                flow
        );
        // TODO : LLM 요청 후에 상품을 캐싱 하므로, 상품 캐싱 로직은 이후에 작성한다.
    }

    @Override
    public boolean canHandle(FlowType flowType) {
        // QUESTION 플로우를 처리할 수 있다고 명시, 차후 확장을 위해 우선 canHandle 메서드를 살려둔다.
        return FlowType.QUESTION.equals(flowType);
    }

    /**
     * 검증 실패 처리를 위한 로직
     */
    private BasicChatResponseDTO handleValidationFailure(
            FlowContext context, ValidInputResponseDTO validated){
        int currentCount = chatCacheService.getCntBySession(context.getSessionId());

        // 2번 미만이면 재시도를 허용하게끔 한다.
        if(currentCount < 2){
            chatCacheService.saveCntBySession(context.getSessionId(), currentCount + 1);

            return BasicChatResponseDTO.builder()
                    .sessionId(context.getSessionId())
                    .message(validated.getMessage()) // "다시 말해주세요" 같은 에러 메시지
                    .newHobby("")
                    .products(null)
                    .flow(context.getCurrentFlow().getValue())
                    .build();
        } else {
            // 2번 이상 실패 시, 강제로 다음 플로우를 진행하기 위함이다.
            return forceNextFlow(context);
        }
    }

    /**
     * 유효성 count 센 다음에 다음 플로우로 강제 전환
     */
    private BasicChatResponseDTO forceNextFlow(FlowContext context){
        log.info("검증 실패 2번, 강제로 다음 플로우 진행 - sessionId: {}, flow: {}",
                context.getSessionId(), context.getCurrentFlow());

        // 1. 다음 플로우로 강제 전환
        FlowConfig config = flowConfiguration.getFlowConfig(context.getCurrentFlow());
        ChatFlowType nextFlow = config.getNextFlow(); //flowConfiguration에 설정 되어 있는 대로 다음 플로우로 간다.
        chatCacheService.saveFlowBySession(context.getSessionId(), config.getNextFlow().getValue());
        chatCacheService.saveCntBySession(context.getSessionId(), 1); //차후 수 세는 로직 바뀌면 여기 변경 예정

        // 2. 다음 플로우 질문 생성 (입력값을 무시하고 진행한다.)
        CustomerContextWrapper contextWrapper = collectCacheData(context);

        return llmService.generateChat(
                context.getSessionId(),
                contextWrapper.getCustomerInfo(),
                contextWrapper.getMood(),
                contextWrapper.getHobby(),
                contextWrapper.getBalance(),
                "", //message 기본 값
                contextWrapper.getSummary(),
                nextFlow.getValue()
        );
    }

    /**
     * 캐시된 데이터 수집
     *
     * 유효성 검증된 후 진행하므로 context에서 뺀다.
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

    /**
     * 플로우별 특정 데이터 저장, 입력이 valid일 경우 이 입력을 저장하고, 다음 플로우를 위한 응답 생성으로 넘어가야 하기 때문이다.
     */
    private void saveFlowSpecificData(String sessionId, ChatFlowType currentFlow, ValidInputResponseDTO validated) {
        // 특정 플로우에서 mood 저장, 유효한 경우엔 캐싱하고 아닌 경우엔 저장 없이 플로우만 넘기도록 한다.
        switch (currentFlow){ // 이 부분은 currentFlow 기준이다.
            case MOODCHECK : //현재는 moodcheck만 있으므로 여기서만 분기
                chatCacheService.saveMoodBySession(sessionId, validated.getInputMessage());
                break;
            default:
                log.warn("알 수 없는 플로우 타입 : {}", currentFlow);
        }
    }

    /**
     * FlowType.QUESTION vs FlowType.RESPONSE 둘 중 하나의 결과에 따라 getNextFlow()일지 getCurrentFlow()일지 선택
     */
    private String getTargetFlow(FlowContext context) {
        FlowConfig config = flowConfiguration.getFlowConfig(context.getCurrentFlow());
        return config.getProcessingType() == FlowType.QUESTION
                ? config.getNextFlow().getValue()     // 다음 플로우 질문
                : context.getCurrentFlow().getValue(); // 현재 플로우 응답
    }
}
