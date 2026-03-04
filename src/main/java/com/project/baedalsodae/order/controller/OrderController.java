package com.project.baedalsodae.order.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;
import com.project.baedalsodae.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
	private final OrderService orderService;

	// TODO: 인증 도메인 완성 후 userId 교체
	@PostMapping
	public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
			@RequestHeader("X-User-Id") UUID userId,
			@RequestBody @Valid CreateOrderRequest request) {
		CreateOrderResponse response = orderService.createOrder(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(SuccessCode.ORDER_CREATED, response));
	}
}
