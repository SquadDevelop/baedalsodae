package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.order.entity.Order;

import java.util.UUID;

public record OrderCookedEvent(UUID orderId, UUID userId, int finalAmount) {
    public static OrderCookedEvent from(Order order) {
        return new OrderCookedEvent(order.getId(), order.getUserId(), order.getFinalAmount());
    }
}
