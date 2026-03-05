package com.project.baedalsodae.order.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;
import com.project.baedalsodae.order.dto.response.OrderDetailResponse;
import com.project.baedalsodae.order.dto.response.OrderListResponse;
import com.project.baedalsodae.order.dto.response.OrderStatusResponse;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.user.entity.UserRole;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    // TODO: 인증 도메인 완성 후 userId 교체
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid CreateOrderRequest request) {
        CreateOrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.ORDER_CREATED, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<OrderListResponse>> getOrders(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @ModelAttribute OrderListRequest request) {
        OrderListResponse response = orderService.getOrders(userId, role, request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_LIST, response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") UserRole userRole,
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @PathVariable("orderId") UUID orderId) {

        OrderDetailResponse response =
                orderService.getOrderDetail(userId, userRole, storeId, orderId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.ORDER_DETAIL, response)
        );
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatus(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") UserRole userRole,
            @RequestParam(name="storeId", required = false) UUID storeId,
            @PathVariable("orderId") UUID orderId) {

        OrderStatusResponse response =
                orderService.getOrderStatus(userId, userRole, storeId, orderId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_STATUS, response));
    }

}
