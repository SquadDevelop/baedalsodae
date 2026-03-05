package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateOrderResponse(UUID orderId, OrderStatus status) {
    public static CreateOrderResponse from(Order order) {
        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .build();
    }
}
