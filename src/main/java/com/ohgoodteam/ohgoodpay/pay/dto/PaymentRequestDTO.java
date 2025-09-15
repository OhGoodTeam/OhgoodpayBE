package com.ohgoodteam.ohgoodpay.pay.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    private String orderId;
    private String requestName;
    private int totalPrice;

}
