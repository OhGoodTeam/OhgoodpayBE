package com.ohgoodteam.ohgoodpay.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "comment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Log4j2
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shorts_id")
    private ShortsEntity shorts;

    @Column(nullable = false)
    private Long gno;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime date;
}
