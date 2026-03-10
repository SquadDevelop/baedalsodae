package com.project.baedalsodae.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import com.project.baedalsodae.allowedRegion.service.AllowedRegionService;
import com.project.baedalsodae.cart.entity.Cart;
import com.project.baedalsodae.cart.entity.CartItem;
import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.publisher.EventPublisher;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.order.dto.query.OrderListQuery;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.*;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.OrderStatusHistory;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.order.repository.OrderQueryRepository;
import com.project.baedalsodae.order.repository.OrderRepository;
import com.project.baedalsodae.order.repository.OrderStatusHistoryRepository;
import com.project.baedalsodae.order.service.impl.OrderServiceImpl;
import com.project.baedalsodae.order.service.impl.OrderStatusHistoryServiceImpl;
import com.project.baedalsodae.payment.entity.Payment;
import com.project.baedalsodae.payment.entity.PaymentStatus;
import com.project.baedalsodae.payment.repository.PaymentRepository;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.user.entity.UserAddress;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.service.UserAddressService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
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

    @Mock private OrderStatusHistoryServiceImpl orderStatusHistoryService;

    @Mock private OrderRepository orderRepository;

    @Mock private AllowedRegionService allowedRegionService;

    @Mock private UserAddressService userAddressService;

    @Mock private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock private CartRepository cartRepository;

    @Mock private StoreRepository storeRepository;

    @Mock private PaymentRepository paymentRepository;

    @Mock private Cart cart;

    @Mock private Store store;

    @Mock private CartItem cartItem1;

    @Mock private CartItem cartItem2;

    @Mock private MenuItem menuItem1;

    @Mock private MenuItem menuItem2;

    @Mock private EventPublisher orderEventPublisher;

    @Mock private OrderQueryRepository orderQueryRepository;

    @Mock private Order order;

    @Mock private Payment payment;

    @Mock private OrderStatusHistory orderStatusHistory;

    @Mock private Address address;

    @Mock private UserAddress userAddress;

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

        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.empty());

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

        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.of(store));

        String sigunguCode = "ABC";
        given(store.getAddress()).willReturn(address);
        given(address.getSigunguCode()).willReturn(sigunguCode);
        given(allowedRegionService.isAllowedByCode(store.getAddress().getSigunguCode()))
                .willReturn(true);

        given(cart.getTotalAmount()).willReturn(BigDecimal.ZERO);

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

        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.of(store));

        String sigunguCode = "ABC";
        given(store.getAddress()).willReturn(address);
        given(address.getSigunguCode()).willReturn(sigunguCode);
        given(allowedRegionService.isAllowedByCode(store.getAddress().getSigunguCode()))
                .willReturn(true);

        given(cart.getTotalAmount()).willReturn(BigDecimal.valueOf(18000));

        given(cartItem1.getMenuItem()).willReturn(menuItem1);
        given(menuItem1.getId()).willReturn(menuItemId1);
        given(menuItem1.getName()).willReturn("치킨");
        given(menuItem1.getPrice()).willReturn(BigDecimal.valueOf(18000));
        given(cartItem1.getQuantity()).willReturn(1);

        given(userAddress.getAddress()).willReturn(address);
        given(userAddressService.getMainUserAddress(userId)).willReturn(userAddress);

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem1);

        given(cart.getItems()).willReturn(cartItems);

        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        CreateOrderResponse response = orderService.createOrder(userId, request);
        log.info("response = {}", response);

        // then
        then(orderRepository).should().save(any(Order.class));
        then(orderStatusHistoryService)
                .should()
                .createForCustomerOrderStatusHistory(eq(userId), any(Order.class));
        then(orderEventPublisher)
                .should()
                .publishOrderEvent(any(Order.class), eq(EventType.ORDER_CREATED));
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

        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.of(store));

        String sigunguCode = "ABC";
        given(store.getAddress()).willReturn(address);
        given(address.getSigunguCode()).willReturn(sigunguCode);
        given(allowedRegionService.isAllowedByCode(store.getAddress().getSigunguCode()))
                .willReturn(true);

        given(cart.getTotalAmount()).willReturn(BigDecimal.valueOf(26000));

        given(userAddress.getAddress()).willReturn(address);
        given(userAddressService.getMainUserAddress(userId)).willReturn(userAddress);

        given(cartItem1.getMenuItem()).willReturn(menuItem1);
        given(menuItem1.getId()).willReturn(menuItemId1);
        given(menuItem1.getName()).willReturn("치킨");
        given(menuItem1.getPrice()).willReturn(BigDecimal.valueOf(18000));
        given(cartItem1.getQuantity()).willReturn(1);

        given(cartItem2.getMenuItem()).willReturn(menuItem2);
        given(menuItem2.getId()).willReturn(menuItemId2);
        given(menuItem2.getName()).willReturn("짜장면");
        given(menuItem2.getPrice()).willReturn(BigDecimal.valueOf(8000));
        given(cartItem2.getQuantity()).willReturn(1);

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem1);
        cartItems.add(cartItem2);

        given(cart.getItems()).willReturn(cartItems);

        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

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
                                        order.getItems().size() == 2
                                                && order.getTotalAmount()
                                                                .compareTo(
                                                                        BigDecimal.valueOf(26000))
                                                        == 0));
        then(orderStatusHistoryService)
                .should()
                .createForCustomerOrderStatusHistory(eq(userId), any(Order.class));
        then(orderEventPublisher)
                .should()
                .publishOrderEvent(any(Order.class), eq(EventType.ORDER_CREATED));
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

    @Test
    @DisplayName("성공 - 내 주문 목록 조회 (빈 목록)")
    void getOrders_success_emptyList() {
        // given
        UUID userId = UUID.randomUUID();
        OrderListRequest request = OrderListRequest.builder().size(20).build();
        given(orderQueryRepository.findOrdersByCustomer(any(OrderListQuery.class)))
                .willReturn(List.of());

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.CUSTOMER.getRole(), request);
        log.info("result = {}", result);

        // then
        assertThat(result.getOrders()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("성공 - 내 주문 목록 조회 (페이징)")
    void getOrders_success_paging() {
        // given
        UUID userId = UUID.randomUUID();
        int size = 5;
        OrderListRequest request = OrderListRequest.builder().size(size).build();

        given(orderQueryRepository.findOrdersByCustomer(any(OrderListQuery.class)))
                .willReturn(
                        new ArrayList<>(
                                Collections.nCopies(
                                        size + 1, OrderSummaryResponse.builder().build())));

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.CUSTOMER.getRole(), request);
        log.info("result = {}", result);

        // then
        assertThat(result.getOrders()).hasSize(size);
        assertThat(result.isHasNext()).isTrue();
    }

    @Test
    @DisplayName("성공 - status 필터 조회")
    void getOrders_success_statusFilter() {
        // given
        UUID userId = UUID.randomUUID();
        OrderListRequest request =
                OrderListRequest.builder().size(20).status(OrderStatus.CREATED).build();

        List<OrderSummaryResponse> filteredOrders =
                List.of(
                        OrderSummaryResponse.builder().build(),
                        OrderSummaryResponse.builder().build());

        given(orderQueryRepository.findOrdersByCustomer(any(OrderListQuery.class)))
                .willReturn(filteredOrders);

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.CUSTOMER.getRole(), request);
        log.info("result = {}", result);

        // then
        then(orderQueryRepository)
                .should()
                .findOrdersByCustomer(argThat(r -> r.status() == OrderStatus.CREATED));
        assertThat(result.getOrders()).hasSize(2);
    }

    @Test
    @DisplayName("성공 - 날짜 범위(startDate~endDate) 필터 조회")
    void getOrders_success_dateRangeFilter() {
        // given
        UUID userId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 5);
        OrderListRequest request =
                OrderListRequest.builder().size(20).startDate(startDate).endDate(endDate).build();

        List<OrderSummaryResponse> filteredOrders = List.of(OrderSummaryResponse.builder().build());

        given(orderQueryRepository.findOrdersByCustomer(any(OrderListQuery.class)))
                .willReturn(filteredOrders);

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.CUSTOMER.getRole(), request);
        log.info("result = {}", result);

        // then
        then(orderQueryRepository)
                .should()
                .findOrdersByCustomer(
                        argThat(
                                r ->
                                        startDate.equals(r.startDate())
                                                && endDate.equals(r.endDate())));
        assertThat(result.getOrders()).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 가게명 혹은 메뉴명 검색")
    void getOrders_success_keywordFilter() {
        // given
        UUID userId = UUID.randomUUID();
        String keyword = "치킨";
        OrderListRequest request = OrderListRequest.builder().size(20).keyword(keyword).build();

        List<OrderSummaryResponse> filteredOrders = List.of(OrderSummaryResponse.builder().build());

        given(orderQueryRepository.findOrdersByCustomer(any(OrderListQuery.class)))
                .willReturn(filteredOrders);

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.CUSTOMER.getRole(), request);
        log.info("result = {}", result);

        // then
        then(orderQueryRepository)
                .should()
                .findOrdersByCustomer(argThat(r -> keyword.equals(r.keyword())));
        assertThat(result.getOrders()).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 복합 필터 조회 (status + keyword + 날짜 범위)")
    void getOrders_success_combinedFilter() {
        // given
        UUID userId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 5);
        OrderListRequest request =
                OrderListRequest.builder()
                        .status(OrderStatus.CREATED)
                        .keyword("치킨")
                        .startDate(startDate)
                        .endDate(endDate)
                        .size(20)
                        .build();

        List<OrderSummaryResponse> filteredOrders = List.of(OrderSummaryResponse.builder().build());

        given(orderQueryRepository.findOrdersByCustomer(any(OrderListQuery.class)))
                .willReturn(filteredOrders);

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.CUSTOMER.getRole(), request);
        log.info("result = {}", result);

        // then
        then(orderQueryRepository)
                .should()
                .findOrdersByCustomer(
                        argThat(
                                r ->
                                        r.status() == OrderStatus.CREATED
                                                && "치킨".equals(r.keyword())
                                                && startDate.equals(r.startDate())
                                                && endDate.equals(r.endDate())));
        assertThat(result.getOrders()).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 커서 기반 다음 페이지 조회 (hasNext=true)")
    void getOrders_success_cursorNextPage() {
        // given
        UUID userId = UUID.randomUUID();
        Instant cursorCreatedAt = Instant.now().minusSeconds(100);
        UUID cursorId = UUID.randomUUID();
        int size = 20;
        OrderListRequest request =
                OrderListRequest.builder()
                        .cursorCreatedAt(cursorCreatedAt)
                        .cursorId(cursorId)
                        .size(size)
                        .build();

        List<OrderSummaryResponse> orders =
                new ArrayList<>(
                        Collections.nCopies(
                                size + 1,
                                OrderSummaryResponse.builder()
                                        .createdAtCursor(Instant.now().minusSeconds(200))
                                        .orderId(UUID.randomUUID())
                                        .build()));

        given(orderQueryRepository.findOrdersByCustomer(any(OrderListQuery.class)))
                .willReturn(orders);

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.CUSTOMER.getRole(), request);
        log.info("result = {}", result);

        // then
        then(orderQueryRepository)
                .should()
                .findOrdersByCustomer(
                        argThat(
                                r ->
                                        cursorCreatedAt.equals(r.cursorCreatedAt())
                                                && cursorId.equals(r.cursorId())));
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursorCreatedAt()).isNotNull();
        assertThat(result.getNextCursorId()).isNotNull();
    }

    @Test
    @DisplayName("성공 - 커서 기반 마지막 페이지 조회")
    void getOrders_success_cursorLastPage() {
        // given
        UUID userId = UUID.randomUUID();
        OrderListRequest request =
                OrderListRequest.builder()
                        .cursorCreatedAt(Instant.now().minusSeconds(100))
                        .cursorId(UUID.randomUUID())
                        .size(20)
                        .build();

        given(orderQueryRepository.findOrdersByCustomer(any(OrderListQuery.class)))
                .willReturn(List.of());

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.CUSTOMER.getRole(), request);
        log.info("result = {}", result);

        // then
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextCursorCreatedAt()).isNull();
        assertThat(result.getNextCursorId()).isNull();
    }

    // ======================== getOrders - OWNER ========================

    @Test
    @DisplayName("실패 - 존재하지 않는 가게")
    void getOrders_owner_fail_storeNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        OrderListRequest request = OrderListRequest.builder().storeId(storeId).build();

        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.empty());

        // when
        Throwable throwable =
                catchThrowable(
                        () -> orderService.getOrders(userId, UserRole.OWNER.getRole(), request));
        log.info("throwable = " + throwable);

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 본인 가게 주문이 아닌 조회")
    void getOrders_owner_fail_storeForbidden() {
        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID otherOwnerId = UUID.randomUUID();
        OrderListRequest request = OrderListRequest.builder().storeId(storeId).build();

        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.of(store));
        given(store.getUserId()).willReturn(otherOwnerId);

        // when
        Throwable throwable =
                catchThrowable(
                        () -> orderService.getOrders(userId, UserRole.OWNER.getRole(), request));
        log.info("throwable = " + throwable);

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_STORE_FORBIDDEN);
    }

    @Test
    @DisplayName("성공 - 가게 주문 목록 조회 (빈 목록)")
    void getOrders_owner_success_emptyList() {
        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        OrderListRequest request = OrderListRequest.builder().storeId(storeId).size(20).build();

        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.of(store));
        given(store.getUserId()).willReturn(userId);
        given(orderQueryRepository.findOrdersByStore(any(OrderListQuery.class)))
                .willReturn(List.of());

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.OWNER.getRole(), request);
        log.info("result = {}", result);

        // then
        assertThat(result.getOrders()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("성공 - 가게 주문 목록 조회 (페이징)")
    void getOrders_owner_success_paging() {
        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        int size = 5;
        OrderListRequest request = OrderListRequest.builder().storeId(storeId).size(size).build();

        List<OrderSummaryResponse> orders =
                new ArrayList<>(
                        Collections.nCopies(size + 1, OrderSummaryResponse.builder().build()));

        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.of(store));
        given(store.getUserId()).willReturn(userId);
        given(orderQueryRepository.findOrdersByStore(any(OrderListQuery.class))).willReturn(orders);

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.OWNER.getRole(), request);
        log.info("result = {}", result);

        // then
        assertThat(result.getOrders()).hasSize(size);
        assertThat(result.isHasNext()).isTrue();
    }

    @Test
    @DisplayName("성공 - 주문번호(orderNo) 검색")
    void getOrders_owner_success_orderNoFilter() {
        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        String orderNo = "ORD-20260305-001";
        OrderListRequest request =
                OrderListRequest.builder().storeId(storeId).orderNo(orderNo).size(20).build();

        List<OrderSummaryResponse> orders =
                new ArrayList<>(Collections.nCopies(21, OrderSummaryResponse.builder().build()));

        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.of(store));
        given(store.getUserId()).willReturn(userId);
        given(orderQueryRepository.findOrdersByStore(any(OrderListQuery.class))).willReturn(orders);

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.OWNER.getRole(), request);
        log.info("result = {}", result);

        // then
        then(orderQueryRepository)
                .should()
                .findOrdersByStore(argThat(r -> orderNo.equals(r.orderNo())));
        assertThat(result.getOrders()).hasSize(20);
    }

    @Test
    @DisplayName("성공 - 복합 필터 조회 (status + orderNo + 날짜 범위)")
    void getOrders_owner_success_combinedFilter() {
        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 5);
        OrderListRequest request =
                OrderListRequest.builder()
                        .storeId(storeId)
                        .status(OrderStatus.CREATED)
                        .orderNo("ORD-20260305-001")
                        .startDate(startDate)
                        .endDate(endDate)
                        .size(20)
                        .build();

        List<OrderSummaryResponse> filteredOrders = List.of(OrderSummaryResponse.builder().build());

        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.of(store));
        given(store.getUserId()).willReturn(userId);
        given(orderQueryRepository.findOrdersByStore(any(OrderListQuery.class)))
                .willReturn(filteredOrders);

        // when
        OrderListResponse result =
                orderService.getOrders(userId, UserRole.OWNER.getRole(), request);
        log.info("result = {}", result);

        // then
        then(orderQueryRepository)
                .should()
                .findOrdersByStore(
                        argThat(
                                r ->
                                        r.status() == OrderStatus.CREATED
                                                && "ORD-20260305-001".equals(r.orderNo())
                                                && startDate.equals(r.startDate())
                                                && endDate.equals(r.endDate())));
        assertThat(result.getOrders()).hasSize(1);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 주문 조회")
    void getOrderDetail_fail_not_found() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserRole userRole = UserRole.CUSTOMER;
        UUID storeId = UUID.randomUUID();

        given(orderRepository.findOrderWithItemsById(orderId)).willReturn(Optional.empty());

        // when
        Throwable throwable =
                catchThrowable(
                        () -> orderService.getOrderDetail(userId, userRole, storeId, orderId));
        log.info("throwable = " + throwable);

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 본인 주문이 아님 (CUSTOMER)")
    void getOrderDetail_fail_not_my_order() {

        // given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UserRole userRole = UserRole.CUSTOMER;

        given(order.getUserId()).willReturn(userId2);

        given(orderRepository.findOrderWithItemsById(orderId)).willReturn(Optional.of(order));

        // when
        Throwable throwable =
                catchThrowable(
                        () -> orderService.getOrderDetail(userId1, userRole, storeId, orderId));
        log.info("throwable = " + throwable);

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_FORBIDDEN);
    }

    @Test
    @DisplayName("실패 - 본인 가게 주문이 아님 (OWNER)")
    void getOrderDetail_fail_not_my_store_order() {

        // given
        UUID storeId1 = UUID.randomUUID();
        UUID storeId2 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole userRole = UserRole.OWNER;

        given(order.getStoreId()).willReturn(storeId2);

        given(orderRepository.findOrderWithItemsById(orderId)).willReturn(Optional.of(order));

        // when
        Throwable throwable =
                catchThrowable(
                        () -> orderService.getOrderDetail(userId, userRole, storeId1, orderId));
        log.info("throwable = " + throwable);

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_STORE_FORBIDDEN);
    }

    @Test
    @DisplayName("성공 - CUSTOMER 단건 정상 조회")
    void getOrderDetail_success_customer() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UserRole userRole = UserRole.CUSTOMER;

        given(order.getUserId()).willReturn(userId);
        given(orderRepository.findOrderWithItemsById(orderId)).willReturn(Optional.of(order));

        // when
        OrderDetailResponse response =
                orderService.getOrderDetail(userId, userRole, storeId, orderId);
        log.info("response = " + response);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("성공 - OWNER 단건 정상 조회")
    void getOrderDetail_success_owner() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole userRole = UserRole.OWNER;

        given(order.getStoreId()).willReturn(storeId);

        given(orderRepository.findOrderWithItemsById(orderId)).willReturn(Optional.of(order));

        // when
        OrderDetailResponse response =
                orderService.getOrderDetail(userId, userRole, storeId, orderId);
        log.info("response = " + response);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 주문")
    void getOrderStatus_fail_order_not_found() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole userRole = UserRole.CUSTOMER;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());

        // when
        Throwable throwable =
                catchThrowable(
                        () -> orderService.getOrderStatus(userId, userRole, storeId, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 본인 주문이 아님 (CUSTOMER)")
    void getOrderStatus_fail_not_my_order() {

        // given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole userRole = UserRole.CUSTOMER;

        given(order.getUserId()).willReturn(userId2);

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        // when
        Throwable throwable =
                catchThrowable(
                        () -> orderService.getOrderStatus(userId1, userRole, storeId, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_FORBIDDEN);
    }

    @Test
    @DisplayName("실패 - 본인 가게 주문이 아님 (OWNER)")
    void getOrderStatus_fail_not_my_store_order() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId1 = UUID.randomUUID();
        UUID storeId2 = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole userRole = UserRole.OWNER;

        given(order.getStoreId()).willReturn(storeId2);

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        // when
        Throwable throwable =
                catchThrowable(
                        () -> orderService.getOrderStatus(userId, userRole, storeId1, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_STORE_FORBIDDEN);
    }

    @Test
    @DisplayName("성공 - CUSTOMER 주문 상태 조회")
    void getOrderStatus_success_customer() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole userRole = UserRole.CUSTOMER;

        List<OrderStatusHistory> histories = List.of(orderStatusHistory);

        given(order.getUserId()).willReturn(userId);

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId))
                .willReturn(histories);

        // when
        OrderStatusResponse response =
                orderService.getOrderStatus(userId, userRole, storeId, orderId);
        log.info("response = " + response);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("성공 - OWNER 주문 상태 조회")
    void getOrderStatus_success_owner() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole userRole = UserRole.OWNER;

        List<OrderStatusHistory> histories = List.of(orderStatusHistory);

        given(order.getStoreId()).willReturn(storeId);

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId))
                .willReturn(histories);

        // when
        OrderStatusResponse response =
                orderService.getOrderStatus(userId, userRole, storeId, orderId);
        log.info("response = " + response);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("실패 - 주문 요청 시 존재하지 않는 주문")
    void requestOrder_fail_orderNotFound() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> orderService.requestOrder(userId, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 본인 주문이 아님")
    void requestOrder_fail_notOwner() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getUserId()).willReturn(UUID.randomUUID());

        // when
        Throwable throwable = catchThrowable(() -> orderService.requestOrder(userId, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_FORBIDDEN);
    }

    @Test
    @DisplayName("실패 - 주문에 대한 결제가 존재하지 않음")
    void requestOrder_fail_payment_not_found() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getUserId()).willReturn(userId);

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> orderService.requestOrder(userId, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 결제가 완료되지 않은 주문 요청 시도")
    void requestOrder_fail_payment_not_completed() {

        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getUserId()).willReturn(userId);

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

        given(payment.getStatus()).willReturn(PaymentStatus.PENDING);

        // when
        Throwable throwable = catchThrowable(() -> orderService.requestOrder(userId, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_PAYMENT_NOT_COMPLETED);
    }

    @Test
    @DisplayName("실패 - CREATED 상태가 아닌 주문 요청 시도")
    void requestOrder_fail_invalidStatus() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getUserId()).willReturn(userId);

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

        given(payment.getStatus()).willReturn(PaymentStatus.SUCCESS);

        given(order.canRequest()).willReturn(false);

        // when
        Throwable throwable = catchThrowable(() -> orderService.requestOrder(userId, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_INVALID_STATUS);
    }

    @Test
    @DisplayName("성공 - 주문 요청 시 상태 변경 및 상태 이력 생성")
    void requestOrder_success_create_history() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getUserId()).willReturn(userId);
        given(order.getStatus()).willReturn(OrderStatus.CREATED);

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

        given(payment.getStatus()).willReturn(PaymentStatus.SUCCESS);

        given(order.canRequest()).willReturn(true);

        // when
        OrderActionStatusResponse response = orderService.requestOrder(userId, orderId);

        // then
        then(order).should().request();
        then(orderStatusHistoryService)
                .should()
                .createForCustomerOrderStatusHistory(
                        eq(userId), eq(OrderStatus.CREATED), any(Order.class));
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 주문")
    void acceptOrder_fail_order_not_found() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UserRole userRole = UserRole.OWNER;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());

        // when
        Throwable throwable =
                catchThrowable(() -> orderService.acceptOrder(userId, userRole, storeId, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 본인 가게 주문이 아님")
    void acceptOrder_fail_not_store_owner() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID storeId1 = UUID.randomUUID();
        UUID storeId2 = UUID.randomUUID();
        UserRole userRole = UserRole.OWNER;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId2);

        // when
        Throwable throwable =
                catchThrowable(() -> orderService.acceptOrder(userId, userRole, storeId1, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_STORE_FORBIDDEN);
    }

    @Test
    @DisplayName("실패 - REQUESTED 상태가 아닌 주문 수락 시도")
    void acceptOrder_fail_invalid_status() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID storeId1 = UUID.randomUUID();
        UUID storeId2 = UUID.randomUUID();
        UserRole userRole = UserRole.OWNER;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId1);

        given(order.canAcceptOrReject()).willReturn(false);

        // when
        Throwable throwable =
                catchThrowable(() -> orderService.acceptOrder(userId, userRole, storeId1, orderId));

        // then
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_INVALID_STATUS);
    }

    @Test
    @DisplayName("성공 - 주문 수락 시 상태 변경 및 상태 이력 생성")
    void acceptOrder_success() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UserRole userRole = UserRole.OWNER;
        OrderStatus fromStatus = OrderStatus.REQUESTED;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId);

        given(order.canAcceptOrReject()).willReturn(true);

        given(order.getStatus()).willReturn(OrderStatus.REQUESTED);

        // when
        OrderActionStatusResponse response =
                orderService.acceptOrder(userId, userRole, storeId, orderId);

        // then
        then(order).should().accept();

        then(orderStatusHistoryService)
                .should()
                .createForOwnerOrderStatusHistory(
                        eq(userId), eq(fromStatus), any(Order.class), isNull());
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 주문")
    void rejectOrder_fail_orderNotFound() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole role = UserRole.OWNER;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());

        // when
        Throwable thrown =
                catchThrowable(
                        () -> orderService.rejectOrder(userId, role, storeId, orderId, null));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 본인 가게 주문이 아님")
    void rejectOrder_fail_storeForbidden() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID otherStoreId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole role = UserRole.OWNER;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(otherStoreId);

        // when
        Throwable thrown =
                catchThrowable(
                        () -> orderService.rejectOrder(userId, role, storeId, orderId, null));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_STORE_FORBIDDEN);
    }

    @Test
    @DisplayName("실패 - REQUESTED 상태가 아닌 주문 거절 시도")
    void rejectOrder_fail_invalidStatus() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole role = UserRole.OWNER;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId);

        given(order.canAcceptOrReject()).willReturn(false);

        // when
        Throwable thrown =
                catchThrowable(
                        () -> orderService.rejectOrder(userId, role, storeId, orderId, null));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_INVALID_STATUS);
    }

    @Test
    @DisplayName("성공 - 주문 거절 시 상태 변경 및 상태 이력 생성")
    void rejectOrder_success() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UserRole userRole = UserRole.OWNER;
        OrderStatus fromStatus = OrderStatus.REQUESTED;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId);

        given(order.canAcceptOrReject()).willReturn(true);

        given(order.getStatus()).willReturn(OrderStatus.REQUESTED);

        // when
        OrderActionStatusResponse response =
                orderService.rejectOrder(userId, userRole, storeId, orderId, null);

        // then
        then(order).should().reject();

        then(orderStatusHistoryService)
                .should()
                .createForOwnerOrderStatusHistory(
                        eq(userId), eq(fromStatus), any(Order.class), isNull());

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 주문")
    void completeCooking_fail_orderNotFound() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole role = UserRole.OWNER;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());

        // when
        Throwable thrown =
                catchThrowable(
                        () -> orderService.completeCookingOrder(userId, role, storeId, orderId));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 본인 가게 주문이 아님")
    void completeCooking_fail_storeForbidden() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole role = UserRole.OWNER;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(UUID.randomUUID());

        // when
        Throwable thrown =
                catchThrowable(
                        () -> orderService.completeCookingOrder(userId, role, storeId, orderId));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_STORE_FORBIDDEN);
    }

    @Test
    @DisplayName("실패 - ACCEPTED 상태가 아닌 조리 완료 시도")
    void completeCooking_fail_invalidStatus() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserRole role = UserRole.OWNER;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId);
        given(order.canCompleteCooking()).willReturn(false);

        // when
        Throwable thrown =
                catchThrowable(
                        () -> orderService.completeCookingOrder(userId, role, storeId, orderId));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_INVALID_STATUS);
    }

    @Test
    @DisplayName("성공 - 조리 완료 시 상태 변경 및 상태 이력 생성")
    void completeCooking_success() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UserRole role = UserRole.OWNER;

        OrderStatus fromStatus = OrderStatus.ACCEPTED;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId);

        given(order.canCompleteCooking()).willReturn(true);

        given(order.getStatus()).willReturn(OrderStatus.ACCEPTED);

        // when
        OrderActionStatusResponse response =
                orderService.completeCookingOrder(userId, role, storeId, orderId);

        // then
        then(order).should().canCompleteCooking();

        then(orderStatusHistoryService)
                .should()
                .createForOwnerOrderStatusHistory(
                        eq(userId), eq(fromStatus), any(Order.class), isNull());

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 주문")
    void startDelivery_fail_order_not_found() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                orderService.startDeliveryOrder(
                                        userId, UserRole.OWNER, storeId, orderId));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 다른 가게 주문 접근")
    void startDelivery_fail_store_forbidden() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID otherStoreId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(otherStoreId);

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                orderService.startDeliveryOrder(
                                        userId, UserRole.OWNER, storeId, orderId));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_STORE_FORBIDDEN.getMessage());
    }

    @Test
    @DisplayName("실패 - COOKED 상태가 아닌 배달 시작 시도")
    void startDelivery_fail_invalid_status() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId);
        given(order.canStartDelivery()).willReturn(false);

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                orderService.startDeliveryOrder(
                                        userId, UserRole.OWNER, storeId, orderId));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_INVALID_STATUS.getMessage());
    }

    @Test
    @DisplayName("성공 - 배달 시작 시 상태 변경 및 상태 이력 생성")
    void startDelivery_success() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderStatus fromStatus = OrderStatus.COOKED;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId);
        given(order.canStartDelivery()).willReturn(true);
        given(order.getStatus()).willReturn(fromStatus);

        // when
        OrderActionStatusResponse response =
                orderService.startDeliveryOrder(userId, UserRole.OWNER, storeId, orderId);

        // then
        then(order).should().startDelivery();

        then(orderStatusHistoryService)
                .should()
                .createForOwnerOrderStatusHistory(
                        eq(userId), eq(fromStatus), any(Order.class), isNull());

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 주문")
    void completeDelivery_fail_order_not_found() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                orderService.completeDeliveryOrder(
                                        userId, UserRole.OWNER, storeId, orderId));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 다른 가게 주문 접근")
    void completeDelivery_fail_store_forbidden() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID otherStoreId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(otherStoreId);

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                orderService.completeDeliveryOrder(
                                        userId, UserRole.OWNER, storeId, orderId));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_STORE_FORBIDDEN.getMessage());
    }

    @Test
    @DisplayName("실패 - DELIVERING 상태가 아닌 배달 완료 시도")
    void completeDelivery_fail_invalid_status() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId);
        given(order.canCompleteDelivery()).willReturn(false);

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                orderService.completeDeliveryOrder(
                                        userId, UserRole.OWNER, storeId, orderId));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_INVALID_STATUS.getMessage());
    }

    @Test
    @DisplayName("성공 - 배달 완료 시 상태 변경 및 상태 이력 생성")
    void completeDelivery_success() {

        // given
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderStatus fromStatus = OrderStatus.DELIVERING;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId);
        given(order.canCompleteDelivery()).willReturn(true);
        given(order.getStatus()).willReturn(fromStatus);

        // when
        OrderActionStatusResponse response =
                orderService.completeDeliveryOrder(userId, UserRole.OWNER, storeId, orderId);

        // then
        then(order).should().completeDelivery();

        then(orderStatusHistoryService)
                .should()
                .createForOwnerOrderStatusHistory(
                        eq(userId), eq(fromStatus), any(Order.class), isNull());

        assertThat(response).isNotNull();

        then(orderEventPublisher)
                .should()
                .publishOrderEvent(any(Order.class), eq(EventType.ORDER_DELIVERED));
    }

    @DisplayName("주문 취소 요청 실패 - 존재하지 않는 주문")
    @Test
    void cancelRequestOrder_fail_order_not_found() {

        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                        () ->
                                orderService.cancelRequestOrder(
                                        userId, UserRole.CUSTOMER, null, orderId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @DisplayName("주문 취소 요청 실패 - 이미 취소 대기 상태인 주문")
    @Test
    void cancelRequestOrder_fail_order_already_cancel_request() {

        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStatus()).willReturn(OrderStatus.CANCEL_REQUESTED);

        // when & then
        assertThatThrownBy(
                        () ->
                                orderService.cancelRequestOrder(
                                        userId, UserRole.CUSTOMER, null, orderId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_INVALID_STATUS.getMessage());
    }

    @DisplayName("주문 취소 요청 실패 - 이미 취소된 주문")
    @Test
    void cancelRequestOrder_fail_order_already_cancel_end() {

        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));
        given(order.getStatus()).willReturn(OrderStatus.CANCELED);

        // when & then
        assertThatThrownBy(
                        () ->
                                orderService.cancelRequestOrder(
                                        userId, UserRole.CUSTOMER, null, orderId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_INVALID_STATUS.getMessage());
    }

    @DisplayName("주문 취소 요청 실패 - 본인 주문이 아님 (고객)")
    @Test
    void cancelRequestOrder_fail_not_my_order_customer() {

        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getUserId()).willReturn(UUID.randomUUID());

        // when & then
        assertThatThrownBy(
                        () ->
                                orderService.cancelRequestOrder(
                                        userId, UserRole.CUSTOMER, null, orderId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_FORBIDDEN.getMessage());
    }

    @DisplayName("주문 취소 요청 실패 - 본인 가게 주문이 아님 (사장님)")
    @Test
    void cancelRequestOrder_fail_not_my_store() {

        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(UUID.randomUUID());

        // when & then
        assertThatThrownBy(
                        () ->
                                orderService.cancelRequestOrder(
                                        userId, UserRole.OWNER, storeId, orderId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_STORE_FORBIDDEN.getMessage());
    }

    @DisplayName("주문 취소 요청 실패 - 취소 불가 상태")
    @Test
    void cancelRequestOrder_fail_invalid_status() {

        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getUserId()).willReturn(userId);
        given(order.canCancelRequestByCustomer()).willReturn(false);

        // when & then
        assertThatThrownBy(
                        () ->
                                orderService.cancelRequestOrder(
                                        userId, UserRole.CUSTOMER, null, orderId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_INVALID_STATUS.getMessage());
    }

    @DisplayName("주문 취소 요청 실패 - 주문 생성 후 5분 이후 취소 시도 (고객)")
    @Test
    void cancelRequestOrder_fail_invalid_Order_createdAt_five_minute() {

        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getUserId()).willReturn(userId);
        given(order.canCancelRequestByCustomer()).willReturn(true);
        given(order.getCreatedAt()).willReturn(Instant.now().minusSeconds(301));

        // when & then
        assertThatThrownBy(
                        () ->
                                orderService.cancelRequestOrder(
                                        userId, UserRole.CUSTOMER, null, orderId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_CANCEL_NOT_ALLOWED.getMessage());
    }

    @DisplayName("주문 취소 요청 성공 - REQUESTED 상태 고객 취소")
    @Test
    void cancelRequestOrder_success_customer_requested() {

        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getUserId()).willReturn(userId);
        given(order.getStatus()).willReturn(OrderStatus.REQUESTED);
        given(order.canCancelRequestByCustomer()).willReturn(true);
        given(order.getCreatedAt()).willReturn(Instant.now().minusSeconds(120));

        // when
        orderService.cancelRequestOrder(userId, UserRole.CUSTOMER, null, orderId, null);

        // then
        verify(order).cancelRequested();
        verify(orderStatusHistoryService)
                .createForCustomerOrderStatusHistory(userId, OrderStatus.REQUESTED, order);
        then(orderEventPublisher)
                .should()
                .publishOrderEvent(any(Order.class), eq(EventType.ORDER_CANCEL_REQUESTED));
    }

    @DisplayName("주문 취소 요청 성공 - ACCEPTED 상태 고객 취소")
    @Test
    void cancelRequestOrder_success_customer_accepted() {

        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getUserId()).willReturn(userId);
        given(order.canCancelRequestByCustomer()).willReturn(true);
        given(order.getCreatedAt()).willReturn(Instant.now().minusSeconds(120));

        // when
        orderService.cancelRequestOrder(userId, UserRole.CUSTOMER, null, orderId, null);

        // then
        verify(order).cancelRequested();
        then(orderEventPublisher)
                .should()
                .publishOrderEvent(any(Order.class), eq(EventType.ORDER_CANCEL_REQUESTED));
    }

    @DisplayName("주문 취소 요청 성공 - ACCEPTED 상태 사장 취소")
    @Test
    void cancelRequestOrder_success_owner() {

        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.getStoreId()).willReturn(storeId);
        given(order.canCancelRequestByOwner()).willReturn(true);
        given(order.getStatus()).willReturn(OrderStatus.ACCEPTED);

        // when
        orderService.cancelRequestOrder(userId, UserRole.OWNER, storeId, orderId, null);

        // then
        verify(order).cancelRequested();
        verify(orderStatusHistoryService)
                .createForOwnerOrderStatusHistory(userId, OrderStatus.ACCEPTED, order, null);
        then(orderEventPublisher)
                .should()
                .publishOrderEvent(any(Order.class), eq(EventType.ORDER_CANCEL_REQUESTED));
    }

    @DisplayName("주문 취소 실패 - 존재하지 않는 주문")
    @Test
    void completeCancelOrder_fail_order_not_found() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> orderService.completeCancelOrder(userId, orderId));
        log.info("throwable = " + thrown);

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
    }

    @DisplayName("주문 취소 실패 - CANCEL_REQUESTED 상태가 아님")
    @Test
    void completeCancelOrder_fail_invalid_status() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));
        given(order.canCompleteCancel()).willReturn(false);

        // when
        Throwable thrown = catchThrowable(() -> orderService.completeCancelOrder(userId, orderId));
        log.info("throwable = " + thrown);

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_INVALID_STATUS);
    }

    @DisplayName("주문 취소 성공 - 주문 취소 시 상태 변경 및 상태 이력 생성")
    @Test
    void completeCancelOrder_success() {

        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderStatus fromStatus = OrderStatus.CANCEL_REQUESTED;

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));
        given(order.canCompleteCancel()).willReturn(true);
        given(order.getStatus()).willReturn(fromStatus);

        // when
        OrderActionStatusResponse response = orderService.completeCancelOrder(userId, orderId);

        // then
        then(order).should().cancel();

        then(orderStatusHistoryService)
                .should()
                .createForOwnerOrderStatusHistory(
                        eq(userId), eq(fromStatus), any(Order.class), isNull());

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("관리자 주문 취소 요청 - 성공")
    void cancelRequestOrder_byAdmin_success() {
        // given
        String reason = "관리자 취소 사유";
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        given(order.canCancelRequestByAdmin()).willReturn(true);
        given(order.getStatus()).willReturn(OrderStatus.ACCEPTED);
        given(order.getId()).willReturn(orderId);

        // when
        OrderActionStatusResponse response =
                orderService.cancelRequestOrder(adminId, UserRole.MANAGER, null, orderId, reason);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);

        verify(order).cancelRequested();
        verify(orderStatusHistoryService)
                .createForAdminOrderStatusHistory(adminId, OrderStatus.ACCEPTED, order, reason);
        verify(orderEventPublisher).publishOrderEvent(order, EventType.ORDER_UPDATED);
    }
}
