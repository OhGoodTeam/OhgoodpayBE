package com.ohgoodteam.ohgoodpay.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 고객 캐시 정보 DTO
 *
 * Redis에 저장되는 고객 기본 정보를 담는 데이터 전송 객체
 * TTL : 12h
 */
@Getter
@NoArgsConstructor // Redis 역직렬화를 위한 기본 생성자
public class CustomerCacheDTO {
    private Long customerId;
    private String name;
    private int creditLimit; // 신용 한도

    @QueryProjection
    @JsonCreator // JSON 역직렬화를 위한 생성자 (redis를 위함)
    public CustomerCacheDTO(
            @JsonProperty("customerId") Long customerId, 
            @JsonProperty("name") String name, 
            @JsonProperty("creditLimit") int creditLimit) {
        this.customerId = customerId;
        this.name = name;
        this.creditLimit = creditLimit;
    }
}