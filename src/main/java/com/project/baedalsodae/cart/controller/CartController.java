package com.project.baedalsodae.cart.controller;

import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.cart.dto.request.AddCartItemRequest;
import com.project.baedalsodae.cart.dto.request.UpdateCartItemQuantityRequest;
import com.project.baedalsodae.cart.dto.response.CartResponse;
import com.project.baedalsodae.cart.service.CartService;
import com.project.baedalsodae.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	// TODO: 인증 도메인 완성 후 userId 교체
	@GetMapping
	public ResponseEntity<ApiResponse<CartResponse>> getCart(@RequestHeader("X-User-Id") UUID userId) {
		CartResponse response = cartService.getCart(userId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.CART_FOUND, response));
	}

	@PostMapping("/items")
	public ResponseEntity<ApiResponse<CartResponse>> addCartItem(
			@RequestHeader("X-User-Id") UUID userId,
			@RequestBody @Valid AddCartItemRequest request) {
		CartResponse response = cartService.addCartItem(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(SuccessCode.CART_ITEM_ADDED, response));
	}

	@PatchMapping("/items/{cartItemId}")
	public ResponseEntity<ApiResponse<Void>> updateCartItemQuantity(
			@RequestHeader("X-User-Id") UUID userId,
			@PathVariable("cartItemId") UUID cartItemId,
			@RequestBody @Valid UpdateCartItemQuantityRequest request) {
		cartService.updateCartItemQuantity(userId, cartItemId, request);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.CART_ITEM_QUANTITY_UPDATED, null));
	}

	@DeleteMapping("/items/{cartItemId}")
	public ResponseEntity<ApiResponse<Void>> removeCartItem(
			@RequestHeader("X-User-Id") UUID userId,
			@PathVariable("cartItemId") UUID cartItemId) {
		cartService.removeCartItem(userId, cartItemId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.CART_ITEM_REMOVED, null));
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> clearCart(@RequestHeader("X-User-Id") UUID userId) {
		cartService.clearCart(userId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.CART_CLEARED, null));
	}
}
