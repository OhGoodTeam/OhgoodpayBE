package com.ohgoodteam.ohgoodpay.pay.dto;

import java.util.List;

import com.ohgoodteam.ohgoodpay.pay.dto.PaymentResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PayImmediatelyResponseDTO {
    
    private Long customerId;
    private String gradeName;
    private int limitPrice;
    private float pointPercent;
    private String account;
    private String accountName;
    private int balance;
    private boolean isExtension;
    private boolean isAuto;
    private List<List<PaymentResponseDTO>> unpaidBills;

}
