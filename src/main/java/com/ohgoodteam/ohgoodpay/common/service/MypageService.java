package com.ohgoodteam.ohgoodpay.common.service;

import com.ohgoodteam.ohgoodpay.common.dto.MypageDTO;

public interface MypageService {
    
    // 마이페이지 고객 정보 조회
    MypageDTO getMypageInfo(Long customerId);

}
