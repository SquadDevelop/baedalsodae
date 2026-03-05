package com.project.baedalsodae.order.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record OrderListResponse(List<OrderSummaryResponse> orders, boolean hasNext) {

    public static OrderListResponse empty() {
        return OrderListResponse.builder()
                .orders(List.of())
                .hasNext(false)
                .build();
    }
}
