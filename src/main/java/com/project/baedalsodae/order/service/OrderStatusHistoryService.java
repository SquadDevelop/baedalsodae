package com.project.baedalsodae.order.service;

import com.project.baedalsodae.order.dto.response.*;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import java.util.UUID;

public interface OrderStatusHistoryService {
    void createForCustomerOrderStatusHistory(UUID userId, Order savedOrder);

    void createForCustomerOrderStatusHistory(UUID userId, OrderStatus fromStatus, Order savedOrder);

    void createForOwnerOrderStatusHistory(
            UUID userId, OrderStatus fromStatus, Order savedOrder, String reason);

    void createForSystemOrderStatusHistory(OrderStatus fromStatus, Order savedOrder);

    void createForAdminOrderStatusHistory(UUID adminId, OrderStatus fromStatus, Order savedOrder, String reason);

}
