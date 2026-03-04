package com.project.baedalsodae.order.service;

import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;

import java.util.UUID;

public interface OrderService {

	CreateOrderResponse createOrder(UUID userId, CreateOrderRequest request);

}
