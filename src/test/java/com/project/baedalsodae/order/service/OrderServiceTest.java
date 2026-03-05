package com.project.baedalsodae.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.project.baedalsodae.cart.entity.Cart;
import com.project.baedalsodae.cart.entity.CartItem;
import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.OrderStatusHistory;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.order.publisher.OrderEventPublisher;
import com.project.baedalsodae.order.repository.OrderRepository;
import com.project.baedalsodae.order.repository.OrderStatusHistoryRepository;
import com.project.baedalsodae.order.service.impl.OrderServiceImpl;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks private OrderServiceImpl orderService;

    @Mock private OrderRepository orderRepository;

    @Mock private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock private CartRepository cartRepository;

    @Mock private StoreRepository storeRepository;

    @Mock private Cart cart;

    @Mock private Store store;

    @Mock private CartItem cartItem1;

    @Mock private CartItem cartItem2;

    @Mock private MenuItem menuItem1;

    @Mock private MenuItem menuItem2;

    @Mock private OrderEventPublisher eventPublisher;

    @Test
    @DisplayName("실패 - 주문 생성 시 장바구니가 존재하지 않음")
    void createOrder_fail_cartNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        String storeRequestMessage = "리뷰이벤트 잽닝이 막국수 주시면 감사하겠습니다.";

        CreateOrderRequest request =
                CreateOrderRequest.builder()
                        .cartId(cartId)
                        .addressId(addressId)
                        .storeRequestMessage(storeRequestMessage)
                        .build();

        given(cartRepository.findCartWithItemsByIdAndUserId(cartId, userId))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> orderService.createOrder(userId, request));
        log.info("throwable = " + throwable);

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 주문 생성 시 장바구니 아이템이 비어있음")
    void createOrder_fail_cartItemEmpty() {
        // given
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        String storeRequestMessage = "리뷰이벤트 잽닝이 막국수 주시면 감사하겠습니다.";

        CreateOrderRequest request =
                CreateOrderRequest.builder()
                        .cartId(cartId)
                        .addressId(addressId)
                        .storeRequestMessage(storeRequestMessage)
                        .build();

        given(cartRepository.findCartWithItemsByIdAndUserId(cartId, userId))
                .willReturn(Optional.of(cart));

        given(cart.hasNoItems()).willReturn(true);

        // when
        Throwable throwable = catchThrowable(() -> orderService.createOrder(userId, request));
        log.info("throwable = " + throwable);

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_EMPTY);
    }

    @Test
    @DisplayName("실패 - 주문 생성 시 존재하지 않는 가게")
    void createOrder_fail_storeNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        String storeRequestMessage = "리뷰이벤트 잽닝이 막국수 주시면 감사하겠습니다.";

        CreateOrderRequest request =
                CreateOrderRequest.builder()
                        .cartId(cartId)
                        .addressId(addressId)
                        .storeRequestMessage(storeRequestMessage)
                        .build();

        given(cartRepository.findCartWithItemsByIdAndUserId(cartId, userId))
                .willReturn(Optional.of(cart));

        given(cart.getStore()).willReturn(store);
        given(store.getId()).willReturn(storeId);

        given(cart.hasNoItems()).willReturn(false);

        given(storeRepository.findById(storeId)).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> orderService.createOrder(userId, request));
        log.info("throwable = " + throwable);

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 주문 생성 시 총 메뉴 금액이 0 이하")
    void createOrder_fail_invalidTotalAmount() {
        // given
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        String storeRequestMessage = "리뷰이벤트 잽닝이 막국수 주시면 감사하겠습니다.";

        CreateOrderRequest request =
                CreateOrderRequest.builder()
                        .cartId(cartId)
                        .addressId(addressId)
                        .storeRequestMessage(storeRequestMessage)
                        .build();

        given(cartRepository.findCartWithItemsByIdAndUserId(cartId, userId))
                .willReturn(Optional.of(cart));

        given(cart.getStore()).willReturn(store);
        given(store.getId()).willReturn(storeId);

        given(cart.hasNoItems()).willReturn(false);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        given(cart.getTotalAmount()).willReturn(0);

        // when
        Throwable throwable = catchThrowable(() -> orderService.createOrder(userId, request));
        log.info("throwable = " + throwable);

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_INVALID_TOTAL_AMOUNT);
    }

    // TODO 주소 도메인 완성 후 만들어야함
    @Test
    @DisplayName("실패 - 주문 생성 시 존재하지 않는 주소")
    void createOrder_fail_addressNotFound() {}

    @Test
    @DisplayName("성공 - 정상적인 주문 생성 (단일 아이템)")
    void createOrder_success_singleItem() {
        // given
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID menuItemId1 = UUID.randomUUID();

        String storeRequestMessage = "리뷰이벤트 잽닝이 치킨 무 추가로 주시면 감사하겠습니다.";

        CreateOrderRequest request =
                CreateOrderRequest.builder()
                        .cartId(cartId)
                        .addressId(addressId)
                        .storeRequestMessage(storeRequestMessage)
                        .build();

        given(cartRepository.findCartWithItemsByIdAndUserId(cartId, userId))
                .willReturn(Optional.of(cart));

        given(cart.getStore()).willReturn(store);
        given(store.getId()).willReturn(storeId);

        given(cart.hasNoItems()).willReturn(false);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        given(cart.getTotalAmount()).willReturn(18000);

        given(cartItem1.getMenuItem()).willReturn(menuItem1);
        given(menuItem1.getId()).willReturn(menuItemId1);
        given(menuItem1.getName()).willReturn("치킨");
        given(menuItem1.getPrice()).willReturn(18000);
        given(cartItem1.getQuantity()).willReturn(1);

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem1);

        given(cart.getItems()).willReturn(cartItems);

        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        given(orderStatusHistoryRepository.save(any(OrderStatusHistory.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        CreateOrderResponse response = orderService.createOrder(userId, request);
        log.info("response = {}", response);

        // then
        then(orderRepository).should().save(any(Order.class));
        then(orderStatusHistoryRepository).should().save(any(OrderStatusHistory.class));
        then(eventPublisher).should().publishOrderCreated(any(Order.class));
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("성공 - 정상적인 주문 생성 (복수 아이템)")
    void createOrder_success_multipleItems() {
        // given
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID menuItemId1 = UUID.randomUUID();
        UUID menuItemId2 = UUID.randomUUID();

        String storeRequestMessage = "리뷰이벤트 잽닝이 치킨 무 추가로 주시면 감사하겠습니다.";

        CreateOrderRequest request =
                CreateOrderRequest.builder()
                        .cartId(cartId)
                        .addressId(addressId)
                        .storeRequestMessage(storeRequestMessage)
                        .build();

        given(cartRepository.findCartWithItemsByIdAndUserId(cartId, userId))
                .willReturn(Optional.of(cart));

        given(cart.getStore()).willReturn(store);
        given(store.getId()).willReturn(storeId);

        given(cart.hasNoItems()).willReturn(false);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        given(cart.getTotalAmount()).willReturn(26000);

        given(cartItem1.getMenuItem()).willReturn(menuItem1);
        given(menuItem1.getId()).willReturn(menuItemId1);
        given(menuItem1.getName()).willReturn("치킨");
        given(menuItem1.getPrice()).willReturn(18000);
        given(cartItem1.getQuantity()).willReturn(1);

        given(cartItem2.getMenuItem()).willReturn(menuItem2);
        given(menuItem2.getId()).willReturn(menuItemId2);
        given(menuItem2.getName()).willReturn("짜장면");
        given(menuItem2.getPrice()).willReturn(8000);
        given(cartItem2.getQuantity()).willReturn(1);

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem1);
        cartItems.add(cartItem2);

        given(cart.getItems()).willReturn(cartItems);

        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        given(orderStatusHistoryRepository.save(any(OrderStatusHistory.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        CreateOrderResponse response = orderService.createOrder(userId, request);
        log.info("response = {}", response);

        // then
        then(orderRepository).should().save(any(Order.class));
        then(orderRepository)
                .should()
                .save(
                        argThat(
                                order ->
                                        order.getOrderItems().size() == 2
                                                && order.getTotalAmount() == 26000));
        then(orderStatusHistoryRepository).should().save(any(OrderStatusHistory.class));
        then(eventPublisher).should().publishOrderCreated(any(Order.class));
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
    }

    // ======================== getOrders ========================

    @Test
    @DisplayName("실패 - 잘못된 날짜 범위 (endDate < startDate)")
    void getOrders_fail_invalidDateRange() {
        // given
        UUID userId = UUID.randomUUID();
        OrderListRequest request =
                OrderListRequest.builder()
                        .startDate(LocalDate.of(2026, 3, 5))
                        .endDate(LocalDate.of(2026, 3, 1))
                        .build();

        // when
        Throwable throwable =
                catchThrowable(() -> orderService.getOrders(userId, "CUSTOMER", request));
        log.info("throwable = " + throwable);

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_INVALID_DATE_RANGE);
    }
}
