package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Builder
public record OrderSummaryResponse(
        UUID orderId,
        String orderNo,
        OrderStatus status,
        String storeNameSnapshot,
        int finalAmount,
        LocalDateTime createdAt,
        Instant createdAtCursor
        ) {

    public static OrderSummaryResponse from(Order order) {
        return OrderSummaryResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .storeNameSnapshot(order.getStoreNameSnapshot())
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getLocalDateCreatedAt())
                .createdAtCursor(order.getCreatedAt())
                .build();
    }
}
