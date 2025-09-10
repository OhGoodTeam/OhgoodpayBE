package com.ohgoodteam.ohgoodpay.pay.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    
    private Long paymentId;
    private Long customerId;
    private Long paymentRequestId;
    private int totalPrice;
    private int price;
    private int point;
    private String requestName;
    private LocalDateTime date;
    private boolean isExpired;
}
