package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.order.entity.Order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCancelRequestedEvent(UUID orderId, UUID userId, BigDecimal finalAmount) {
    public static OrderCancelRequestedEvent from(Order order) {
        return new OrderCancelRequestedEvent(order.getId(), order.getUserId(), order.getFinalAmount());
    }
}
