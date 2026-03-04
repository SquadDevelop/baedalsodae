package com.project.baedalsodae.cart.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateCartItemQuantityRequest(
		@Min(1) int quantity
) {
	public boolean isValidQuantity() {
		return this.quantity > 0;
	}
}
