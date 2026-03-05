package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.order.entity.enums.OrderStatus;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record OrderSummaryResponse(
        UUID orderId,
        UUID storeId,
        String orderNo,
        OrderStatus status,
        String storeNameSnapshot,
        int finalAmount,
        LocalDateTime createdAt,
        Instant createdAtCursor
        ) {

}
