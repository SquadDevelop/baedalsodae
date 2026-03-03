package com.project.baedalsodae.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.cart.dto.request.AddCartItemRequest;
import com.project.baedalsodae.cart.dto.request.UpdateCartItemQuantityRequest;
import com.project.baedalsodae.cart.dto.response.CartResponse;
import com.project.baedalsodae.cart.service.CartService;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CartService cartService;

	private UUID userId;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
	}

	@Test
	@DisplayName("성공 - 장바구니 조회")
	void getCart_success() throws Exception {
		CartResponse response = new CartResponse(
				UUID.randomUUID(),
				UUID.randomUUID(),
				1,
				1,
				18000,
				List.of(),
				Instant.now(),
				Instant.now()
		);
		given(cartService.getCart(userId)).willReturn(response);

		mockMvc.perform(get("/api/v1/carts")
						.header("X-User-Id", userId))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("CT200"));
	}

	@Test
	@DisplayName("실패 - 장바구니 조회 시 장바구니 없음")
	void getCart_fail_cartNotFound() throws Exception {
		given(cartService.getCart(userId))
				.willThrow(new BusinessException(ErrorCode.CART_NOT_FOUND));

		mockMvc.perform(get("/api/v1/carts")
						.header("X-User-Id", userId))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("성공 - 장바구니 아이템 추가")
	void addCartItem_success() throws Exception {
		UUID storeId = UUID.randomUUID();
		UUID cartId = UUID.randomUUID();

		AddCartItemRequest request = new AddCartItemRequest(UUID.randomUUID(), UUID.randomUUID(), 1);
		CartResponse response = new CartResponse(
				cartId,
				storeId,
				1,
				1,
				18000,
				List.of(),
				Instant.now(),
				Instant.now()
		);
		given(cartService.addCartItem(userId, request)).willReturn(response);

		mockMvc.perform(post("/api/v1/carts/items")
						.header("X-User-Id", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("CT201"));
	}

	@Test
	@DisplayName("실패 - 장바구니 아이템 추가 시 수량 0 이하")
	void addCartItem_fail_invalidQuantity() throws Exception {
		AddCartItemRequest request = new AddCartItemRequest(UUID.randomUUID(), UUID.randomUUID(), 0);

		mockMvc.perform(post("/api/v1/carts/items")
						.header("X-User-Id", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("성공 - 장바구니 아이템 수량 변경")
	void updateCartItemQuantity_success() throws Exception {
		UUID cartItemId = UUID.randomUUID();
		UpdateCartItemQuantityRequest request = new UpdateCartItemQuantityRequest(3);

		mockMvc.perform(patch("/api/v1/carts/items/{cartItemId}", cartItemId)
						.header("X-User-Id", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("CT202"));
	}

	@Test
	@DisplayName("성공 - 장바구니 아이템 삭제")
	void removeCartItem_success() throws Exception {
		UUID cartItemId = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/carts/items/{cartItemId}", cartItemId)
						.header("X-User-Id", userId))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("CT203"));
	}

	@Test
	@DisplayName("성공 - 장바구니 비우기")
	void clearCart_success() throws Exception {
		mockMvc.perform(delete("/api/v1/carts")
						.header("X-User-Id", userId))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("CT204"));
	}
}