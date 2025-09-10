package com.ohgoodteam.ohgoodpay.common.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MypageDTO {
    
    private Long customerId;
    private String name;
    private String emailId;
    private String account;
    private String accountName;
    private int point;
    private int gradePoint;
    private String gradeName;
    private int limitPrice;
    private float pointPercent;
}
