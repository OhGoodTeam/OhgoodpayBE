package com.ohgoodteam.ohgoodpay.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ohgoodteam.ohgoodpay.common.entity.PaymentRequestEntity;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequestEntity, Long> {
    
    PaymentRequestEntity findByPaymentRequestId(Long paymentRequestId);
}
