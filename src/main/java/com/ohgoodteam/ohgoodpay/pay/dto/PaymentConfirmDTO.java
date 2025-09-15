package com.ohgoodteam.ohgoodpay.pay.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmDTO {
    private boolean success;
    private boolean result;
}
