package com.ohgoodteam.ohgoodpay.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ohgoodteam.ohgoodpay.common.entity.GradeEntity;
import com.ohgoodteam.ohgoodpay.pay.dto.GradeDTO;

public interface GradeRepository extends JpaRepository<GradeEntity, Long> {
    
    // 등급 조회
    GradeEntity findByGradeName(String gradeName);

    // entity to dto
    default GradeDTO entityToDto(GradeEntity gradeEntity) {
        return GradeDTO.builder()
            .gradeName(gradeEntity.getGradeName())
            .limitPrice(gradeEntity.getLimitPrice())
            .pointPercent(gradeEntity.getPointPercent())
            .upgrade(gradeEntity.getUpgrade())
            .build();
    }

    //dto to entity
    default GradeEntity dtoToEntity(GradeDTO gradeDTO) {
        return GradeEntity.builder()
            .gradeName(gradeDTO.getGradeName())
            .limitPrice(gradeDTO.getLimitPrice())
            .pointPercent(gradeDTO.getPointPercent())
            .upgrade(gradeDTO.getUpgrade())
            .build();
    }
}
