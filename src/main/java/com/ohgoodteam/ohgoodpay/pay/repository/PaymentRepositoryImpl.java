package com.ohgoodteam.ohgoodpay.pay.repository;

import com.ohgoodteam.ohgoodpay.common.entity.PaymentEntity;
import com.ohgoodteam.ohgoodpay.common.entity.QPaymentEntity;
import com.ohgoodteam.ohgoodpay.common.entity.QPaymentRequestEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<PaymentEntity> findRecent3Months(Long customerId,
                                                 LocalDateTime fromAt,
                                                 LocalDateTime toExcl,
                                                 boolean onlyValidated) {
        QPaymentEntity p = QPaymentEntity.paymentEntity;
        QPaymentRequestEntity r = QPaymentRequestEntity.paymentRequestEntity;

        BooleanBuilder where = new BooleanBuilder()
                .and(p.customer.customerId.eq(customerId))
                .and(p.date.goe(fromAt))
                .and(p.date.lt(toExcl))
                .and(p.isExpired.eq(false));                 // primitive이므로 isNull() 금지

        JPAQuery<PaymentEntity> q = query.selectFrom(p)
                // optional=false 이므로 join으로 충분. (비페이징에서는 fetchJoin으로 N+1 회피 OK)
                .join(p.paymentRequest, r).fetchJoin()
                .where(where)
                .orderBy(p.date.desc());

        if (onlyValidated) {
            // PaymentRequestEntity.isValidated 가 primitive boolean이면 NULL 허용 못함
            q.where(r.isValidated.eq(true));
            // NULL도 허용하려면 엔티티 필드를 Boolean으로 바꾼 후:
            // q.where(r.isValidated.isTrue().or(r.isValidated.isNull()));
        }

        return q.fetch();
    }

    @Override
    public Page<PaymentEntity> findRecent3Months(Long customerId,
                                                 LocalDateTime fromAt,
                                                 LocalDateTime toExcl,
                                                 boolean onlyValidated,
                                                 Pageable pageable) {
        QPaymentEntity p = QPaymentEntity.paymentEntity;
        QPaymentRequestEntity r = QPaymentRequestEntity.paymentRequestEntity;

        BooleanBuilder where = new BooleanBuilder()
                .and(p.customer.customerId.eq(customerId))
                .and(p.date.goe(fromAt))
                .and(p.date.lt(toExcl))
                .and(p.isExpired.eq(false));

        // ⚠️ 페이징에서는 fetchJoin 사용하지 않음 (중복/경고/페이징 왜곡 방지)
        JPAQuery<PaymentEntity> content = query.selectFrom(p)
                .join(p.paymentRequest, r)
                .where(where);

        if (onlyValidated) {
            content.where(r.isValidated.eq(true));
        }

        // 정렬 매핑 (기본: date desc)
        if (pageable.getSort().isUnsorted()) {
            content.orderBy(p.date.desc());
        } else {
            // 필요한 필드만 허용적으로 매핑
            pageable.getSort().forEach(order -> {
                String prop = order.getProperty();
                switch (prop) {
                    case "date" -> content.orderBy(order.isAscending() ? p.date.asc() : p.date.desc());
                    case "totalPrice" -> content.orderBy(order.isAscending() ? p.totalPrice.asc() : p.totalPrice.desc());
                    default -> content.orderBy(p.date.desc()); // 안전 기본값
                }
            });
        }

        List<PaymentEntity> rows = content
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query.select(p.count())
                .from(p)
                .join(p.paymentRequest, r)
                .where(where)
                .fetchOne();

        return new PageImpl<>(rows, pageable, total == null ? 0 : total);
    }
}
