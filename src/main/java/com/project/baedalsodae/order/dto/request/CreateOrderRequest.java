package com.project.baedalsodae.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateOrderRequest(
		@NotNull UUID cartId,
		@NotNull UUID addressId,
		String storeRequestMessage,
		String deliveryRequestMessage
) {

}