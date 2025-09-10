package com.ohgoodteam.ohgoodpay.recommend.service.fastapi;

import com.ohgoodteam.ohgoodpay.recommend.dto.DashSayMyNameResponse;
import com.ohgoodteam.ohgoodpay.recommend.dto.DashSpendingAnalyzeResponse;
import com.ohgoodteam.ohgoodpay.recommend.dto.dashdto.SpendingAnalyzeRequest;

import java.util.Map;

public interface DashAiClient {
    DashSayMyNameResponse sayMyName(Map<String, Object> payload);
    DashSpendingAnalyzeResponse analyzeSpending(
            com.ohgoodteam.ohgoodpay.recommend.dto.dashdto.SpendingAnalyzeRequest req
    );
}
