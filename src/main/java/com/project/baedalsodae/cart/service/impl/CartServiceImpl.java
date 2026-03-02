package com.project.baedalsodae.cart.service.impl;

import com.project.baedalsodae.cart.dto.request.AddCartItemRequest;
import com.project.baedalsodae.cart.dto.response.CartResponse;
import com.project.baedalsodae.cart.entity.Cart;
import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.cart.service.CartService;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final StoreRepository storeRepository;

	@Override
	@Transactional(readOnly = true)
	public CartResponse getCart(UUID userId) {
		Cart cart = cartRepository.findByUserIdAndIsDeletedFalse(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

		return CartResponse.from(cart);
	}


	@Override
	@Transactional
	public CartResponse addCartItem(UUID userId, AddCartItemRequest request) {
		if (!request.isValidQuantity())
			throw new BusinessException(ErrorCode.CART_INVALID_QUANTITY);

		Store store = storeRepository.findById(request.storeId())
				.orElseThrow(()-> new BusinessException(ErrorCode.STORE_NOT_FOUND));

		return null;
	}
}
