package com.ohgoodteam.ohgoodpay.common.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Builder
@Getter
@Log4j2
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    private CustomerEntity customer;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_request_id", nullable = false)
    @ToString.Exclude
    private PaymentRequestEntity paymentRequest;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    private int price;

    private int point;

    @Column(nullable = false)
    private String requestName;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private boolean isExpired;
}

