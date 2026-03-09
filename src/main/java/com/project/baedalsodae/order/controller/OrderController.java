package com.project.baedalsodae.order.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.*;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.review.dto.request.ReviewRequest;
import com.project.baedalsodae.review.dto.response.ReviewResponse;
import com.project.baedalsodae.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid CreateOrderRequest request) {
        CreateOrderResponse response = orderService.createOrder(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.ORDER_CREATED, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<OrderListResponse>> getOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @ModelAttribute OrderListRequest request) {
        OrderListResponse response =
                orderService.getOrders(
                        userDetails.getUserId(), userDetails.getUserRole().name(), request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_LIST, response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @PathVariable("orderId") UUID orderId) {

        OrderDetailResponse response =
                orderService.getOrderDetail(
                        userDetails.getUserId(), userDetails.getUserRole(), storeId, orderId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_DETAIL, response));
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @PathVariable("orderId") UUID orderId) {

        OrderStatusResponse response =
                orderService.getOrderStatus(
                        userDetails.getUserId(), userDetails.getUserRole(), storeId, orderId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_STATUS, response));
    }

    @PostMapping("/{orderId}/request")
    public ResponseEntity<ApiResponse<OrderActionStatusResponse>> requestOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("orderId") UUID orderId) {

        OrderActionStatusResponse response =
                orderService.requestOrder(userDetails.getUserId(), orderId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_REQUESTED, response));
    }

    @PostMapping("/{orderId}/accept")
    public ResponseEntity<ApiResponse<OrderActionStatusResponse>> acceptOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @PathVariable("orderId") UUID orderId) {

        OrderActionStatusResponse response =
                orderService.acceptOrder(
                        userDetails.getUserId(), userDetails.getUserRole(), storeId, orderId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_ACCEPTED, response));
    }

    @PostMapping("/{orderId}/reject")
    public ResponseEntity<ApiResponse<OrderActionStatusResponse>> rejectOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @RequestParam(name = "reason", required = false) String reason,
            @PathVariable("orderId") UUID orderId) {

        OrderActionStatusResponse response =
                orderService.rejectOrder(
                        userDetails.getUserId(),
                        userDetails.getUserRole(),
                        storeId,
                        orderId,
                        reason);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_REJECTED, response));
    }

    @PostMapping("/{orderId}/cooked")
    public ResponseEntity<ApiResponse<OrderActionStatusResponse>> completeCookingOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @PathVariable("orderId") UUID orderId) {

        OrderActionStatusResponse response =
                orderService.completeCookingOrder(
                        userDetails.getUserId(), userDetails.getUserRole(), storeId, orderId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.ORDER_COOKING_COMPLETED, response));
    }

    @PostMapping("/{orderId}/delivering")
    public ResponseEntity<ApiResponse<OrderActionStatusResponse>> startDeliveryOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @PathVariable("orderId") UUID orderId) {

        OrderActionStatusResponse response =
                orderService.startDeliveryOrder(
                        userDetails.getUserId(), userDetails.getUserRole(), storeId, orderId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_DELIVERING, response));
    }

    @PostMapping("/{orderId}/delivered")
    public ResponseEntity<ApiResponse<OrderActionStatusResponse>> completeDeliveryOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @PathVariable("orderId") UUID orderId) {

        OrderActionStatusResponse response =
                orderService.completeDeliveryOrder(
                        userDetails.getUserId(), userDetails.getUserRole(), storeId, orderId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_DELIVERED, response));
    }

    @PostMapping("/{orderId}/cancel-request")
    public ResponseEntity<ApiResponse<OrderActionStatusResponse>> cancelRequestOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @RequestParam(name = "reason", required = false) String reason,
            @PathVariable("orderId") UUID orderId) {

        OrderActionStatusResponse response =
                orderService.cancelRequestOrder(
                        userDetails.getUserId(),
                        userDetails.getUserRole(),
                        storeId,
                        orderId,
                        reason);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_CANCEL_REQUESTED, response));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderActionStatusResponse>> completeCancelOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("orderId") UUID orderId) {

        OrderActionStatusResponse response =
                orderService.completeCancelOrder(userDetails.getUserId(), orderId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_CANCELED, response));
    }

    @PostMapping("/{orderId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId,
            @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.REVIEW_CREATED,
                        reviewService.createReview(userDetails.getUserId(), orderId, request)));
    }
}
