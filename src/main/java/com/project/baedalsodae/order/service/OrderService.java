package com.project.baedalsodae.order.service;

import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;
import com.project.baedalsodae.order.dto.response.OrderDetailResponse;
import com.project.baedalsodae.order.dto.response.OrderListResponse;
import com.project.baedalsodae.user.entity.UserRole;

import java.util.UUID;

public interface OrderService {

    CreateOrderResponse createOrder(UUID userId, CreateOrderRequest request);

    OrderListResponse getOrders(UUID userId, String role, OrderListRequest request);

    OrderDetailResponse getOrderDetail(UUID userId, UserRole userRole, UUID orderId);
}
