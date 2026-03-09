package com.project.baedalsodae.order.dto.query;

import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OrderListQuery(
        UUID userId,
        OrderStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String keyword,
        String orderNo,
        UUID storeId,
        Integer size,
        Instant cursorCreatedAt,
        UUID cursorId) {

    public Integer resolvedSize() {
        return size != null ? size : 20;
    }

    public LocalDate resolvedStartDate() {
        return startDate != null ? startDate : LocalDate.now().minusMonths(3);
    }

    public LocalDate resolvedEndDate() {
        return endDate != null ? endDate : LocalDate.now();
    }

    public static OrderListQuery forCustomer(UUID userId, OrderListRequest request) {
        return OrderListQuery.builder()
                .userId(userId)
                .status(request.status())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .keyword(request.keyword())
                .size(request.size())
                .cursorCreatedAt(request.cursorCreatedAt())
                .cursorId(request.cursorId())
                .build();
    }

    public static OrderListQuery forOwner(UUID storeId, OrderListRequest request) {
        return OrderListQuery.builder()
                .storeId(storeId)
                .status(request.status())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .keyword(request.keyword())
                .orderNo(request.orderNo())
                .size(request.size())
                .cursorCreatedAt(request.cursorCreatedAt())
                .cursorId(request.cursorId())
                .build();
    }

    public static OrderListQuery forAdmin(OrderListRequest request) {
        return OrderListQuery.builder()
                .status(request.status())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .keyword(request.keyword())
                .orderNo(request.orderNo())
                .size(request.size())
                .cursorCreatedAt(request.cursorCreatedAt())
                .cursorId(request.cursorId())
                .build();
    }
}
