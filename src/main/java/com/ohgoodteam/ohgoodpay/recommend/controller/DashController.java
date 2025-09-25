package com.ohgoodteam.ohgoodpay.recommend.controller;

import com.ohgoodteam.ohgoodpay.recommend.dto.*;
import com.ohgoodteam.ohgoodpay.recommend.dto.dashdto.AdviceDTO;
import com.ohgoodteam.ohgoodpay.recommend.service.DashAdviceService;
import com.ohgoodteam.ohgoodpay.recommend.service.PayThisMonthService;
import com.ohgoodteam.ohgoodpay.recommend.service.SayMyNameService;
import com.ohgoodteam.ohgoodpay.recommend.service.SpendingAnalyzeService;
import com.ohgoodteam.ohgoodpay.recommend.util.ApiResponseWrapper;
import com.ohgoodteam.ohgoodpay.security.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dash")
@RequiredArgsConstructor
@Validated
@Tag(name = "Dash")
public class DashController {

    private final JWTUtil jwtUtil;
    private final SayMyNameService sayMyNameService;
    private final SpendingAnalyzeService spendingAnalyzeService;
    private final DashAdviceService dashAdviceService;
    private final PayThisMonthService service;

    /** JwtUtil의 체크드 예외를 401로 변환 */
    private Long extractCustomerIdSafe(HttpServletRequest request) {
        try {
            String idStr = jwtUtil.extractCustomerId(request);
            return Long.parseLong(idStr);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token", e);
        }
    }

    /** body가 우선, 없으면 토큰에서 추출 */
    private Long resolveCustomerId(HttpServletRequest request, Long fromBody) {
        return (fromBody != null) ? fromBody : extractCustomerIdSafe(request);
    }

    @Operation(summary = "ohgoodscore 계산 및 한마디")
    @PostMapping("/saymyname")
    public ResponseEntity<ApiResponseWrapper<DashSayMyNameResponseDTO>> sayMyName(
            HttpServletRequest request,
            @RequestBody(required = false) @Valid DashSayMyNameRequestDTO req
    ) {
        Long customerId = resolveCustomerId(request, (req != null ? req.getCustomerId() : null));
        var out = sayMyNameService.execute(customerId);
        return ResponseEntity.ok(ApiResponseWrapper.ok(out));
    }

    @Operation(summary = "결제 내역 카테고리 분류")
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponseWrapper<SpendingAnalyzeResponseDTO>> analyze(
            HttpServletRequest request,
            @RequestBody(required = false) @Valid SpendingAnalyzeRequestDTO req
    ) {
        Long customerId = resolveCustomerId(request, (req != null ? req.getCustomerId() : null));
        Integer windowMonths = (req != null ? req.getWindowMonths() : null);

        // 기본값 3개월
        if (windowMonths == null) windowMonths = 3;

        // 불변 DTO → 새로 만들어 전달 (정적 팩토리 활용)
        SpendingAnalyzeRequestDTO finalReq =
                (req != null && req.getCustomerId() != null && req.getWindowMonths() != null)
                        ? req
                        : SpendingAnalyzeRequestDTO.of(customerId, windowMonths);

        var data = spendingAnalyzeService.analyze(finalReq);
        return ResponseEntity.ok(ApiResponseWrapper.ok(data));
    }

    @Operation(summary = "맞춤 AI 조언")
    @PostMapping("/advice")
    public ResponseEntity<ApiResponseWrapper<AdviceDTO.Out>> advice(
            HttpServletRequest request,
            @RequestBody(required = false) @Valid AdviceRequestDTO req
    ) {
        Long customerId = resolveCustomerId(request, (req != null ? req.getCustomerId() : null));
        var data = dashAdviceService.generate(customerId); // ✅ 4L 하드코딩 제거
        return ResponseEntity.ok(ApiResponseWrapper.ok(data));
    }
    // 토큰 기반
    @Operation(summary = "이번 달 결제 내역(me)")
    @GetMapping("/me/pay-this-month")
    public ResponseEntity<ApiResponseWrapper<Object>> getThisMonthForMe(HttpServletRequest request) {
        Long customerId = resolveCustomerId(request, null);
        var data = service.getThisMonth(customerId);
        return ResponseEntity.ok(ApiResponseWrapper.ok(data));
    }
    
    @Operation(summary = "이번 달 결제 내역")
    @GetMapping("/customer/{customerId}/pay-this-month")
    public ResponseEntity<ApiResponseWrapper<Object>> getThisMonth(@PathVariable Long customerId) {
        var data = service.getThisMonth(customerId);
        return ResponseEntity.ok(ApiResponseWrapper.ok(data));
    }

}