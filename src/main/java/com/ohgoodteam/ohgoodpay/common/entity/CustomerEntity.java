package com.ohgoodteam.ohgoodpay.common.entity;

import java.time.LocalDateTime;

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

@Entity
@Table(name = "customer")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String emailId;

    @Column(nullable = false)
    private String pwd;

    @Column(nullable = false)
    private String birth;

    @Column(nullable = false)
    private String account;

    @Column(nullable = false)
    private String accountName;

    private int point;

    private boolean isBlocked;

    private String profileImg;

    private String nickname;

    private String introduce;

    private int score;

    private String hobby;

    private boolean isExtension;

    private boolean isAuto;

    private int gradePoint;

    private int blockedCnt;

    private int extensionCnt;

    @Column(nullable = false)
    private LocalDateTime joinDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_name")
    @ToString.Exclude
    private GradeEntity grade;

    private int balance;
}
