package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.order.entity.Order;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderDeliveredEvent(UUID orderId, UUID userId, BigDecimal finalAmount) {
    public static OrderDeliveredEvent from(Order order) {
        return new OrderDeliveredEvent(order.getId(), order.getUserId(), order.getFinalAmount());
    }
}
