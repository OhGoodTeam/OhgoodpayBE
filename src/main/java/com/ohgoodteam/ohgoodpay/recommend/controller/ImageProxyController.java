package com.ohgoodteam.ohgoodpay.recommend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
@Tag(name = "이미지 프록시", description = "외부 이미지 프록시 API")
public class ImageProxyController {

    private final RestTemplate restTemplate;

    public ImageProxyController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/image-proxy")
    @PermitAll
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            HttpHeaders headers = new HttpHeaders();

            // 원본 응답의 Content-Type을 사용하거나 기본값 설정
            String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType != null) {
                headers.setContentType(MediaType.parseMediaType(contentType));
            } else {
                headers.setContentType(MediaType.IMAGE_JPEG);
            }

            // 캐시 설정
            headers.setCacheControl("public, max-age=3600");

            // CORS 헤더 추가
            headers.add("Access-Control-Allow-Origin", "*");

            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("이미지 프록시 요청 실패: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}