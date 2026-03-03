package com.project.baedalsodae.cart.dto.response;

import com.project.baedalsodae.cart.entity.Cart;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CartResponse(
		UUID cartId,
		UUID storeId,
		int totalItemCount,
		int totalQuantity,
		int totalAmount,
		List<CartItemResponse> items,
		Instant createdAt,
		Instant updatedAt
) {
	public static CartResponse from(Cart cart) {
		List<CartItemResponse> items = cart.getItems().stream()
				.map(CartItemResponse::from)
				.toList();

		return new CartResponse(
				cart.getId(),
				cart.getStore().getId(),
				items.size(),
				cart.getTotalQuantity(),
				cart.getTotalAmount(),
				items,
				cart.getCreatedAt(),
				cart.getUpdatedAt()
		);
	}

	public static CartResponse empty() {
		return new CartResponse(
				null,
				null,
				0,
				0,
				0,
				new ArrayList<>(),
				null,
				null
		);
	}
}
