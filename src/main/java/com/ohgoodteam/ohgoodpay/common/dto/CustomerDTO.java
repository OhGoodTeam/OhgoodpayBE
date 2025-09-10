package com.ohgoodteam.ohgoodpay.common.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {
    
    private Long customerId;
    private String name;
    private String emailId;
    private String birth;
    private String account;
    private String accountName;
    private int balance;
    private int point;
    private boolean isBlocked;
    private String profileImg;
    private String nickname;
    private String introduce;
    private int score;
    private String hobby;
    private int blockedCnt;
    private int extensionCnt;
    private LocalDateTime joinDate;
    private boolean isExtension;
    private boolean isAuto;
    private int gradePoint;
    private String gradeName;

}
