package com.project.baedalsodae.order.dto.event;

import com.project.baedalsodae.order.entity.Order;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, UUID userId, int finalAmount) {
    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(order.getId(), order.getUserId(), order.getFinalAmount());
    }
}
