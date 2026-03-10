package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.order.entity.Order;
import java.util.UUID;

public record OrderDeliveredEvent(UUID orderId, UUID storeId) {
    public static OrderDeliveredEvent from(Order order) {
        return new OrderDeliveredEvent(order.getId(), order.getStoreId());
    }
}
