package com.ohgoodteam.ohgoodpay.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.ohgoodteam.ohgoodpay.common.dto.CustomerDTO;
import com.ohgoodteam.ohgoodpay.common.entity.CustomerEntity;
import com.ohgoodteam.ohgoodpay.common.entity.GradeEntity;

import jakarta.transaction.Transactional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    
    // customerId로 회원 조회
    CustomerEntity findByCustomerId(Long customerId);

    // customerId로 회원 연장 상태 업데이트
    @Transactional
    @Modifying
    @Query("UPDATE CustomerEntity c SET c.isExtension = :isExtension WHERE c.customerId = :customerId")
    int updateCustomerIsExtension(boolean isExtension, Long customerId);

    // customerId로 회원 자동 연장 상태 업데이트
    @Transactional
    @Modifying
    @Query("UPDATE CustomerEntity c SET c.isAuto = :isAuto, c.extensionCnt = c.extensionCnt + 1 WHERE c.customerId = :customerId")
    int updateCustomerIsAuto(boolean isAuto, Long customerId);

    // customerId로 회원 gradePoint 업데이트
    @Transactional
    @Modifying
    @Query("UPDATE CustomerEntity c SET c.gradePoint = c.gradePoint + :gradePoint WHERE c.customerId = :customerId")
    int updateCustomerGradePoint(int gradePoint, Long customerId);

    // gradePoint 로 grade_name 업데이트
    @Transactional
    @Modifying
    @Query("UPDATE CustomerEntity c SET c.grade.gradeName = :gradeName WHERE c.customerId = :customerId")
    int updateCustomerGradeName(String gradeName, Long customerId);

    // customerId로 회원 balance 업데이트
    @Transactional
    @Modifying
    @Query("UPDATE CustomerEntity c SET c.balance = c.balance + :balance WHERE c.customerId = :customerId")
    int plusCustomerBalance(int balance, Long customerId);

    // entity to dto
    default CustomerDTO entityToDto(CustomerEntity customerEntity) {
        return CustomerDTO.builder()
            .customerId(customerEntity.getCustomerId())
            .name(customerEntity.getName())
            .emailId(customerEntity.getEmailId())
            .birth(customerEntity.getBirth())
            .account(customerEntity.getAccount())
            .accountName(customerEntity.getAccountName())
            .point(customerEntity.getPoint())
            .isBlocked(customerEntity.isBlocked())
            .profileImg(customerEntity.getProfileImg())
            .nickname(customerEntity.getNickname())
            .introduce(customerEntity.getIntroduce())
            .score(customerEntity.getScore())
            .hobby(customerEntity.getHobby())
            .blockedCnt(customerEntity.getBlockedCnt())
            .extensionCnt(customerEntity.getExtensionCnt())
            .joinDate(customerEntity.getJoinDate())
            .isExtension(customerEntity.isExtension())
            .isAuto(customerEntity.isAuto())
            .gradePoint(customerEntity.getGradePoint())
            .gradeName(customerEntity.getGrade().getGradeName())
            .balance(customerEntity.getBalance())
            .build();
    }

    // dto to entity
    default CustomerEntity dtoToEntity(CustomerDTO customerDTO) {
        return CustomerEntity.builder()
            .customerId(customerDTO.getCustomerId())
            .name(customerDTO.getName())
            .emailId(customerDTO.getEmailId())
            .birth(customerDTO.getBirth())
            .account(customerDTO.getAccount())
            .accountName(customerDTO.getAccountName())
            .point(customerDTO.getPoint())
            .isBlocked(customerDTO.isBlocked())
            .profileImg(customerDTO.getProfileImg())
            .nickname(customerDTO.getNickname())
            .introduce(customerDTO.getIntroduce())
            .score(customerDTO.getScore())
            .hobby(customerDTO.getHobby())
            .blockedCnt(customerDTO.getBlockedCnt())
            .extensionCnt(customerDTO.getExtensionCnt())
            .joinDate(customerDTO.getJoinDate())
            .isExtension(customerDTO.isExtension())
            .isAuto(customerDTO.isAuto())
            .gradePoint(customerDTO.getGradePoint())
            .grade(GradeEntity.builder().gradeName(customerDTO.getGradeName()).build())
            .balance(customerDTO.getBalance())
            .build();
    }
}
