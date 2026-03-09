package com.project.baedalsodae.cart.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.cart.dto.request.AddCartItemRequest;
import com.project.baedalsodae.cart.dto.request.UpdateCartItemQuantityRequest;
import com.project.baedalsodae.cart.dto.response.CartResponse;
import com.project.baedalsodae.cart.service.CartService;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CartResponse response = cartService.getCart(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CART_FOUND, response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addCartItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid AddCartItemRequest request) {
        CartResponse response = cartService.addCartItem(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.CART_ITEM_ADDED, response));
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> updateCartItemQuantity(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("cartItemId") UUID cartItemId,
            @RequestBody @Valid UpdateCartItemQuantityRequest request) {
        cartService.updateCartItemQuantity(userDetails.getUserId(), cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CART_ITEM_QUANTITY_UPDATED, null));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("cartItemId") UUID cartItemId) {
        cartService.removeCartItem(userDetails.getUserId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CART_ITEM_REMOVED, null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        cartService.clearCart(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CART_CLEARED, null));
    }
}
