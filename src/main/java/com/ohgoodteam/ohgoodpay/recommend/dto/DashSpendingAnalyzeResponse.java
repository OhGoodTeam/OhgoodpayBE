package com.ohgoodteam.ohgoodpay.recommend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashSpendingAnalyzeResponse {

    private List<MonthBlock> months;
    private Double momGrowth;
    private List<Spike> spikes;
    private Map<String, List<TopCat>> topCategoriesByMonth;

    @JsonProperty("top_transactions_3m")             // 기본 표기
    @JsonAlias({"top_transactions3m"})               // 변형 표기도 허용
    private List<TopTxn> topTransactions3m;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MonthBlock {
        private String month;
        private Double totalSpend;
        private Map<String, Double> byCategory;
        private Map<String, Double> categoryShare;
        private List<TopCat> topCategories;
        private List<TopTxn> topTransactions;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopCat {
        private String category;
        private Double amount;
        private Double share;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopTxn {
        private String id;
        private String ts;
        private String merchantName; // @JsonNaming 으로 "merchant_name" ↔ 매핑됨
        private Double amount;
        private String category;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Spike {
        private String month;
        private Double zscore;
    }
}
