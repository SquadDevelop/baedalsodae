package com.project.baedalsodae.cart.service;

import com.project.baedalsodae.cart.dto.request.AddCartItemRequest;
import com.project.baedalsodae.cart.dto.request.UpdateCartItemQuantityRequest;
import com.project.baedalsodae.cart.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {
	CartResponse getCart(UUID userId);

	CartResponse addCartItem(UUID userId, AddCartItemRequest request);

	void updateCartItemQuantity(UUID userId, UUID cartItemId, UpdateCartItemQuantityRequest request);

	void removeCartItem(UUID userId, UUID cartItemId);
}
