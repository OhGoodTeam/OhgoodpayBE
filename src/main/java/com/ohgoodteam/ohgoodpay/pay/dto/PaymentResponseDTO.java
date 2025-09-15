package com.ohgoodteam.ohgoodpay.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private Long paymentRequestId;
    private String qrCode;
    private String pinCode;
    private boolean success;
    private String qrImageUrl;
}
