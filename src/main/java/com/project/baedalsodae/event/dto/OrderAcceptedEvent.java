package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.order.entity.Order;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderAcceptedEvent(UUID orderId, UUID userId, BigDecimal finalAmount) {
    public static OrderAcceptedEvent from(Order order) {
        return new OrderAcceptedEvent(order.getId(), order.getUserId(), order.getFinalAmount());
    }
}
