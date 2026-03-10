package com.project.baedalsodae.order.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.OrderActionStatusResponse;
import com.project.baedalsodae.order.dto.response.OrderDetailResponse;
import com.project.baedalsodae.order.dto.response.OrderListResponse;
import com.project.baedalsodae.order.dto.response.OrderStatusResponse;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<OrderListResponse>> getOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @ModelAttribute OrderListRequest request) {
        UserRole userRole = userDetails.getUserRole();
        OrderListResponse response =
                orderService.getOrders(userDetails.getUserId(), userRole.getRole(), request);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_LIST, response));
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetails(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("orderId") UUID orderId) {
        OrderDetailResponse response =
                orderService.getOrderDetail(
                        userDetails.getUserId(), userDetails.getUserRole(), null, orderId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_DETAIL, response));
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @GetMapping("/orders/{orderId}/status-history")
    public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatusHistories(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("orderId") UUID orderId) {
        OrderStatusResponse response =
                orderService.getOrderStatus(
                        userDetails.getUserId(), userDetails.getUserRole(), null, orderId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_STATUS, response));
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @PostMapping("/orders/{orderId}/cancel-request")
    public ResponseEntity<ApiResponse<OrderActionStatusResponse>> cancelRequestOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "reason", required = false) String reason,
            @PathVariable("orderId") UUID orderId) {

        OrderActionStatusResponse response =
                orderService.cancelRequestOrder(
                        userDetails.getUserId(), userDetails.getUserRole(), null, orderId, reason);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ORDER_CANCEL_REQUESTED, response));
    }
}
