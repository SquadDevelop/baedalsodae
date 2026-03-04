package com.project.baedalsodae.order.service.impl;

import com.project.baedalsodae.cart.entity.Cart;
import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.OrderItem;
import com.project.baedalsodae.order.entity.OrderStatusHistory;
import com.project.baedalsodae.order.publisher.OrderEventPublisher;
import com.project.baedalsodae.order.repository.OrderRepository;
import com.project.baedalsodae.order.repository.OrderStatusHistoryRepository;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.order.util.OrderNoGenerator;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final CartRepository cartRepository;
	private final StoreRepository storeRepository;
	private final OrderRepository orderRepository;
	private final OrderStatusHistoryRepository orderStatusHistoryRepository;
	private final OrderEventPublisher eventPublisher;

	@Override
	@Transactional
	public CreateOrderResponse createOrder(UUID userId, CreateOrderRequest request) {
		final UUID cartId = request.cartId();
		final UUID addressId = request.addressId();

		Cart cart = cartRepository.findCartWithItemsByIdAndUserId(cartId, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

		if (cart.hasNoItems())
			throw new BusinessException(ErrorCode.CART_ITEM_EMPTY);

		final UUID storeId = cart.getStore().getId();
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

		int totalAmount = cart.getTotalAmount();
		if (totalAmount <= 0)
			throw new BusinessException(ErrorCode.ORDER_INVALID_TOTAL_AMOUNT);

		// 할인 쿠폰 도메인, 배달 도메인이 없음
		final int deliveryFee = 0;
		final int discountAmount = 0;
		final int finalAmount = totalAmount - discountAmount + deliveryFee;
		if (finalAmount < 0)
			throw new BusinessException(ErrorCode.ORDER_INVALID_FINAL_AMOUNT);

		//TODO 주소 도메인 완성 후 만들어야함. 주소 조회, 주소를 배달 주소 스냅샷으로 변환
		String deliveryAddressSnapshot = "서울특별시 강남구 테헤란로 123 (역삼동) 4층";

		final String createdOrderNo = OrderNoGenerator.generate();

		Order order = Order.create(
				userId,
				store,
				addressId,
				deliveryAddressSnapshot,
				createdOrderNo,
				request.storeRequestMessage(),
				request.deliveryRequestMessage(),
				totalAmount,
				deliveryFee,
				discountAmount,
				finalAmount
		);

		List<OrderItem> orderItems = cart.getItems().stream()
				.map(cartItem -> OrderItem.create(
						order,
						cartItem
				))
				.toList();

		order.addOrderItems(orderItems);

		final Order savedOrder = orderRepository.save(order);

		OrderStatusHistory orderStatusHistory = OrderStatusHistory.create(
				savedOrder,
				userId
		);

		orderStatusHistoryRepository.save(orderStatusHistory);

		eventPublisher.publishOrderCreated(savedOrder);

		return CreateOrderResponse.from(savedOrder);
	}

}
