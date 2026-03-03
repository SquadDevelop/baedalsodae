package com.project.baedalsodae.order.service;

import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

	@InjectMocks
	private OrderServiceImpl orderService;

	@Mock
	private CartRepository cartRepository;

	@Test
	@DisplayName("실패 - 주문 생성 시 장바구니가 존재하지 않음")
	void createOrder_fail_cartNotFound() {
		//given
		UUID userId = UUID.randomUUID();
		UUID cartId = UUID.randomUUID();
		UUID addressId = UUID.randomUUID();

		String storeRequestMessage = "리뷰이벤트 잽닝이 막국수 주시면 감사하겠습니다.";

		CreateOrderRequest request = CreateOrderRequest.builder()
				.cartId(cartId)
				.addressId(addressId)
				.storeRequestMessage(storeRequestMessage)
				.build();

		//when
		Throwable throwable = catchThrowable(() -> orderService.createOrder(userId, request));
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);
	}
}
