package com.project.baedalsodae.order.service;

import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;
import com.project.baedalsodae.order.dto.response.OrderListResponse;
import java.util.UUID;

public interface OrderService {

    CreateOrderResponse createOrder(UUID userId, CreateOrderRequest request);

    OrderListResponse getOrders(UUID userId, String role, OrderListRequest request);
}
