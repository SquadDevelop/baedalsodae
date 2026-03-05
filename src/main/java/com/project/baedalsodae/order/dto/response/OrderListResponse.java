package com.project.baedalsodae.order.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderListResponse {

    private List<OrderSummaryResponse> orders;
    private boolean hasNext;
    private Instant nextCursorCreatedAt;
    private UUID nextCursorId;

    public static OrderListResponse empty() {
        return OrderListResponse.builder().orders(List.of()).hasNext(false).build();
    }

    public static OrderListResponse from(List<OrderSummaryResponse> orders, boolean hasNext) {
        if (orders.isEmpty()) return empty();

        OrderSummaryResponse lastOrder = orders.get(orders.size() - 1);
        return OrderListResponse.builder()
                .orders(orders)
                .hasNext(hasNext)
                .nextCursorCreatedAt(hasNext ? lastOrder.getCreatedAtCursor() : null)
                .nextCursorId(hasNext ? lastOrder.getOrderId() : null)
                .build();
    }
}
