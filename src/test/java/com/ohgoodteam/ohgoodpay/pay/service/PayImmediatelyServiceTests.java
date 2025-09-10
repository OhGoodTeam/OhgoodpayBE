package com.ohgoodteam.ohgoodpay.pay.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ohgoodteam.ohgoodpay.common.entity.GradeEntity;
import com.ohgoodteam.ohgoodpay.common.entity.PaymentEntity;
import com.ohgoodteam.ohgoodpay.pay.dto.PayImmediatelyResponseDTO;

@SpringBootTest
public class PayImmediatelyServiceTests {
    
    @Autowired
    private PayImmediatelyService payImmediatelyService;

    @Test
    public void testClassifyUnpaidBills() {
        PayImmediatelyResponseDTO unpaidBills = payImmediatelyService.classifyUnpaidBills(1L);
        System.out.println("--------------------------------");
        System.out.println(unpaidBills);
        System.out.println("--------------------------------");
    }

    @Test   
    public void testRequestCustomerExtension() {
        boolean isExtension = payImmediatelyService.requestCustomerExtension(1L);
        System.out.println("--------------------------------");
        System.out.println(isExtension);
        System.out.println("--------------------------------");
    }

    @Test
    public void testRequestCustomerAutoExtension() {
        boolean isAutoExtension = payImmediatelyService.requestCustomerAutoExtension(1L);
        System.out.println("--------------------------------");
        System.out.println(isAutoExtension);
        System.out.println("--------------------------------");
    }

    @Test
    public void testFindbyCustomerGradeName() {
        GradeEntity grade = payImmediatelyService.findbyCustomerGradeName(1L);
        System.out.println("--------------------------------");
        System.out.println(grade);
        System.out.println("--------------------------------");
    }

    @Test
    public void testPayImmediately() {
        boolean isPayImmediately = payImmediatelyService.payImmediately(1L, new Long[] { 21L });
        System.out.println("--------------------------------");
        System.out.println(isPayImmediately);
        System.out.println("--------------------------------");
    }
}
