package com.ohgoodteam.ohgoodpay.recommend.util;

//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;

///**
// * API 에러 응답 공통 DTO
// *
// * TODO : 공용으로 사용하지 않을시 삭제 예정....
// */
//@Getter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@Schema(description = "에러 응답")
//public class ApiErrorResponse {
//
//    @Schema(description = "에러 메시지", example = "잘못된 요청입니다")
//    private String message;
//
//    @Schema(description = "에러 코드", example = "BAD_REQUEST")
//    private String code;
//
//    @Schema(description = "에러 발생 시간")
//    private LocalDateTime timestamp;
//
//    public static ApiErrorResponse of(String code, String message) {
//        return ApiErrorResponse.builder()
//                .code(code)
//                .message(message)
//                .timestamp(LocalDateTime.now())
//                .build();
//    }
//}