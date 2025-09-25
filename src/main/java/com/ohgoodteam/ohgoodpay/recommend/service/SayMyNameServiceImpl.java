package com.ohgoodteam.ohgoodpay.recommend.service;

import com.ohgoodteam.ohgoodpay.common.repository.CustomerRepository;
import com.ohgoodteam.ohgoodpay.pay.repository.PaymentRepository;
import com.ohgoodteam.ohgoodpay.recommend.dto.DashSayMyNameResponseDTO;
import com.ohgoodteam.ohgoodpay.recommend.dto.dashdto.SayMyNameDTO;
import com.ohgoodteam.ohgoodpay.recommend.service.fastapi.DashAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SayMyNameServiceImpl implements SayMyNameService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final DashAiClient dashAiClient;

    @Override
    public DashSayMyNameResponseDTO execute(Long customerId) {
        // 기간 경계 (KST 기준, [from, to))
        var nowKst = ZonedDateTime.now(KST);
        var firstOfThisMonth = nowKst.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        var firstOfNextMonth = firstOfThisMonth.plusMonths(1);
        var firstOf12mAgo    = firstOfThisMonth.minusMonths(11);

        // 고객 플래그/점수 로드 (Projection)
        var flags = customerRepository.findFlagsById(customerId);
        if (flags == null) throw new IllegalArgumentException("No such customer: " + customerId);

        boolean isBlocked    = Boolean.TRUE.equals(flags.getIsBlocked());
        boolean isAuto       = Boolean.TRUE.equals(flags.getIsAuto());
        boolean isExtensionF = Boolean.TRUE.equals(flags.getIsExtension()); // 의미가 '이번달 연장'이 아니라면 참고용
        int     gradePoint   = flags.getGradePoint() == null ? 0 : flags.getGradePoint();
        int     autoExtCnt12m = parseIntSafe(flags.getExtensionCnt());      // 스키마 VARCHAR → int 변환

        // 결제 집계 (payment)
        long cnt12m = paymentRepository.countValidInRange(customerId, firstOf12mAgo, firstOfNextMonth);
        long amt12m = paymentRepository.sumAmountValidInRange(customerId, firstOf12mAgo, firstOfNextMonth); // 원 단위 long

        long curAmt = paymentRepository.sumAmountValidInRange(customerId, firstOfThisMonth, firstOfNextMonth);
        double currentCycleSpend = (curAmt == 0L) ? (amt12m / 12.0) : (double) curAmt;

        boolean extensionThisMonth =
                paymentRepository.existsExtensionThisMonth(customerId, firstOfThisMonth, firstOfNextMonth);
        boolean autoExtensionThisMonth =
                paymentRepository.existsAutoExtensionThisMonth(customerId, firstOfThisMonth, firstOfNextMonth)
                        || (isAuto && extensionThisMonth); // 정책: 자동연장 ON + 연장 발생 = 자동연장 간주
        var customer = customerRepository.findById(customerId).orElseThrow();
        String name = customer.getName();

        // 속껍질 In 구성 (FastAPI용 피처 포함)
        var in = SayMyNameDTO.In.builder()
                .customerId(customerId)
                .name(name)
                .extensionThisMonth(extensionThisMonth)
                .autoExtensionThisMonth(autoExtensionThisMonth)
                .autoExtensionCnt12m(autoExtCnt12m)
                .gradePoint(gradePoint)
                .isBlocked(isBlocked)
                .paymentCnt12m((int) cnt12m)
                .paymentAmount12m((double) amt12m)
                .currentCycleSpend(currentCycleSpend)
                .build();

        log.info("[SayMyName] features -> {}", in);

        // 5) FastAPI 호출 (세션 + 점수)
        var out = dashAiClient.sayMyName(in);
        log.info("[SayMyName] fastapi <- {}", out);
        return DashSayMyNameResponseDTO.builder()
                .message(out.getMessage())
                .sessionId(out.getSessionId())
                .ttlSeconds(out.getTtlSeconds())
                .ohgoodScore(out.getScore())
                .extensionThisMonth(in.isExtensionThisMonth())
                .autoExtensionThisMonth(in.isAutoExtensionThisMonth())
                .autoExtensionCnt12m(in.getAutoExtensionCnt12m())
                .gradePoint(in.getGradePoint())
                .blocked(in.isBlocked())
                .paymentCnt12m(in.getPaymentCnt12m())
                .paymentAmount12m(in.getPaymentAmount12m())
                .currentCycleSpend(in.getCurrentCycleSpend())
                .build();

    }

    private int parseIntSafe(String s) {
        try { return (s == null || s.isBlank()) ? 0 : Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}
