package com.project.baedalsodae.order.repository.condition;

import com.project.baedalsodae.order.dto.query.OrderListQuery;
import com.project.baedalsodae.order.entity.QOrder;
import com.project.baedalsodae.order.entity.QOrderItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class OrderQueryCondition {
	private static final QOrder order = QOrder.order;
	private static final QOrderItem orderItem = QOrderItem.orderItem;

	public static BooleanExpression statusEq(OrderListQuery query) {
		return query.status() != null ? order.status.eq(query.status()) : null;
	}

	public static BooleanExpression dateRange(OrderListQuery query) {
		LocalDate start = query.resolvedStartDate();
		LocalDate end = query.resolvedEndDate();
		Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
		Instant endInstant = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
		return order.createdAt.goe(startInstant).and(order.createdAt.lt(endInstant));
	}

	public static BooleanExpression keywordContains(OrderListQuery query) {
		if (query.keyword() == null || query.keyword().isBlank()) return null;
		String keyword = query.keyword();
		return order.storeNameSnapshot
				.containsIgnoreCase(keyword)
				.or(
						order.id.in(
								JPAExpressions.select(orderItem.order.id)
										.from(orderItem)
										.where(orderItem.nameSnapshot.containsIgnoreCase(keyword))
						)
				);
	}

	public static BooleanExpression orderNoEqIgnoreCase(OrderListQuery query) {
		return query.orderNo() != null
				? order.orderNo.equalsIgnoreCase(query.orderNo())
				: null;
	}

	public static BooleanExpression cursorCondition(OrderListQuery query) {
		if (query.cursorCreatedAt() == null || query.cursorId() == null) return null;
		return order.createdAt
				.lt(query.cursorCreatedAt())
				.or(
						order.createdAt
								.eq(query.cursorCreatedAt())
								.and(order.id.lt(query.cursorId())));
	}

}
