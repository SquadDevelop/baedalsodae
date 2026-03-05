package com.project.baedalsodae.order.repository.impl;

import com.project.baedalsodae.order.dto.query.OrderListQuery;
import com.project.baedalsodae.order.dto.response.OrderSummaryResponse;
import com.project.baedalsodae.order.entity.QOrder;
import com.project.baedalsodae.order.repository.OrderQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.project.baedalsodae.order.repository.condition.OrderQueryCondition.*;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QOrder order = QOrder.order;

    @Override
    public List<OrderSummaryResponse> findOrdersByCustomer(OrderListQuery query) {
        return queryFactory
                .select(Projections.constructor(
                        OrderSummaryResponse.class,
                        order.id,
                        order.orderNo,
                        order.storeId,
                        order.status,
                        order.storeNameSnapshot,
                        order.finalAmount,
                        order.createdAt))
                .from(order)
                .where(
                        order.userId.eq(query.userId()),
                        order.isDeleted.isFalse(),
                        statusEq(query),
                        dateRange(query),
                        keywordContains(query),
                        cursorCondition(query))
                .orderBy(order.createdAt.desc(), order.id.desc())
                .limit(query.resolvedSize() + 1)
                .fetch();
    }

    @Override
    public List<OrderSummaryResponse> findOrdersByStore(OrderListQuery query) {
        return queryFactory
                        .select(Projections.constructor(
                                OrderSummaryResponse.class,
                                order.id,
                                order.orderNo,
                                order.storeId,
                                order.status,
                                order.storeNameSnapshot,
                                order.finalAmount,
                                order.createdAt))
                        .from(order)
                        .where(
                                order.storeId.eq(query.storeId()),
                                order.isDeleted.isFalse(),
                                statusEq(query),
                                dateRange(query),
                                orderNoEqIgnoreCase(query),
                                cursorCondition(query))
                        .orderBy(order.createdAt.desc(), order.id.desc())
                        .limit(query.resolvedSize() + 1)
                        .fetch();

    }
}
