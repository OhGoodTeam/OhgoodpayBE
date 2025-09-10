package com.ohgoodteam.ohgoodpay.pay.service;

import java.beans.Transient;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ohgoodteam.ohgoodpay.common.dto.CustomerDTO;
import com.ohgoodteam.ohgoodpay.common.entity.CustomerEntity;
import com.ohgoodteam.ohgoodpay.common.entity.GradeEntity;
import com.ohgoodteam.ohgoodpay.common.entity.PaymentEntity;
import com.ohgoodteam.ohgoodpay.common.repository.CustomerRepository;
import com.ohgoodteam.ohgoodpay.pay.repository.PaymentRepository;   
import com.ohgoodteam.ohgoodpay.pay.repository.GradeRepository;
import com.ohgoodteam.ohgoodpay.pay.dto.GradeDTO;
import com.ohgoodteam.ohgoodpay.pay.dto.PayImmediatelyResponseDTO;
import com.ohgoodteam.ohgoodpay.pay.dto.PaymentResponseDTO;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayImmediatelyServiceImpl implements PayImmediatelyService {
    
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final GradeRepository gradeRepository;

    // 전체 결제건 조회
    @Override
    public List<PaymentResponseDTO> getAllPayment(Long customerId) {
        List<PaymentEntity> paymentEntities = paymentRepository.findByCustomerCustomerId(customerId);
        List<PaymentResponseDTO> paymentResponseDTOs = new ArrayList<>();
        for (PaymentEntity paymentEntity : paymentEntities) {
            PaymentResponseDTO paymentResponseDTO = entityToDto(paymentEntity);
            paymentResponseDTOs.add(paymentResponseDTO);
        }
        return paymentResponseDTOs;
    }
    
    // 미납부건 월별 분류
    @Override
    public PayImmediatelyResponseDTO classifyUnpaidBills(Long customerId) {
        // 고객 정보 조회
        CustomerEntity customer = customerRepository.findByCustomerId(customerId);
        CustomerDTO customerDTO = customerRepository.entityToDto(customer);

        // 고객 등급 조회
        GradeEntity grade = findbyCustomerGradeName(customerId);
        GradeDTO gradeDTO = gradeRepository.entityToDto(grade);

        // 고객의 미납부건 조회
        List<PaymentEntity> unpaidBills = paymentRepository.findByCustomerCustomerIdAndIsExpiredFalse(customerId);
        
        // 월별로 그룹화
        Map<YearMonth, List<PaymentEntity>> billsByMonth = unpaidBills.stream()
            .collect(Collectors.groupingBy(payment -> 
                YearMonth.from(payment.getDate())
            ));
        
        // 월별로 정렬된 리스트 생성 (최신 월부터)
        List<List<PaymentEntity>> entityList = billsByMonth.entrySet().stream()
            .sorted((entry1, entry2) -> entry1.getKey().compareTo(entry2.getKey()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());


        List<List<PaymentResponseDTO>> result = new ArrayList<>();
        for (List<PaymentEntity> entity : entityList) {
            List<PaymentResponseDTO> paymentResponseDTOList = new ArrayList<>();
            for (PaymentEntity payment : entity) {
                PaymentResponseDTO paymentResponseDTO = entityToDto(payment);
                paymentResponseDTOList.add(paymentResponseDTO);
            }
            result.add(paymentResponseDTOList);
        }
         
        PayImmediatelyResponseDTO payImmediatelyResponseDTO = PayImmediatelyResponseDTO.builder()
            .customerId(customerDTO.getCustomerId())
            .gradeName(gradeDTO.getGradeName())
            .limitPrice(gradeDTO.getLimitPrice())
            .pointPercent(gradeDTO.getPointPercent())
            .account(customerDTO.getAccount())
            .accountName(customerDTO.getAccountName())
            .balance(customerDTO.getBalance())
            .isExtension(customerDTO.isExtension())
            .isAuto(customerDTO.isAuto())
            .unpaidBills(result)
            .build();

        return payImmediatelyResponseDTO;
    }

    // 고객 수동 연장 신청
    @Override
    public boolean requestCustomerExtension(Long customerId) {
        CustomerEntity customer = customerRepository.findByCustomerId(customerId);

        // 이미 연장 상태면 신청 불가, 전월 미납부건이 없으면 신청 불가
        if(customer.isExtension() || !checkUnpaidBills(customerId, YearMonth.now().minusMonths(1))) {
            return false;
        }
        int result = customerRepository.updateCustomerIsExtension(true, customerId);
        return result > 0;
    }

    // 고객 자동 연장 신청
    @Override
    @Transactional
    public boolean requestCustomerAutoExtension(Long customerId) {
        CustomerEntity customer = customerRepository.findByCustomerId(customerId);

        // 이미 연장 상태면 신청 불가, 전월 미납부건이 없으면 신청 불가
        if(customer.isExtension() || !checkUnpaidBills(customerId, YearMonth.now().minusMonths(1))) {
            return false;
        }
        customerRepository.updateCustomerIsExtension(true, customerId);
        customerRepository.updateCustomerIsAuto(true, customerId);
        return true;
    }

    // 고객 등급 및 한도 조회
    @Override
    public GradeEntity findbyCustomerGradeName(Long customerId) {
        String gradeName = customerRepository.findByCustomerId(customerId).getGrade().getGradeName();
        return gradeRepository.findByGradeName(gradeName);
    }

    // 결제건 납부 처리
    @Override
    public boolean payImmediately(Long customerId, Long[] lists) {
        int result = paymentRepository.updatePaymentIsExpiredByPaymentId(true, lists);
        // 들어온 결제건이 이번 달인지 확인
        PaymentEntity payment = paymentRepository.findByPaymentId(lists[0]);
        YearMonth paymentYearMonth = YearMonth.from(payment.getDate());
        YearMonth nowYearMonth = YearMonth.now();
        int sumPrice = paymentRepository.sumPriceByPaymentId(lists);
        // payment의 년월과 현재 년월이 같은지 체크
        if (paymentYearMonth.equals(nowYearMonth)) {
            // 이번 달 결제건이면 balance 업데이트 (한도 풀어줌)
            customerRepository.plusCustomerBalance(sumPrice, customerId);
        }
        
        if(result > 0) {
            int gradePoint = sumPrice / 10000;
            customerRepository.updateCustomerGradePoint(gradePoint, customerId);
        }
        //지금 년월과 납부된 결제건의 년월이 다른 경우 연장 상태 해제 검토
        if (!paymentYearMonth.equals(nowYearMonth)) {
            if (checkUnpaidBills(customerId, paymentYearMonth)) {
                releaseExtension(customerId);
            } 
        }
        return result > 0;
    }

    // 미납건이 있는지 확인. 확인 후 미납건이 있으면 false 없으면 true
    public boolean checkUnpaidBills(Long customerId, YearMonth yearMonth) {
        List<PaymentEntity> unpaidBills = paymentRepository.findByCustomerCustomerIdAndIsExpiredFalseAndYearMonth(customerId, yearMonth.getYear(), yearMonth.getMonthValue());
        if(unpaidBills.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    // 고객이 연장 상태면 해제
    public void releaseExtension(Long customerId) {
        CustomerEntity customer = customerRepository.findByCustomerId(customerId);
        if(customer.isExtension()) {
            customerRepository.updateCustomerIsExtension(false, customerId);
            customerRepository.updateCustomerIsAuto(false, customerId);
        }
    }
}
