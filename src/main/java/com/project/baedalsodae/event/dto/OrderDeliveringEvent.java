package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.order.entity.Order;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderDeliveringEvent(UUID orderId, UUID userId, BigDecimal finalAmount) {
    public static OrderDeliveringEvent from(Order order) {
        return new OrderDeliveringEvent(order.getId(), order.getUserId(), order.getFinalAmount());
    }
}
