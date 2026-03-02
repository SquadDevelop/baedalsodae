package com.project.baedalsodae.cart.dto.request;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import jakarta.validation.constraints.Min;
import lombok.NonNull;

import java.util.UUID;

public record AddCartItemRequest(
		@NonNull UUID storeId,
		@NonNull UUID menuItemId,
		@Min(1) int quantity
) {
	public boolean isValidQuantity() {
		return this.quantity > 0;
	}
}
