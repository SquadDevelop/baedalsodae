package com.project.baedalsodae.order.repository.impl;

import static com.project.baedalsodae.order.repository.condition.OrderQueryCondition.*;

import com.project.baedalsodae.order.dto.query.OrderListQuery;
import com.project.baedalsodae.order.dto.response.OrderSummaryResponse;
import com.project.baedalsodae.order.entity.QOrder;
import com.project.baedalsodae.order.repository.OrderQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QOrder order = QOrder.order;

    @Override
    public List<OrderSummaryResponse> findOrdersByCustomer(OrderListQuery query) {
        return queryFactory
                .select(
                        Projections.constructor(
                                OrderSummaryResponse.class,
                                order.id,
                                order.storeId,
                                order.orderNo,
                                order.status,
                                order.storeNameSnapshot,
                                order.finalAmount,
                                order.createdAt))
                .from(order)
                .where(
                        order.userId.eq(query.userId()),
                        isDeletedIsFalse(),
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
                .select(
                        Projections.constructor(
                                OrderSummaryResponse.class,
                                order.id,
                                order.storeId,
                                order.orderNo,
                                order.status,
                                order.storeNameSnapshot,
                                order.finalAmount,
                                order.createdAt))
                .from(order)
                .where(
                        order.storeId.eq(query.storeId()),
                        isDeletedIsFalse(),
                        statusEq(query),
                        dateRange(query),
                        orderNoEqIgnoreCase(query),
                        cursorCondition(query))
                .orderBy(order.createdAt.desc(), order.id.desc())
                .limit(query.resolvedSize() + 1)
                .fetch();
    }
}
