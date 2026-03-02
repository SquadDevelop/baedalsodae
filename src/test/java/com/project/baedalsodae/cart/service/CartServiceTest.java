package com.project.baedalsodae.cart.service;

import com.project.baedalsodae.cart.dto.request.AddCartItemRequest;
import com.project.baedalsodae.cart.dto.request.UpdateCartItemQuantityRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
	private CartItem cartItem1;

	@Mock
	private Store store1;

	@Mock
	private Store store2;

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

		given(store1.getId()).willReturn(storeId);
		Cart emptyCart = Cart.create(userId, store1);

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
		UUID menuItemId1 = UUID.randomUUID();
		UUID menuItemId2 = UUID.randomUUID();

		given(store1.getId()).willReturn(storeId);
		Cart cart = Cart.create(userId, store1);

		given(menuItem1.getId()).willReturn(menuItemId1);
		given(menuItem1.getName()).willReturn("후라이드 치킨");
		given(menuItem1.getPrice()).willReturn(18000);
		CartItem cartItem1 = CartItem.create(cart, menuItem1, 1);

		given(menuItem2.getId()).willReturn(menuItemId2);
		given(menuItem2.getName()).willReturn("짜장면");
		given(menuItem2.getPrice()).willReturn(8000);
		CartItem cartItem2 = CartItem.create(cart, menuItem2, 1);

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

	@Test
	@DisplayName("실패 - 장바구니 아이템 추가시 다른 가게 메뉴를 이미 담은 장바구니에 추가 시도")
	void addCartItem_fail_differentStore() {
		//given
		UUID userId = UUID.randomUUID();
		UUID storeId1 = UUID.randomUUID();
		UUID storeId2 = UUID.randomUUID();
		UUID menuItemId1 = UUID.randomUUID();
		UUID menuItemId2 = UUID.randomUUID();
		AddCartItemRequest addCartItemRequest = new AddCartItemRequest(
				storeId2,
				menuItemId2,
				1
		);

		given(storeRepository.findById(storeId2))
				.willReturn(Optional.of(store2));

		given(menuItemRepository.findById(menuItemId2))
				.willReturn(Optional.of(menuItem2));

		given(store1.getId()).willReturn(storeId1);
		Cart cart = Cart.create(userId, store1);
		CartItem cartItem1 = CartItem.create(cart, menuItem1, 1);
		cart.addItem(cartItem1);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.of(cart));

		//when
		Throwable throwable = catchThrowable(() -> {
			cartService.addCartItem(userId, addCartItemRequest);
		});
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_DIFFERENT_STORE);
	}

	@Test
	@DisplayName("성공 - 최초 추가, 장바구니 신규 생성 후 아이템 추가")
	void addCartItem_success_newCart() {
		//given
		UUID userId = UUID.randomUUID();
		UUID storeId1 = UUID.randomUUID();
		UUID menuItemId1 = UUID.randomUUID();
		AddCartItemRequest addCartItemRequest = new AddCartItemRequest(
				storeId1,
				menuItemId1,
				1
		);

		given(storeRepository.findById(storeId1))
				.willReturn(Optional.of(store1));

		given(menuItemRepository.findById(menuItemId1))
				.willReturn(Optional.of(menuItem1));

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.empty());

		given(cartRepository.save(any(Cart.class)))
				.willAnswer(inv -> inv.getArgument(0));

		given(menuItem1.getId()).willReturn(menuItemId1);

		//when
		CartResponse response = cartService.addCartItem(userId, addCartItemRequest);
		log.info("response = {}", response);

		//then
		assertThat(response).isNotNull();
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).menuItemId()).isEqualTo(menuItemId1);
		assertThat(response.items().get(0).quantity()).isEqualTo(addCartItemRequest.quantity());
	}

	@Test
	@DisplayName("성공 - 기존 장바구니에 아이템 추가")
	void addCartItem_success_existingCart_addNewMenuItem() {
		//given
		UUID userId = UUID.randomUUID();
		UUID storeId1 = UUID.randomUUID();
		UUID menuItemId1 = UUID.randomUUID();
		UUID menuItemId2 = UUID.randomUUID();
		AddCartItemRequest addCartItemRequest = new AddCartItemRequest(
				storeId1,
				menuItemId2,
				1
		);

		given(storeRepository.findById(storeId1))
				.willReturn(Optional.of(store1));

		given(menuItemRepository.findById(menuItemId2))
				.willReturn(Optional.of(menuItem2));

		given(menuItem1.getId()).willReturn(menuItemId1);
		given(menuItem1.getName()).willReturn("치킨");
		given(menuItem1.getPrice()).willReturn(18000);
		given(store1.getId()).willReturn(storeId1);
		Cart cart = Cart.create(userId, store1);
		CartItem cartItem1 = CartItem.create(cart, menuItem1, 1);
		cart.addItem(cartItem1);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.of(cart));

		given(cartRepository.save(any(Cart.class)))
				.willAnswer(inv -> inv.getArgument(0));

		given(menuItem2.getId()).willReturn(menuItemId2);
		given(menuItem2.getName()).willReturn("짜장면");
		given(menuItem2.getPrice()).willReturn(8000);

		//when
		CartResponse response = cartService.addCartItem(userId, addCartItemRequest);
		log.info("response = {}", response);

		//then
		assertThat(response).isNotNull();
		assertThat(response.items()).hasSize(2);
		assertThat(response.items().get(0).menuItemId()).isEqualTo(menuItemId1);
		assertThat(response.items().get(1).menuItemId()).isEqualTo(menuItemId2);
		assertThat(response.totalAmount()).isEqualTo(26000);
	}

	@Test
	@DisplayName("성공 - 이미 담긴 메뉴면 수량만 증가")
	void addCartItem_success_existingCart_increaseQuantity_whenSameMenuItem() {
		//given
		UUID userId = UUID.randomUUID();
		UUID storeId1 = UUID.randomUUID();
		UUID menuItemId1 = UUID.randomUUID();
		AddCartItemRequest addCartItemRequest = new AddCartItemRequest(
				storeId1,
				menuItemId1,
				1
		);

		given(storeRepository.findById(storeId1))
				.willReturn(Optional.of(store1));

		given(menuItemRepository.findById(menuItemId1))
				.willReturn(Optional.of(menuItem1));

		given(menuItem1.getId()).willReturn(menuItemId1);
		given(menuItem1.getName()).willReturn("치킨");
		given(menuItem1.getPrice()).willReturn(18000);
		given(store1.getId()).willReturn(storeId1);
		Cart cart = Cart.create(userId, store1);
		CartItem cartItem1 = CartItem.create(cart, menuItem1, 1);
		cart.addItem(cartItem1);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.of(cart));

		given(cartRepository.save(any(Cart.class)))
				.willAnswer(inv -> inv.getArgument(0));

		//when
		CartResponse response = cartService.addCartItem(userId, addCartItemRequest);
		log.info("response = {}", response);

		//then
		assertThat(response).isNotNull();
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).menuItemId()).isEqualTo(menuItemId1);
		assertThat(response.items().get(0).quantity()).isEqualTo(2);
		assertThat(response.totalQuantity()).isEqualTo(2);
		assertThat(response.totalAmount()).isEqualTo(36000);
	}

	@Test
	@DisplayName("실패 - 장바구니 아이템 수량 변경 시 수량이 0 이하")
	void updateCartItemQuantity_fail_invalidQuantity() {
		//given
		UUID userId = UUID.randomUUID();
		UUID cartItemId = UUID.randomUUID();
		UpdateCartItemQuantityRequest request = new UpdateCartItemQuantityRequest(0);

		//when
		Throwable throwable = catchThrowable(() -> {
			cartService.updateCartItemQuantity(userId, cartItemId, request);
		});
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_INVALID_QUANTITY);
	}

	@Test
	@DisplayName("실패 - 장바구니 아이템 수량 변경 시 장바구니가 존재하지 않음")
	void updateCartItemQuantity_fail_cartNotFound() {
		//given
		UUID userId = UUID.randomUUID();
		UUID cartItemId = UUID.randomUUID();
		UpdateCartItemQuantityRequest request = new UpdateCartItemQuantityRequest(2);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.empty());

		//when
		Throwable throwable = catchThrowable(() -> {
			cartService.updateCartItemQuantity(userId, cartItemId, request);
		});
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);

	}

	@Test
	@DisplayName("실패 - 장바구니 아이템 수량 변경 시 해당 아이템이 장바구니에 없음")
	void updateCartItemQuantity_fail_cartItemNotFound() {
		//given
		UUID userId = UUID.randomUUID();
		UUID cartItemId1 = UUID.randomUUID();
		UpdateCartItemQuantityRequest request = new UpdateCartItemQuantityRequest(2);

		Cart cart = Cart.create(userId, store1);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.of(cart));

		//when
		Throwable throwable = catchThrowable(() -> {
			cartService.updateCartItemQuantity(userId, cartItemId1, request);
		});
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_NOT_FOUND);

	}

	@Test
	@DisplayName("성공 - 장바구니 아이템 수량 정상 변경")
	void updateCartItemQuantity_success() {
		//given
		UUID userId = UUID.randomUUID();
		UUID cartItemId1 = UUID.randomUUID();
		UpdateCartItemQuantityRequest request = new UpdateCartItemQuantityRequest(2);

		Cart cart = Cart.create(userId, store1);

		given(cartItem1.getId()).willReturn(cartItemId1);
		cart.addItem(cartItem1);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.of(cart));

		//when
		cartService.updateCartItemQuantity(userId, cartItemId1, request);

		//then
		verify(cartItem1).changeQuantity(request.quantity());
	}

	@Test
	@DisplayName("실패 - 장바구니 아이템 삭제 시 장바구니가 존재하지 않음")
	void removeCartItem_fail_cartNotFound() {
		//given
		UUID userId = UUID.randomUUID();
		UUID cartItemId1 = UUID.randomUUID();

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.empty());

		//when
		Throwable throwable = catchThrowable(() -> {
			cartService.removeCartItem(userId, cartItemId1);
		});
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);

	}

	@Test
	@DisplayName("실패 - 장바구니 아이템 삭제 시 해당 아이템이 장바구니에 없음")
	void removeCartItem_fail_cartItemNotFound() {
		//given
		UUID userId = UUID.randomUUID();
		UUID cartItemId1 = UUID.randomUUID();

		Cart cart = Cart.create(userId, store1);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.of(cart));

		//when
		Throwable throwable = catchThrowable(() -> {
			cartService.removeCartItem(userId, cartItemId1);
		});
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_NOT_FOUND);

	}

	@Test
	@DisplayName("성공 - 아이템 정상 삭제")
	void removeCartItem_success() {
		//given
		UUID userId = UUID.randomUUID();
		UUID cartItemId1 = UUID.randomUUID();
		UpdateCartItemQuantityRequest request = new UpdateCartItemQuantityRequest(2);

		Cart cart = Cart.create(userId, store1);

		given(cartItem1.getId()).willReturn(cartItemId1);
		cart.addItem(cartItem1);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.of(cart));

		//when
		cartService.removeCartItem(userId, cartItemId1);

		//then
		assertThat(cart.getItems()).hasSize(0);
	}

	@Test
	@DisplayName("실패 - 장바구니 비우기 시 장바구니가 존재하지 않음")
	void clearCart_fail_cartNotFound() {
		//given
		UUID userId = UUID.randomUUID();
		UUID cartItemId1 = UUID.randomUUID();

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.empty());

		//when
		Throwable throwable = catchThrowable(() -> {
			cartService.clearCart(userId);
		});
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);

	}

	@Test
	@DisplayName("성공 - 장바구니 비우기 전체 아이템 삭제")
	void clearCart_success() {
		//given
		UUID userId = UUID.randomUUID();

		Cart cart = Cart.create(userId, store1);

		given(cartRepository.findByUserIdAndIsDeletedFalse(userId))
				.willReturn(Optional.of(cart));

		//when
		cartService.clearCart(userId);

		//then
		verify(cartRepository).delete(cart);
	}
}
