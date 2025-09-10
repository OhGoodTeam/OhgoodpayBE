package com.ohgoodteam.ohgoodpay.common.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ohgoodteam.ohgoodpay.common.dto.MypageDTO;
import com.ohgoodteam.ohgoodpay.common.service.MypageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*")
public class MypageController {
    
    private final MypageService mypageService;

    @GetMapping("/mypage/{customerId}")
    public ResponseEntity<MypageDTO> getMypage(@PathVariable Long customerId) {
        MypageDTO mypageDTO = mypageService.getMypageInfo(customerId);
        if(mypageDTO == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(mypageDTO, HttpStatus.OK);
    }
}
