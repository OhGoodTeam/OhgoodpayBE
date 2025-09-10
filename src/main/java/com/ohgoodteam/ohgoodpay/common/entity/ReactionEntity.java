package com.ohgoodteam.ohgoodpay.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.extern.log4j.Log4j2;

@Getter
@Entity
@Table(name = "reaction")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Log4j2
public class ReactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reactionId;

    @Column(nullable = false, length = 10)
    private String react;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false) 
    private CustomerEntity customer;    
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shorts_id", nullable = false) 
    private ShortsEntity shorts;   
}
