package com.ohgoodteam.ohgoodpay.shorts.controller.search;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.search.ShortsSearchResponseDTO.CursorResponse;
import com.ohgoodteam.ohgoodpay.shorts.service.search.ShortsSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ShortsSearchController {
    private final ShortsSearchService shortsSearchService;

    /**
     * 쇼츠 검색
     * @param q
     * @param limit
     * @param lastId
     * @param lastDate
     * @param lastScore
     * @return
     */
    @GetMapping("/public/shorts/search")
    public CursorResponse search(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Long lastId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastDate,
        @RequestParam(required = false) BigDecimal lastScore
    ){
        return shortsSearchService.getFeed(q, limit, lastId, lastDate, lastScore);
    }
}
