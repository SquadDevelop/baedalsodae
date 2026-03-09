package com.project.baedalsodae.order.service.impl;

import com.project.baedalsodae.allowedRegion.service.AllowedRegionService;
import com.project.baedalsodae.cart.entity.Cart;
import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.order.dto.query.OrderListQuery;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.*;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.OrderItem;
import com.project.baedalsodae.order.entity.OrderStatusHistory;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.order.publisher.OrderEventPublisher;
import com.project.baedalsodae.order.repository.OrderQueryRepository;
import com.project.baedalsodae.order.repository.OrderRepository;
import com.project.baedalsodae.order.repository.OrderStatusHistoryRepository;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.order.service.OrderStatusHistoryService;
import com.project.baedalsodae.order.util.OrderNoGenerator;
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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderStatusHistoryService orderStatusHistoryService;
    private final CartRepository cartRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderQueryRepository orderQueryRepository;
    private final PaymentRepository paymentRepository;
    private final AllowedRegionService allowedRegionService;
    private final UserAddressService userAddressService;

    @Override
    @Transactional
    public CreateOrderResponse createOrder(UUID userId, CreateOrderRequest request) {
        final UUID cartId = request.cartId();
        final UUID addressId = request.addressId();
        Cart cart =
                cartRepository
                        .findCartWithItemsByIdAndUserId(cartId, userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        if (cart.hasNoItems()) throw new BusinessException(ErrorCode.CART_ITEM_EMPTY);

        final UUID storeId = cart.getStore().getId();
        Store store =
                storeRepository
                        .findByIdAndIsDeletedIsFalse(storeId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (!allowedRegionService.isAllowedByCode(store.getAddress().getSigunguCode())) {
            throw new BusinessException(ErrorCode.STORE_REGION_NOT_ALLOWED);
        }

        BigDecimal totalAmount = cart.getTotalAmount();
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException(ErrorCode.ORDER_INVALID_TOTAL_AMOUNT);

        // 할인 쿠폰 도메인, 배달 도메인이 없음
        final BigDecimal deliveryFee = BigDecimal.ZERO;
        final BigDecimal discountAmount = BigDecimal.ZERO;
        final BigDecimal finalAmount = totalAmount.subtract(discountAmount).add(deliveryFee);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException(ErrorCode.ORDER_INVALID_FINAL_AMOUNT);

        UserAddress userAddress = userAddressService.getMainUserAddress(userId);
        if (!allowedRegionService.isAllowedByCode(userAddress.getAddress().getSigunguCode())) {
            throw new BusinessException(ErrorCode.USER_ADDRESS_NOT_ALLOWED);
        }

        // TODO 주소 도메인 완성 후 만들어야함. 주소 조회, 주소를 배달 주소 스냅샷으로 변환
        String deliveryAddressSnapshot = "서울특별시 강남구 테헤란로 123 (역삼동) 4층";

        final String createdOrderNo = OrderNoGenerator.generate();

        // TODO 인증 도메인 완료 시 넣어줌
        final String userNickName = "잽닝";
        final String userPhone = "01011111111";

        Order order =
                Order.create(
                        userId,
                        userNickName,
                        userPhone,
                        store,
                        addressId,
                        deliveryAddressSnapshot,
                        createdOrderNo,
                        request.storeRequestMessage(),
                        request.deliveryRequestMessage(),
                        totalAmount,
                        deliveryFee,
                        discountAmount,
                        finalAmount);

        List<OrderItem> orderItems =
                cart.getItems().stream()
                        .map(cartItem -> OrderItem.create(order, cartItem))
                        .toList();
        order.addOrderItems(orderItems);
        final Order savedOrder = orderRepository.save(order);

        orderStatusHistoryService.createForCustomerOrderStatusHistory(userId, order);

        orderEventPublisher.publishOrderCreated(savedOrder);

        return CreateOrderResponse.from(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderListResponse getOrders(UUID userId, String role, OrderListRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        // TODO 인증 도메인 완성 시 AOP로 권한 체크
        if (UserRole.MANAGER.getRole().equals(role) || UserRole.MASTER.getRole().equals(role)) {
            return getAdminOrders(request);
        }
        if (UserRole.OWNER.getRole().equals(role)) {
            return getOwnerOrders(userId, request);
        }
        return getCustomerOrders(userId, request);
    }

    private OrderListResponse getCustomerOrders(UUID userId, OrderListRequest request) {
        OrderListQuery query = OrderListQuery.forCustomer(userId, request);
        List<OrderSummaryResponse> orders = orderQueryRepository.findOrdersByCustomer(query);

        boolean hasNext = orders.size() > query.resolvedSize();
        if (hasNext) orders = orders.subList(0, query.resolvedSize());

        return OrderListResponse.from(orders, hasNext);
    }

    private OrderListResponse getOwnerOrders(UUID userId, OrderListRequest request) {
        Store store =
                storeRepository
                        .findByIdAndIsDeletedIsFalse(request.storeId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        if (!store.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN);
        }

        OrderListQuery query = OrderListQuery.forOwner(store.getId(), request);
        List<OrderSummaryResponse> orders = orderQueryRepository.findOrdersByStore(query);

        boolean hasNext = orders.size() > query.resolvedSize();
        if (hasNext) orders = orders.subList(0, query.resolvedSize());

        return OrderListResponse.from(orders, hasNext);
    }

    private OrderListResponse getAdminOrders(OrderListRequest request) {
        OrderListQuery query = OrderListQuery.forAdmin(request);
        List<OrderSummaryResponse> orders = orderQueryRepository.findAllOrders(query);

        boolean hasNext = orders.size() > query.resolvedSize();
        if (hasNext) orders = orders.subList(0, query.resolvedSize());

        return OrderListResponse.from(orders, hasNext);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_DATE_RANGE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(
            UUID userId, UserRole userRole, UUID storeId, UUID orderId) {

        Order order =
                orderRepository
                        .findOrderWithItemsById(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (userRole.getRole().equals(UserRole.CUSTOMER.getRole())
                && !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
        }

        if (userRole.getRole().equals(UserRole.OWNER.getRole())
                && !order.getStoreId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN);
        }

        return OrderDetailResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatusResponse getOrderStatus(
            UUID userId, UserRole userRole, UUID storeId, UUID orderId) {
        Order order =
                orderRepository
                        .findByIdAndIsDeletedFalse(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (userRole.getRole().equals(UserRole.CUSTOMER.getRole())
                && !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
        }

        if (userRole.getRole().equals(UserRole.OWNER.getRole())
                && !order.getStoreId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN);
        }

        List<OrderStatusHistory> histories =
                orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId);

        return OrderStatusResponse.from(order, histories);
    }

    @Override
    @Transactional
    public OrderActionStatusResponse requestOrder(UUID userId, UUID orderId) {
        Order order =
                orderRepository
                        .findByIdAndIsDeletedFalse(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
        }

        Payment payment =
                paymentRepository
                        .findByOrderId(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.ORDER_PAYMENT_NOT_COMPLETED);
        }

        if (!order.canRequest()) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        final OrderStatus fromStatus = order.getStatus();

        order.request();

        orderStatusHistoryService.createForCustomerOrderStatusHistory(userId, fromStatus, order);

        orderEventPublisher.publishOrderRequested(order);

        return OrderActionStatusResponse.from(order, payment);
    }

    @Override
    @Transactional
    public OrderActionStatusResponse acceptOrder(
            UUID userId, UserRole userRole, UUID storeId, UUID orderId) {
        Order order =
                orderRepository
                        .findByIdAndIsDeletedFalse(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (userRole.getRole().equals(UserRole.OWNER.getRole())
                && !order.getStoreId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN);
        }

        if (!order.canAcceptOrReject()) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        final OrderStatus fromStatus = order.getStatus();

        order.accept();

        orderStatusHistoryService.createForOwnerOrderStatusHistory(userId, fromStatus, order, null);

        orderEventPublisher.publishOrderAccepted(order);

        return OrderActionStatusResponse.from(order);
    }

    @Override
    @Transactional
    public OrderActionStatusResponse rejectOrder(
            UUID userId, UserRole userRole, UUID storeId, UUID orderId, String reason) {
        Order order =
                orderRepository
                        .findByIdAndIsDeletedFalse(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (userRole.getRole().equals(UserRole.OWNER.getRole())
                && !order.getStoreId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN);
        }

        if (!order.canAcceptOrReject()) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        final OrderStatus fromStatus = order.getStatus();

        order.reject();

        orderStatusHistoryService.createForOwnerOrderStatusHistory(
                userId, fromStatus, order, reason);

        orderEventPublisher.publishOrderRejected(order);

        return OrderActionStatusResponse.from(order);
    }

    @Override
    @Transactional
    public OrderActionStatusResponse completeCookingOrder(
            UUID userId, UserRole userRole, UUID storeId, UUID orderId) {
        Order order =
                orderRepository
                        .findByIdAndIsDeletedFalse(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (userRole.getRole().equals(UserRole.OWNER.getRole())
                && !order.getStoreId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN);
        }

        if (!order.canCompleteCooking()) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        final OrderStatus fromStatus = order.getStatus();

        order.completeCooking();

        orderStatusHistoryService.createForOwnerOrderStatusHistory(userId, fromStatus, order, null);

        orderEventPublisher.publishOrderCooked(order);

        return OrderActionStatusResponse.from(order);
    }

    @Override
    @Transactional
    public OrderActionStatusResponse startDeliveryOrder(
            UUID userId, UserRole userRole, UUID storeId, UUID orderId) {

        Order order =
                orderRepository
                        .findByIdAndIsDeletedFalse(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (userRole.getRole().equals(UserRole.OWNER.getRole())
                && !order.getStoreId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN);
        }

        if (!order.canStartDelivery()) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        final OrderStatus fromStatus = order.getStatus();

        order.startDelivery();

        orderStatusHistoryService.createForOwnerOrderStatusHistory(userId, fromStatus, order, null);

        orderEventPublisher.publishOrderDelivering(order);

        return OrderActionStatusResponse.from(order);
    }

    @Override
    @Transactional
    public OrderActionStatusResponse completeDeliveryOrder(
            UUID userId, UserRole userRole, UUID storeId, UUID orderId) {

        Order order =
                orderRepository
                        .findByIdAndIsDeletedFalse(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (userRole.getRole().equals(UserRole.OWNER.getRole())
                && !order.getStoreId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN);
        }

        if (!order.canCompleteDelivery()) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        final OrderStatus fromStatus = order.getStatus();

        order.completeDelivery();

        orderStatusHistoryService.createForOwnerOrderStatusHistory(userId, fromStatus, order, null);

        orderEventPublisher.publishOrderDelivered(order);

        return OrderActionStatusResponse.from(order);
    }

    @Override
    @Transactional
    public OrderActionStatusResponse cancelRequestOrder(
            UUID userId, UserRole userRole, UUID storeId, UUID orderId, String reason) {

        Order order =
                orderRepository
                        .findByIdAndIsDeletedFalse(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCEL_REQUESTED) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        if (userRole == UserRole.CUSTOMER) {
            return cancelRequestByCustomer(userId, order);
        } else if (userRole == UserRole.OWNER) {
            return cancelRequestByOwner(userId, storeId, order, reason);
        }

        throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
    }

    private OrderActionStatusResponse cancelRequestByCustomer(UUID userId, Order order) {
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
        }
        if (!order.canCancelRequestByCustomer()) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        if (order.getCreatedAt().plusSeconds(300).isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
        }

        final OrderStatus fromStatus = order.getStatus();

        order.cancelRequested();

        orderStatusHistoryService.createForCustomerOrderStatusHistory(userId, fromStatus, order);

        orderEventPublisher.publishOrderCancelRequested(order);

        return OrderActionStatusResponse.from(order);
    }

    @Override
    @Transactional
    public OrderActionStatusResponse completeCancelOrder(UUID userId, UUID orderId) {
        Order order =
                orderRepository
                        .findByIdAndIsDeletedFalse(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.canCompleteCancel()) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        final OrderStatus fromStatus = order.getStatus();

        order.cancel();

        orderStatusHistoryService.createForOwnerOrderStatusHistory(userId, fromStatus, order, null);

        orderEventPublisher.publishOrderCanceled(order);

        return OrderActionStatusResponse.from(order);
    }

    @Override
    public boolean isOrderDelivered(UUID orderId) {
        Order order =
                orderRepository
                        .findByIdAndIsDeletedFalse(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return order.getStatus() == OrderStatus.DELIVERED;
    }

    private OrderActionStatusResponse cancelRequestByOwner(
            UUID userId, UUID storeId, Order order, String reason) {
        if (!order.getStoreId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN);
        }

        if (!order.canCancelRequestByOwner()) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }

        final OrderStatus fromStatus = order.getStatus();

        order.cancelRequested();

        orderStatusHistoryService.createForOwnerOrderStatusHistory(
                userId, fromStatus, order, reason);

        orderEventPublisher.publishOrderCancelRequested(order);

        return OrderActionStatusResponse.from(order);
    }

    @Override
    public Order findById(final UUID orderId) {
        return orderRepository
                .findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }
}
