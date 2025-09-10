package com.ohgoodteam.ohgoodpay.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Entity
@Table(name = "grade")
@Builder
@Getter
@Log4j2
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GradeEntity {
    @Id
    private String gradeName;

    @Column(nullable = false)
    private int upgrade;

    @Column(nullable = false)
    private int limitPrice;

    @Column(nullable = false)
    private float pointPercent;
}
