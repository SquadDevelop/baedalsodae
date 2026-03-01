package com.project.baedalsodae.cart.service;

import com.project.baedalsodae.cart.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {
	CartResponse getCart(UUID userId);
}
