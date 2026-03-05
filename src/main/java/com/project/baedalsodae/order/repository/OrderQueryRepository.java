package com.project.baedalsodae.order.repository;

import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.OrderListResponse;
import java.util.UUID;

public interface OrderQueryRepository {

    OrderListResponse findOrdersByCustomer(UUID userId, OrderListRequest request);

    OrderListResponse findOrdersByStore(UUID storeId, OrderListRequest request);
}
