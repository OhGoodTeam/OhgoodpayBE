//package com.ohgoodteam.ohgoodpay.pay.service;
//
//import java.time.YearMonth;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import org.springframework.stereotype.Service;
//
//import com.ohgoodteam.ohgoodpay.common.entity.PaymentEntity;
//import com.ohgoodteam.ohgoodpay.pay.repository.PaymentRepository;
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class PayImmediatelyServiceImpl implements PayImmediatelyService {
//
//    private final PaymentRepository paymentRepository;
//
//    @Override
//    public List<List<PaymentEntity>> classifyUnpaidBills(Long customerId) {
//        // 고객의 미납부건 조회
//        List<PaymentEntity> unpaidBills = paymentRepository.findByCustomerCustomerIdAndIsExpiredFalse(customerId);
//
//        // 월별로 그룹화
//        Map<YearMonth, List<PaymentEntity>> billsByMonth = unpaidBills.stream()
//            .collect(Collectors.groupingBy(payment ->
//                YearMonth.from(payment.getDate())
//            ));
//
//        // 월별로 정렬된 리스트 생성 (최신 월부터)
//        List<List<PaymentEntity>> result = billsByMonth.entrySet().stream()
//            .sorted((entry1, entry2) -> entry1.getKey().compareTo(entry2.getKey()))
//            .map(Map.Entry::getValue)
//            .collect(Collectors.toList());
//
//        return result;
//    }
//}
