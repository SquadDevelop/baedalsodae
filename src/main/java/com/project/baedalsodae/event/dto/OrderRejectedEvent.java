package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.order.entity.Order;
import java.util.UUID;

public record OrderRejectedEvent(UUID orderId, UUID userId, int finalAmount) {
    public static OrderRejectedEvent from(Order order) {
        return new OrderRejectedEvent(order.getId(), order.getUserId(), order.getFinalAmount());
    }
}
