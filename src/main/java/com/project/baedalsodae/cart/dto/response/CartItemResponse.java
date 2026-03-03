package com.project.baedalsodae.cart.dto.response;

import com.project.baedalsodae.cart.entity.CartItem;

import java.util.UUID;

public record CartItemResponse(
		UUID cartItemId,
		UUID menuItemId,
		String menuName,
		int unitPrice,
		int quantity,
		int lineAmount
) {
	public static CartItemResponse from(CartItem item) {
		return new CartItemResponse(
				item.getId(),
				item.getMenuItem().getId(),
				item.getMenuItem().getName(),
				item.getMenuItem().getPrice(),
				item.getQuantity(),
				item.getLineAmount()
		);
	}

}
