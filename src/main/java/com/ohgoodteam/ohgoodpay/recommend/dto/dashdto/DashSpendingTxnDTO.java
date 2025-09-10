package com.ohgoodteam.ohgoodpay.recommend.dto.dashdto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DashSpendingTxnDTO {
    private String id;
    private String ts;           // ISO8601 (UTC offset)
    private Double amount;
    private String currency;
    private String status;
    private String merchantName; // -> merchant_name 로 직렬화
    private String mcc;
    private String channel;
    private Boolean isBnpl;      // -> is_bnpl
    private Integer installments;
    private String memo;
    private String category;
}
