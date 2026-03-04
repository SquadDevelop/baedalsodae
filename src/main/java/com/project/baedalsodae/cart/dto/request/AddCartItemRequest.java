package com.project.baedalsodae.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddCartItemRequest(
		@NotNull UUID storeId,
		@NotNull UUID menuItemId,
		@Min(1) int quantity
) {
	public boolean isValidQuantity() {
		return this.quantity > 0;
	}
}
