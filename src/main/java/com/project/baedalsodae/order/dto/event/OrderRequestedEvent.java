package com.project.baedalsodae.order.dto.event;

import com.project.baedalsodae.order.entity.Order;

import java.util.UUID;

public record OrderRequestedEvent(UUID orderId, UUID userId, int finalAmount) {
    public static OrderRequestedEvent from(Order order) {
        return new OrderRequestedEvent(order.getId(), order.getUserId(), order.getFinalAmount());
    }
}
