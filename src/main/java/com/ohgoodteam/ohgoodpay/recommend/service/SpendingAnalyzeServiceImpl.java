package com.ohgoodteam.ohgoodpay.recommend.service;

import com.ohgoodteam.ohgoodpay.common.entity.PaymentEntity;
import com.ohgoodteam.ohgoodpay.pay.repository.PaymentRepository;
import com.ohgoodteam.ohgoodpay.recommend.dto.DashSpendingAnalyzeRequest;
import com.ohgoodteam.ohgoodpay.recommend.dto.DashSpendingAnalyzeResponse;
import com.ohgoodteam.ohgoodpay.recommend.service.fastapi.DashAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpendingAnalyzeServiceImpl implements SpendingAnalyzeService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final boolean ONLY_VALIDATED = true; // 디버깅시 false로 내려 테스트

    private final DashAiClient dashAiClient;
    private final PaymentRepository paymentRepository;

    @Override
    public DashSpendingAnalyzeResponse execute(DashSpendingAnalyzeRequest req) {
        return dashAiClient.analyzeSpending(req);
    }

    @Override
    public DashSpendingAnalyzeResponse execute(Long customerId) {
        // 1) 달 기준 3개월 경계 계산 (예: 9월이면 7/1 ~ 10/1 미만)
        LocalDate firstThisMonth = LocalDate.now(KST).withDayOfMonth(1);
        LocalDateTime fromAt = firstThisMonth.minusMonths(2).atStartOfDay(); // 포함
        LocalDateTime toExcl = firstThisMonth.plusMonths(1).atStartOfDay();  // 미만

        log.info("[PAY-QUERY] cid={}, fromAt(KST)={}, toExcl(KST)={}, onlyValidated={}",
                customerId, fromAt, toExcl, ONLY_VALIDATED);

        // 2) 결제 조회
        // 2) DB 조회
        var payments = paymentRepository.findRecent3Months(customerId, fromAt, toExcl, ONLY_VALIDATED);
        log.info("[PAY-RESULT] size={}", payments.size());
        if (payments.isEmpty()) {
            log.warn("[PAY-EMPTY] DB에서 최근 3개월 결제가 0건입니다. 필터/타임존/검증조건을 확인하세요.");
        } else {
            var p0 = payments.get(0);
            log.info("[PAY-SAMPLE] id={}, date={}, isExpired={}, validated={}",
                    p0.getPaymentId(), p0.getDate(), p0.isExpired(), p0.getPaymentRequest().isValidated());
        }
        // 3) PaymentEntity -> TxnIn 매핑
        var txs = payments.stream()
                .map(p -> DashSpendingAnalyzeRequest.TxnIn.builder()
                        .id("P-" + p.getPaymentId())
                        .ts(toUtcIso(p.getDate()))                 // KST DATETIME -> UTC ISO8601
                        .amount(resolveAmount(p))                   // totalPrice 우선
                        .currency("KRW")
                        .status("paid")                             // payment는 완료건으로 간주
                        .merchantName(nullToUnknown(p.getRequestName()))
                        .mcc(null)
                        .channel(null)
                        .memo(null)
                        .build())
                .toList();

        // 4) FastAPI 호출
        var req = DashSpendingAnalyzeRequest.builder()
                .transactions(txs)
                .useLlmFallback(false)
                .build();

        return dashAiClient.analyzeSpending(req);
    }

    private static String nullToUnknown(String s) {
        return (s == null || s.isBlank()) ? "UNKNOWN" : s;
    }

    private static double resolveAmount(PaymentEntity p) {
        int total = p.getTotalPrice();
        if (total > 0) return total;
        // 정책에 따라 포인트를 합산할지 결정 (필요시 아래 주석 해제)
        // return p.getPrice() + p.getPoint();
        return p.getPrice();
    }

    /** DB DATETIME(KST 가정) -> UTC ISO 문자열 */
    private static String toUtcIso(LocalDateTime kst) {
        return kst.atZone(KST)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toOffsetDateTime()
                .toString();
    }
}
