package com.ohgoodteam.ohgoodpay.recommend.dto.dashdto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

public class SayMyNameDTO {

    @Getter
    @ToString
    @Builder
    @Jacksonized
    public static class In {
        // ---------- 요청 DTO ----------
        @JsonAlias({"customer_id","customerId"})
        private final Long customerId;
        @JsonAlias({"username"})
        private final String name;
        private final String grade;                 // 선택
        private final boolean extensionThisMonth;
        private final boolean autoExtensionThisMonth;
        private final int     autoExtensionCnt12m;
        private final int     gradePoint;
        private final boolean isBlocked;
        private final int     paymentCnt12m;
        private final double  paymentAmount12m;
        private final double  currentCycleSpend;


        public static In ofBasic(
                Long customerId, String name,
                boolean extensionThisMonth, boolean autoExtensionThisMonth,
                int autoExtensionCnt12m, int gradePoint, boolean isBlocked,
                int paymentCnt12m, double paymentAmount12m, double currentCycleSpend
        ) {
            return builder()
                    .customerId(customerId)
                    .name(name)
                    .extensionThisMonth(extensionThisMonth)
                    .autoExtensionThisMonth(autoExtensionThisMonth)
                    .autoExtensionCnt12m(autoExtensionCnt12m)
                    .gradePoint(gradePoint)
                    .isBlocked(isBlocked)
                    .paymentCnt12m(paymentCnt12m)
                    .paymentAmount12m(paymentAmount12m)
                    .currentCycleSpend(currentCycleSpend)
                    .build();
        }

        /** grade 포함 프리셋 */
        public static In ofWithGrade(
                Long customerId, String name, String grade,
                boolean extensionThisMonth, boolean autoExtensionThisMonth,
                int autoExtensionCnt12m, int gradePoint, boolean isBlocked,
                int paymentCnt12m, double paymentAmount12m, double currentCycleSpend
        ) {
            return builder()
                    .customerId(customerId)
                    .name(name)
                    .grade(grade)
                    .extensionThisMonth(extensionThisMonth)
                    .autoExtensionThisMonth(autoExtensionThisMonth)
                    .autoExtensionCnt12m(autoExtensionCnt12m)
                    .gradePoint(gradePoint)
                    .isBlocked(isBlocked)
                    .paymentCnt12m(paymentCnt12m)
                    .paymentAmount12m(paymentAmount12m)
                    .currentCycleSpend(currentCycleSpend)
                    .build();
        }
    }

    // ---------- 응답 DTO ----------
    @Getter
    @ToString
    @Builder
    public static class Out {
        private final String  message;
        private final String  sessionId;    // FastAPI가 camelCase로 주면 그대로
        private final Integer ttlSeconds;
        private final Integer score;
        private final String  userId;
        private final String  gradeName;
        private final Integer gradeLimit;
        private final Double  pointPercent;

        public static Out ofCore(String message, String sessionId, Integer ttlSeconds, Integer score) {
            return builder()
                    .message(message)
                    .sessionId(sessionId)
                    .ttlSeconds(ttlSeconds)
                    .score(score)
                    .build();
        }
    }
}
