package com.ohgoodteam.ohgoodpay.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;

@Entity
@Table(name = "communication")
@Builder
@Getter
@Log4j2
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long communicationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime date;
}
