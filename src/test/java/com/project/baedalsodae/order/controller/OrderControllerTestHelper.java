package com.project.baedalsodae.order.controller;

import com.project.baedalsodae.common.WebMvcTestBase;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;

import java.util.UUID;

public class OrderControllerTestHelper extends WebMvcTestBase {

	final String ORDER_BASE_URL = "/orders";
	final String X_USER_ID_HEADER = "X-User-Id";

	CreateOrderRequest ORDER_FIXTURE =
			CreateOrderRequest.builder()
					.cartId(UUID.randomUUID())
					.addressId(UUID.randomUUID())
					.build();
}
