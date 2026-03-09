package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.global.common.util.TimeUtils;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OrderSummaryResponse {
    private UUID orderId;
    private UUID storeId;
    private String orderNo;
    private OrderStatus status;
    private String storeNameSnapshot;
    private BigDecimal finalAmount;
    private Instant createdAtCursor;
    private LocalDateTime createdAt;

    public OrderSummaryResponse(
            UUID orderId,
            UUID storeId,
            String orderNo,
            OrderStatus status,
            String storeNameSnapshot,
            BigDecimal finalAmount,
            Instant createdAtCursor) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.orderNo = orderNo;
        this.status = status;
        this.storeNameSnapshot = storeNameSnapshot;
        this.finalAmount = finalAmount;
        this.createdAtCursor = createdAtCursor;
        this.createdAt = TimeUtils.toLocalDateTime(createdAtCursor);
    }
}
