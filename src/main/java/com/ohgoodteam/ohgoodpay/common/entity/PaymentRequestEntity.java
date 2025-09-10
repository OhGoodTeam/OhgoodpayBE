package com.ohgoodteam.ohgoodpay.common.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_request")
@Builder
@Getter
@Log4j2
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentRequestId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String qrcode;

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    private String requestName;

    @Column(nullable = false)
    private boolean isValidated;

    @Column(nullable = false)
    private LocalDateTime date;
}
