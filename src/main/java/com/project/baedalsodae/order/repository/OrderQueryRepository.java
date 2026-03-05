package com.project.baedalsodae.order.repository;

import com.project.baedalsodae.order.dto.query.OrderListQuery;
import com.project.baedalsodae.order.dto.response.OrderSummaryResponse;

import java.util.List;

public interface OrderQueryRepository {

    List<OrderSummaryResponse> findOrdersByCustomer(OrderListQuery query);

    List<OrderSummaryResponse> findOrdersByStore(OrderListQuery query);
}
