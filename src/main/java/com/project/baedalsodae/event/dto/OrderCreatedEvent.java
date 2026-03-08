package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.order.entity.Order;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, UUID userId, BigDecimal finalAmount) {
    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(order.getId(), order.getUserId(), order.getFinalAmount());
    }
}
