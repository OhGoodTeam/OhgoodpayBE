package com.ohgoodteam.ohgoodpay.pay.dto;

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
public class GradeDTO {
    
    private String gradeName;
    private int limitPrice;
    private float pointPercent;
    private int upgrade;
}
