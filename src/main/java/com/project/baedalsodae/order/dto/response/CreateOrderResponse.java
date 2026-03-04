package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.enums.OrderStatus;

import java.util.UUID;

public record CreateOrderResponse(
    UUID orderId,
	OrderStatus status
) {
	public static CreateOrderResponse from(Order order) {
		return new CreateOrderResponse(order.getId(), order.getStatus());
	}
}