package com.project.baedalsodae.cart.service;

import com.project.baedalsodae.cart.dto.request.AddCartItemRequest;
import com.project.baedalsodae.cart.dto.response.CartResponse;
import com.project.baedalsodae.cart.entity.Cart;
import com.project.baedalsodae.cart.entity.CartItem;
import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.cart.service.impl.CartServiceImpl;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.repository.MenuItemRepository;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

	@Mock
	private CartRepository cartRepository;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private MenuItemRepository menuItemRepository;

	@InjectMocks
	private CartServiceImpl cartService;

	@Mock
	private MenuItem menuItem1;

	@Mock
	private MenuItem menuItem2;

	@Mock
	private Store store1;

	@Test
	@DisplayName("실패 - 장바구니가 존재하지 않음")
	void getCart_fail_cartNotFound() {
		//given
		UUID userId = UUID.randomUUID();
		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.empty());

		//when
		Throwable throwable = catchThrowable(() -> cartService.getCart(userId));
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);
	}

	@Test
	@DisplayName("성공 - 빈 장바구니 조회")
	void getCart_success_EmptyCart() {
		//given
		UUID userId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		Cart emptyCart = Cart.create(userId, storeId);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.of(emptyCart));

		//when
		CartResponse response = cartService.getCart(userId);
		log.info("response = {}", response);

		//then
		assertThat(response.items()).isEmpty();
		assertThat(response.totalAmount()).isZero();
	}

	@Test
	@DisplayName("성공 - 장바구니 정상 조회")
	void getCart_success() {
		//given
		UUID userId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();

		Cart cart = Cart.create(userId, storeId);

		given(menuItem1.getId()).willReturn(UUID.randomUUID());
		given(menuItem1.getName()).willReturn("후라이드 치킨");
		given(menuItem1.getPrice()).willReturn(18000);
		CartItem cartItem1 = CartItem.create(cart, menuItem1);

		given(menuItem2.getId()).willReturn(UUID.randomUUID());
		given(menuItem2.getName()).willReturn("짜장면");
		given(menuItem2.getPrice()).willReturn(8000);
		CartItem cartItem2 = CartItem.create(cart, menuItem2);

		cart.addItem(cartItem1);
		cart.addItem(cartItem2);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.of(cart));

		//when
		CartResponse response = cartService.getCart(userId);
		log.info("response = {}", response);

		//then
		assertThat(response.items()).hasSize(2);
		assertThat(response.totalAmount()).isEqualTo(26000);
	}

	@Test
	@DisplayName("실패 - 장바구니 아이템 추가 시 수량이 0이하")
	void addCartItem_fail_invalidQuantity() {
		//given
		UUID userId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		UUID menuItemId = UUID.randomUUID();
		AddCartItemRequest addCartItemRequest = new AddCartItemRequest(
				storeId,
				menuItemId,
				0
		);

		//when
		Throwable throwable = catchThrowable(() -> {
			cartService.addCartItem(userId, addCartItemRequest);
		});
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_INVALID_QUANTITY);
	}

	@Test
	@DisplayName("실패 - 장바구니 아이템 추가 시 가게가 존재하지 않음")
	void addCartItem_fail_storeNotFound() {
		//given
		UUID userId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		UUID menuItemId = UUID.randomUUID();
		AddCartItemRequest addCartItemRequest = new AddCartItemRequest(
				storeId,
				menuItemId,
				1
		);

		given(storeRepository.findById(storeId))
				.willReturn(Optional.empty());

		//when
		Throwable throwable = catchThrowable(() -> {
			cartService.addCartItem(userId, addCartItemRequest);
		});
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_NOT_FOUND);
	}

	@Test
	@DisplayName("실패 - 장바구니 아이템 추가 시 메뉴가 존재하지 않음")
	void addCartItem_fail_menuNotFound() {
		//given
		UUID userId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		UUID menuItemId = UUID.randomUUID();
		AddCartItemRequest addCartItemRequest = new AddCartItemRequest(
				storeId,
				menuItemId,
				1
		);

		given(storeRepository.findById(storeId))
				.willReturn(Optional.of(store1));

		given(menuItemRepository.findById(menuItemId))
				.willReturn(Optional.empty());

		//when
		Throwable throwable = catchThrowable(() -> {
			cartService.addCartItem(userId, addCartItemRequest);
		});
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_ITEM_NOT_FOUND);
	}
}
