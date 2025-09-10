package com.ohgoodteam.ohgoodpay.pay.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ohgoodteam.ohgoodpay.common.entity.GradeEntity;
import com.ohgoodteam.ohgoodpay.common.entity.PaymentEntity;
import com.ohgoodteam.ohgoodpay.common.repository.CustomerRepository;
import com.ohgoodteam.ohgoodpay.pay.dto.PayImmediatelyResponseDTO;
import com.ohgoodteam.ohgoodpay.pay.dto.PaymentResponseDTO;
import com.ohgoodteam.ohgoodpay.pay.service.PayImmediatelyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*")
public class PayImmediatelyController {

    private final PayImmediatelyService payImmediatelyService;
    private final CustomerRepository customerRepository;

    // 납부 페이지 진입시 초기 정보 조회
    @GetMapping("/payment/info/{customerId}")
    public ResponseEntity<PayImmediatelyResponseDTO> getPayImmediately(@PathVariable Long customerId) {

        PayImmediatelyResponseDTO response = payImmediatelyService.classifyUnpaidBills(customerId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 즉시 납부 요청
    @PostMapping("/payment/immediately/{customerId}")
    public ResponseEntity<PayImmediatelyResponseDTO> payImmediately(@PathVariable Long customerId, @RequestBody Long[] lists) {
        boolean isPayImmediately = payImmediatelyService.payImmediately(customerId, lists);
        if(isPayImmediately) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // 수동 연장 요청
    @PostMapping("/payment/extension/{customerId}")
    public ResponseEntity<PayImmediatelyResponseDTO> requestCustomerExtension(@PathVariable Long customerId) {
        boolean isRequestCustomerExtension = payImmediatelyService.requestCustomerExtension(customerId);
        if(isRequestCustomerExtension) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // 결제 상세 내역 페이지 진입시 초기 정보 조회
    @GetMapping("/payment/history/{customerId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentDetail(@PathVariable Long customerId) {
        List<PaymentResponseDTO> response = payImmediatelyService.getAllPayment(customerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
