package com.project.baedalsodae.cart.service.impl;

import com.project.baedalsodae.cart.dto.request.AddCartItemRequest;
import com.project.baedalsodae.cart.dto.request.UpdateCartItemQuantityRequest;
import com.project.baedalsodae.cart.dto.response.CartResponse;
import com.project.baedalsodae.cart.entity.Cart;
import com.project.baedalsodae.cart.entity.CartItem;
import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.cart.service.CartService;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.repository.MenuItemRepository;
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
	private final MenuItemRepository menuItemRepository;

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

		final UUID storeId = request.storeId();
		final UUID menuItemId = request.menuItemId();

		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

		MenuItem menuItem = menuItemRepository.findById(menuItemId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));

		Optional<Cart> optionalCart = cartRepository.findByUserIdAndIsDeletedFalse(userId);

		optionalCart.ifPresent(existingCart -> {
			if (!existingCart.isSameStore(request.storeId()))
				throw new BusinessException(ErrorCode.CART_DIFFERENT_STORE);
		});

		Cart cart = optionalCart.orElseGet(() -> Cart.create(userId, store));
		cart.addItem(CartItem.create(cart, menuItem, request.quantity()));

		final Cart savedCart = cartRepository.save(cart);

		return CartResponse.from(savedCart);
	}

	@Override
	@Transactional
	public void updateCartItemQuantity(UUID userId, UUID cartItemId, UpdateCartItemQuantityRequest request) {
		if (!request.isValidQuantity())
			throw new BusinessException(ErrorCode.CART_INVALID_QUANTITY);

		Cart cart = cartRepository.findByUserIdAndIsDeletedFalse(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

		cart.findCartItemById(cartItemId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));


	}
}
