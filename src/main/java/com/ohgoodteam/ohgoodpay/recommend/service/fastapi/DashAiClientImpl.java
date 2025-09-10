package com.ohgoodteam.ohgoodpay.recommend.service.fastapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgoodteam.ohgoodpay.config.FastApiConfig;
import com.ohgoodteam.ohgoodpay.recommend.dto.DashSayMyNameResponse;
import com.ohgoodteam.ohgoodpay.recommend.dto.DashSpendingAnalyzeResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;
import java.util.Map;

@Slf4j
@Component("dashAiClientImpl")
@RequiredArgsConstructor
public class DashAiClientImpl implements DashAiClient {

    private static final int DEFAULT_TTL_SECONDS = 86400;

    @Qualifier("fastapiWebClient")
    private final WebClient webClient;
    private final FastApiConfig cfg;
    private final ObjectMapper om;

    @Override
    public DashSayMyNameResponse sayMyName(Map<String, Object> payload) {
        final String path = resolvedPath();
        final int readTimeoutMs = Optional.ofNullable(cfg.getHttp())
                .map(FastApiConfig.Http::getReadTimeout).orElse(5000);

        log.info("[AI->] POST {} payloadKeys={}", path, payload.keySet());

        // 1) 상태코드 + 문자열 바디를 먼저 확보 (예외 던지지 않음)
        HttpResult result = webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Internal-Token", cfg.getInternalToken())
                .bodyValue(payload)
                .exchangeToMono(resp -> resp.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(body -> new HttpResult(resp.statusCode(), body)))
                .timeout(Duration.ofMillis(readTimeoutMs + 1000L))
                .block();

        if (result == null) throw new IllegalStateException("FastAPI empty response @ " + path);

        log.debug("[AI<-] {} body={}", result.status, preview(result.body, 500));

        // 2) HTTP 에러면 여기서 한 번에 처리(원문 바디 그대로 남김)
        if (!result.status.is2xxSuccessful()) {
            throw new IllegalStateException("FastAPI " + result.status + " @ " + path + ": " + result.body);
        }

        // 3) 파싱: envelope → raw 순서로 시도
        SayMyNameData data = null;
        try {
            FastApiEnvelope<SayMyNameData> env =
                    om.readValue(result.body, new TypeReference<FastApiEnvelope<SayMyNameData>>() {});
            if (env != null && env.getData() != null) {
                if (!isSuccess(env)) {
                    throw new IllegalStateException("FastAPI failure: code=" + env.getCode() + ", message=" + env.getMessage());
                }
                data = env.getData();
            }
        } catch (Exception ignore) {
            // envelope 아님 → 아래 raw로 재시도
        }

        if (data == null) {
            try {
                data = om.readValue(result.body, SayMyNameData.class);
            } catch (Exception parseEx) {
                // 파싱 실패 시 원문 바디를 그대로 남겨 원인 파악 용이
                throw new IllegalStateException("FastAPI parse error @ " + path + ": " + parseEx.getMessage()
                        + " ; body=" + preview(result.body, 1000), parseEx);
            }
        }

        String msg  = (data.getMessage() == null || data.getMessage().isBlank())
                ? "오굿스코어 한줄평을 준비하고 있어요!" : data.getMessage();
        Integer ttl = (data.getTtlSeconds() == null) ? DEFAULT_TTL_SECONDS : data.getTtlSeconds();
        Integer score    = data.getScore();

        return DashSayMyNameResponse.builder()
                .message(msg)
                .nextStep(null)
                .ohgoodScore(score)
                .build();
    }

    private record HttpResult(HttpStatusCode status, String body) {}

    @Override
    public DashSpendingAnalyzeResponse analyzeSpending(SpendingAnalyzeRequest req) {
        String endpoint = Optional.ofNullable(cfg.getPaths())
                .map(m -> m.get("analyzeSpending"))
                .orElse("/dash/analyze");
        String path = Optional.ofNullable(cfg.getApiPrefix()).orElse("") +
                (endpoint.startsWith("/") ? endpoint : "/" + endpoint);

        int readTimeoutMs = Optional.ofNullable(cfg.getHttp())
                .map(FastApiConfig.Http::getReadTimeout).orElse(5000);

        int n = (req.getTransactions() == null) ? 0 : req.getTransactions().size();
        log.info("[AI->] POST {} transactions={}", path, n);

        // 상태코드 + 원문 바디 확보(에러 바디 포함)
        HttpResult result = webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Internal-Token", cfg.getInternalToken()) // sayMyName와 동일하게 헤더 붙임
                .bodyValue(req)
                .exchangeToMono(resp -> resp.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(body -> new HttpResult(resp.statusCode(), body)))
                .timeout(Duration.ofMillis(readTimeoutMs + 1000L))
                .block();

        if (result == null) throw new IllegalStateException("FastAPI empty response @ " + path);
        log.debug("[AI<-] {} body={}", result.status, preview(result.body, 500));

        if (!result.status.is2xxSuccessful()) {
            throw new IllegalStateException("FastAPI " + result.status + " @ " + path + ": " + result.body);
        }

        // 1) envelope → 2) raw 순서로 파싱 (sayMyName와 동일 패턴)
        try {
            FastApiEnvelope<DashSpendingAnalyzeResponse> env =
                    om.readValue(result.body, new TypeReference<FastApiEnvelope<DashSpendingAnalyzeResponse>>() {});
            if (env != null && env.getData() != null) {
                if (!isSuccess(env)) {
                    throw new IllegalStateException("FastAPI failure: code=" + env.getCode() + ", message=" + env.getMessage());
                }
                return env.getData();
            }
        } catch (Exception ignore) {
            // envelope 아님 → raw로 재시도
        }

        try {
            return om.readValue(result.body, DashSpendingAnalyzeResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("FastAPI parse error @ " + path + ": " + e.getMessage()
                    + " ; body=" + preview(result.body, 1000), e);
        }
    }

    private record HttpResult(HttpStatusCode status, String body) {}

    private static String preview(String s, int max) {
        if (s == null) return "null";
        return (s.length() <= max) ? s : s.substring(0, max) + "...";
    }

    private boolean isSuccess(FastApiEnvelope<?> env) {
        Object s = env.getSuccess();
        if (s instanceof Boolean b) return b;
        if (s instanceof String str) return "true".equalsIgnoreCase(str);
        if ("200".equals(env.getCode())) return true;
        return "success".equalsIgnoreCase(String.valueOf(env.getMessage()));
    }

    @Data @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FastApiEnvelope<T> {
        private Object success;
        private String code;
        private String message;
        private T data;
    }

    @Data @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SayMyNameData {
        private String message;
        @JsonProperty("sessionId")
        private String sessionId;
        @JsonProperty("ttlSeconds")
        private Integer ttlSeconds;
        @JsonProperty("score")
        private Integer score;
    }
}