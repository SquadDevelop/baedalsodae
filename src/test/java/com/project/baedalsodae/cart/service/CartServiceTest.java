package com.project.baedalsodae.cart.service;

import com.project.baedalsodae.cart.repository.CartRepository;
import com.project.baedalsodae.cart.service.impl.CartServiceImpl;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

	@Mock
	private CartRepository cartRepository;

	@InjectMocks
	private CartServiceImpl cartService;

	@Test
	@DisplayName("실패 - 장바구니가 존재하지 않음")
	void getCart_fail_cartNotFound(){
		//given
		UUID userId = UUID.randomUUID();
		given(cartRepository.findByUserId(userId))
				.willReturn(Optional.empty());

		//when
		Throwable throwable = catchThrowable(()-> cartService.getCart(userId));
		log.info("throwable = " + throwable);

		//then
		assertThat(throwable)
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);
	}



}
