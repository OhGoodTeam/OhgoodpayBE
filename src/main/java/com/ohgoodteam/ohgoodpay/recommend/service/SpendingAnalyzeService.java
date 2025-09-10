package com.ohgoodteam.ohgoodpay.recommend.service;

import com.ohgoodteam.ohgoodpay.recommend.dto.DashSpendingAnalyzeRequest;
import com.ohgoodteam.ohgoodpay.recommend.dto.DashSpendingAnalyzeResponse;

public interface SpendingAnalyzeService {
    DashSpendingAnalyzeResponse execute(Long customerId);
    DashSpendingAnalyzeResponse execute(DashSpendingAnalyzeRequest req);

}