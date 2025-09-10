package com.ohgoodteam.ohgoodpay.pay.service;

import java.util.List;

import com.ohgoodteam.ohgoodpay.common.entity.CustomerEntity;
import com.ohgoodteam.ohgoodpay.common.entity.GradeEntity;
import com.ohgoodteam.ohgoodpay.common.entity.PaymentEntity;
import com.ohgoodteam.ohgoodpay.common.entity.PaymentRequestEntity;
import com.ohgoodteam.ohgoodpay.common.repository.CustomerRepository;
import com.ohgoodteam.ohgoodpay.pay.dto.PayImmediatelyResponseDTO;
import com.ohgoodteam.ohgoodpay.pay.dto.PaymentResponseDTO;
import com.ohgoodteam.ohgoodpay.pay.repository.PaymentRequestRepository;

import lombok.RequiredArgsConstructor;

public interface PayImmediatelyService {

    // 전체 결제건 조회
    List<PaymentResponseDTO> getAllPayment(Long customerId);

    // 미납부건 월별 분류
    PayImmediatelyResponseDTO classifyUnpaidBills(Long customerId);
    
    // 고객 수동 연장 신청
    boolean requestCustomerExtension(Long customerId);
    
    // 고객 자동 연장 신청
    boolean requestCustomerAutoExtension(Long customerId);

    //고객 등급 및 한도 조회
    GradeEntity findbyCustomerGradeName(Long customerId);

    // 결제건 납부 처리
    boolean payImmediately(Long customerId, Long[] lists);

    default PaymentEntity dtoToEntity(PaymentResponseDTO paymentResponseDTO) {

        CustomerEntity customer = CustomerEntity.builder().customerId(paymentResponseDTO.getCustomerId()).build();
        PaymentRequestEntity paymentRequest = PaymentRequestEntity.builder().paymentRequestId(paymentResponseDTO.getPaymentRequestId()).build();
        PaymentEntity paymentEntity = PaymentEntity.builder()
            .paymentId(paymentResponseDTO.getPaymentId())
            .customer(customer)
            .paymentRequest(paymentRequest)
            .totalPrice(paymentResponseDTO.getTotalPrice())
            .price(paymentResponseDTO.getPrice())
            .point(paymentResponseDTO.getPoint())
            .requestName(paymentResponseDTO.getRequestName())
            .date(paymentResponseDTO.getDate())
            .isExpired(paymentResponseDTO.isExpired())
            .build();
        return paymentEntity;
    }

    default PaymentResponseDTO entityToDto(PaymentEntity paymentEntity) {
        return PaymentResponseDTO.builder()
            .paymentId(paymentEntity.getPaymentId())
            .customerId(paymentEntity.getCustomer().getCustomerId())
            .paymentRequestId(paymentEntity.getPaymentRequest().getPaymentRequestId())
            .totalPrice(paymentEntity.getTotalPrice())
            .price(paymentEntity.getPrice())
            .point(paymentEntity.getPoint())
            .requestName(paymentEntity.getRequestName())
            .date(paymentEntity.getDate())
            .isExpired(paymentEntity.isExpired())
            .build();
    }
}
