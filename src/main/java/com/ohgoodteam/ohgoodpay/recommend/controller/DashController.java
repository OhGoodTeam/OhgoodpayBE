package com.ohgoodteam.ohgoodpay.recommend.controller;


import com.ohgoodteam.ohgoodpay.recommend.dto.DashSayMyNameRequest;
import com.ohgoodteam.ohgoodpay.recommend.dto.DashSayMyNameResponse;
import com.ohgoodteam.ohgoodpay.recommend.dto.DashSpendingAnalyzeRequest;
import com.ohgoodteam.ohgoodpay.recommend.dto.DashSpendingAnalyzeResponse;
import com.ohgoodteam.ohgoodpay.recommend.service.SayMyNameService;
import com.ohgoodteam.ohgoodpay.recommend.service.SpendingAnalyzeService;
import com.ohgoodteam.ohgoodpay.recommend.util.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/dash")
@RequiredArgsConstructor
@Validated
public class DashController {

    private final SayMyNameService sayMyNameService;
    private final SpendingAnalyzeService spendingAnalyzeService;

    @Operation(summary = "사용자 정보 input", description = "사용자 기본 정보 받은 후 오굿스코어 계산 후 한줄평 반환")
    @PostMapping("/saymyname")
    public ApiResponseWrapper<DashSayMyNameResponse> sayMyName(
            @Parameter(description = "오굿스코어 한줄평 요청 (customerId만 필요)")
            @RequestBody @Valid DashSayMyNameRequest req
    ) {
        try {
            DashSayMyNameResponse resp = sayMyNameService.execute(req.getCustomerId());
            return ApiResponseWrapper.ok(resp);
        } catch (IllegalArgumentException e) {
            // 예: 존재하지 않는 고객 등 클라이언트 오류
            return ApiResponseWrapper.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            // 예: FastAPI 4xx/5xx, 외부 연동 오류(게이트웨이 성격) → 502로 매핑
            return ApiResponseWrapper.error(502, e.getMessage());
        } catch (Exception e) {
            // 그 외 서버 내부 오류
            return ApiResponseWrapper.error(500, "서버 내부 오류가 발생했습니다");
        }
    }

    @Operation(summary = "사용자 3개월 결제내역 분석",
            description = "요청 바디에 customerId만 보내면 서버가 DB에서 3개월 결제 내역을 조회→카테고리 분류/분석 후 결과를 반환합니다.")
    @PostMapping(value="/analyze", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseWrapper<DashSpendingAnalyzeResponse> analyze(
            @Valid @RequestBody DashSpendingAnalyzeRequest req
    ) {
        try {
            var resp = spendingAnalyzeService.execute(req.getCustomerId());
            return ApiResponseWrapper.ok(resp);
        } catch (IllegalArgumentException e) { return ApiResponseWrapper.error(400, e.getMessage()); }
        catch (IllegalStateException e)  { return ApiResponseWrapper.error(502, e.getMessage()); }
        catch (Exception e)              { return ApiResponseWrapper.error(500, "서버 내부 오류가 발생했습니다"); }
    }
}