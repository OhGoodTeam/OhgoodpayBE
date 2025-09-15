package com.ohgoodteam.ohgoodpay.pay.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentModalDTO {
    private String requestName;
    private int price;
    private int point;
    private int balance;
    private Long requestId;
}
