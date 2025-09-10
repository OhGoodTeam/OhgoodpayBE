package com.ohgoodteam.ohgoodpay.pay.repository;

import com.ohgoodteam.ohgoodpay.common.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepositoryCustom {
    List<PaymentEntity> findRecent3Months(Long customerId,
                                          LocalDateTime fromAt,
                                          LocalDateTime toExcl,
                                          boolean onlyValidated);

    Page<PaymentEntity> findRecent3Months(Long customerId,
                                          LocalDateTime fromAt,
                                          LocalDateTime toExcl,
                                          boolean onlyValidated,
                                          Pageable pageable);
}