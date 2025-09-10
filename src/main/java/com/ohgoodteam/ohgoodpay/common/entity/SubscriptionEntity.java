package com.ohgoodteam.ohgoodpay.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Getter
@Entity
@Table(name = "subscription")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Log4j2
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_id", nullable = false) 
    private CustomerEntity following;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="follower_id", nullable = false)
    private CustomerEntity follower;
}
