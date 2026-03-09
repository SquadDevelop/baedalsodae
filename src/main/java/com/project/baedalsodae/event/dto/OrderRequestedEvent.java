package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.order.entity.Order;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderRequestedEvent(UUID orderId, UUID userId, BigDecimal finalAmount) {
    public static OrderRequestedEvent from(Order order) {
        return new OrderRequestedEvent(order.getId(), order.getUserId(), order.getFinalAmount());
    }
}
