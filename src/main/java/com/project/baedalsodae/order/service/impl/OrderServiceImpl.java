package com.project.baedalsodae.order.service.impl;

import com.project.baedalsodae.cart.entity.Cart;
import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;
import com.project.baedalsodae.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final CartRepository cartRepository;

	@Override
	@Transactional
	public CreateOrderResponse createOrder(UUID userId, CreateOrderRequest request) {
		final UUID cartId = request.cartId();

		Cart cart = cartRepository.findCartWithItemsByUserIdAndCartId(userId, cartId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));


		return null;
	}

}
