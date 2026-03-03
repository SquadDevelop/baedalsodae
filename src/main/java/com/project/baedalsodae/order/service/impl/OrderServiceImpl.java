package com.project.baedalsodae.order.service.impl;

import com.project.baedalsodae.cart.entity.Cart;
import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final CartRepository cartRepository;
	private final StoreRepository storeRepository;

	@Override
	@Transactional
	public CreateOrderResponse createOrder(UUID userId, CreateOrderRequest request) {
		final UUID cartId = request.cartId();

		Cart cart = cartRepository.findCartWithItemsByIdAndUserId(cartId, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

		if(cart.hasNoItems())
			throw new BusinessException(ErrorCode.CART_ITEM_EMPTY);

		final UUID storeId = cart.getStore().getId();
		Store store = storeRepository.findById(storeId)
				.orElseThrow(()-> new BusinessException(ErrorCode.STORE_NOT_FOUND));

		if(cart.isInvalidTotalAmount())
			throw new BusinessException(ErrorCode.ORDER_INVALID_TOTAL_AMOUNT);



		return null;
	}

}
