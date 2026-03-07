package com.project.baedalsodae.order.service;

import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.*;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.UUID;

public interface OrderService {

    CreateOrderResponse createOrder(UUID userId, CreateOrderRequest request);

    OrderListResponse getOrders(UUID userId, String role, OrderListRequest request);

    OrderDetailResponse getOrderDetail(UUID userId, UserRole userRole, UUID storeId, UUID orderId);

    OrderStatusResponse getOrderStatus(UUID userId, UserRole userRole, UUID storeId, UUID orderId);

    OrderActionStatusResponse requestOrder(UUID userId, UUID orderId);

    OrderActionStatusResponse acceptOrder(UUID userId, UserRole userRole, UUID storeId, UUID orderId);

    OrderActionStatusResponse rejectOrder(UUID userId, UserRole userRole, UUID storeId, UUID orderId, String reason);

    OrderActionStatusResponse completeCookingOrder(UUID userId, UserRole userRole, UUID storeId, UUID orderId);

    OrderActionStatusResponse startDeliveryOrder(UUID userId, UserRole userRole, UUID storeId, UUID orderId);

    OrderActionStatusResponse completeDeliveryOrder(UUID userId, UserRole userRole, UUID storeId, UUID orderId);

    OrderActionStatusResponse cancelRequestOrder(UUID userId, UserRole userRole, UUID storeId, UUID orderId, String reason);

    OrderActionStatusResponse completeCancelOrder(UUID userId, UUID orderId);
}
