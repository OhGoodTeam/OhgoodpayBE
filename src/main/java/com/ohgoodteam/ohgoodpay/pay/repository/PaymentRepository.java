package com.ohgoodteam.ohgoodpay.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ohgoodteam.ohgoodpay.common.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long>, PaymentRepositoryCustom {
    
}
