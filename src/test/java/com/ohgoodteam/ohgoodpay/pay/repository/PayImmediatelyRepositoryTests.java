package com.ohgoodteam.ohgoodpay.pay.repository;

import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ohgoodteam.ohgoodpay.common.entity.GradeEntity;
// import com.ohgoodteam.ohgoodpay.common.entity.CustomerEntity;
import com.ohgoodteam.ohgoodpay.common.entity.PaymentEntity;
import com.ohgoodteam.ohgoodpay.common.repository.CustomerRepository;

@SpringBootTest
public class PayImmediatelyRepositoryTests {

    // private final CustomerRepository customerRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // 고객의 결제 내역 조회
    @Test
    public void testFindByCustomerId() {
        // CustomerEntity customer = customerRepository.findById(1L).orElseThrow(() -> new RuntimeException("Customer not found"));
        List<PaymentEntity> payments = paymentRepository.findByCustomerCustomerId(1L);
        System.out.println("--------------------------------");
        for (PaymentEntity payment : payments) {
            System.out.println(payment);
        }
        System.out.println("--------------------------------");
    }


    // 고객의 결제 내역 조회 (만료되지 않은 결제 내역)
    @Test
    public void testFindByCustomerIdAndIsExpiredFalse() {
        List<PaymentEntity> payments = paymentRepository.findByCustomerCustomerIdAndIsExpiredFalse(1L);
        System.out.println("--------------------------------");
        for (PaymentEntity payment : payments) {
            System.out.println(payment);
        }
        System.out.println("--------------------------------");
    }

    // 결제 단일 건 조회
    @Test
    public void testFindByPaymentId() {
        PaymentEntity payment = paymentRepository.findByPaymentId(21L);
        System.out.println("--------------------------------");
        System.out.println(payment);
        System.out.println(payment.getDate());
        System.out.println(YearMonth.from(payment.getDate()));
        System.out.println(YearMonth.now());
        System.out.println(YearMonth.from(payment.getDate()).equals(YearMonth.now()));
        System.out.println();
        System.out.println("--------------------------------");
    }

    // 등급 조회
    @Test
    public void testFindByGradeId() {
        List<GradeEntity> grades = gradeRepository.findAll();
        System.out.println("--------------------------------");
        for (GradeEntity grade : grades) {
            System.out.println(grade);
        }
        System.out.println("--------------------------------");
    }

    //결제건 납부 업데이트
    @Test
    public void testUpdatePaymentIsExpired() {
        int result = paymentRepository.updatePaymentIsExpiredByPaymentId(true, new Long[] { 19L,20L });
        System.out.println("--------------------------------");
        System.out.println(result);
        System.out.println("--------------------------------");
    }

    // 회원 수동 연장 업데이트
    // @Test
    // public void testUpdateCustomerIsExtension() {
    //     int result = customerRepository.updateCustomerIsExtension(true, 1L);
    //     System.out.println("--------------------------------");
    //     System.out.println(result);
    //     System.out.println("--------------------------------");
    // }

    // 회원 자동 연장 업데이트
    // @Test
    // public void testUpdateCustomerIsAuto() {
    //     int result = customerRepository.updateCustomerIsAuto(true, 1L);
    //     System.out.println("--------------------------------");
    //     System.out.println(result);
    //     System.out.println("--------------------------------");
    // }

    // 해당 월 미납부 결제 조회
    @Test
    public void testFindByCustomerIdAndIsExpiredFalseAndDate() {
        List<PaymentEntity> payments = paymentRepository.findByCustomerCustomerIdAndIsExpiredFalseAndYearMonth(1L, 2024, 8);
        System.out.println("--------------------------------");
        for (PaymentEntity payment : payments) {
            System.out.println(payment.getDate());
        }
        System.out.println("--------------------------------");
    }
}
