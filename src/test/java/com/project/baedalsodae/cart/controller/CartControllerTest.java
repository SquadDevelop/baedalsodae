package com.project.baedalsodae.cart.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.cart.dto.request.AddCartItemRequest;
import com.project.baedalsodae.cart.dto.request.UpdateCartItemQuantityRequest;
import com.project.baedalsodae.cart.dto.response.CartResponse;
import com.project.baedalsodae.cart.service.CartService;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.user.entity.UserRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class CartControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CartService cartService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        UserDetailsImpl userDetails =
                UserDetailsImpl.builder()
                        .userId(userId)
                        .username("testUser")
                        .password(null)
                        .userRole(UserRole.CUSTOMER)
                        .isDeleted(false)
                        .build();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        userDetails, null, userDetails.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("성공 - 장바구니 조회")
    void getCart_success() throws Exception {
        CartResponse response =
                new CartResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        1,
                        1,
                        BigDecimal.valueOf(18000),
                        List.of(),
                        Instant.now(),
                        Instant.now());
        given(cartService.getCart(userId)).willReturn(response);

        mockMvc.perform(get("/carts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.CART_FOUND.getCode()))
                .andDo(
                        document(
                                "cart/get-cart",
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.cartId").description("장바구니 ID"),
                                        fieldWithPath("data.storeId").description("가게 ID"),
                                        fieldWithPath("data.totalItemCount")
                                                .description("총 아이템 종류 수"),
                                        fieldWithPath("data.totalQuantity").description("총 수량"),
                                        fieldWithPath("data.totalAmount").description("총 금액"),
                                        fieldWithPath("data.items").description("장바구니 아이템 목록"),
                                        fieldWithPath("data.createdAt").description("생성일시"),
                                        fieldWithPath("data.updatedAt").description("수정일시"))));
    }

    @Test
    @DisplayName("실패 - 장바구니 조회 시 장바구니 없음")
    void getCart_fail_cartNotFound() throws Exception {
        given(cartService.getCart(userId))
                .willThrow(new BusinessException(ErrorCode.CART_NOT_FOUND));

        mockMvc.perform(get("/carts")).andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("성공 - 장바구니 아이템 추가")
    void addCartItem_success() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        AddCartItemRequest request =
                new AddCartItemRequest(UUID.randomUUID(), UUID.randomUUID(), 1);
        CartResponse response =
                new CartResponse(
                        cartId,
                        storeId,
                        1,
                        1,
                        BigDecimal.valueOf(18000),
                        List.of(),
                        Instant.now(),
                        Instant.now());
        given(cartService.addCartItem(userId, request)).willReturn(response);

        mockMvc.perform(
                        post("/carts/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(SuccessCode.CART_ITEM_ADDED.getCode()))
                .andDo(
                        document(
                                "cart/add-cart-item",
                                requestFields(
                                        fieldWithPath("storeId").description("가게 ID"),
                                        fieldWithPath("menuItemId").description("메뉴 아이템 ID"),
                                        fieldWithPath("quantity").description("수량 (1 이상)"),
                                        fieldWithPath("validQuantity").ignored()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.cartId").description("장바구니 ID"),
                                        fieldWithPath("data.storeId").description("가게 ID"),
                                        fieldWithPath("data.totalItemCount")
                                                .description("총 아이템 종류 수"),
                                        fieldWithPath("data.totalQuantity").description("총 수량"),
                                        fieldWithPath("data.totalAmount").description("총 금액"),
                                        fieldWithPath("data.items").description("장바구니 아이템 목록"),
                                        fieldWithPath("data.createdAt").description("생성일시"),
                                        fieldWithPath("data.updatedAt").description("수정일시"))));
    }

    @Test
    @DisplayName("실패 - 장바구니 아이템 추가 시 수량 0 이하")
    void addCartItem_fail_invalidQuantity() throws Exception {
        AddCartItemRequest request =
                new AddCartItemRequest(UUID.randomUUID(), UUID.randomUUID(), 0);

        mockMvc.perform(
                        post("/carts/items")
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

        mockMvc.perform(
                        patch("/carts/items/{cartItemId}", cartItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code").value(SuccessCode.CART_ITEM_QUANTITY_UPDATED.getCode()))
                .andDo(
                        document(
                                "cart/update-cart-item-quantity",
                                pathParameters(
                                        parameterWithName("cartItemId").description("장바구니 아이템 ID")),
                                requestFields(
                                        fieldWithPath("quantity").description("변경할 수량 (1 이상)"),
                                        fieldWithPath("validQuantity").ignored()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data").optional().ignored())));
    }

    @Test
    @DisplayName("성공 - 장바구니 아이템 삭제")
    void removeCartItem_success() throws Exception {
        UUID cartItemId = UUID.randomUUID();

        mockMvc.perform(delete("/carts/items/{cartItemId}", cartItemId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.CART_ITEM_REMOVED.getCode()))
                .andDo(
                        document(
                                "cart/remove-cart-item",
                                pathParameters(
                                        parameterWithName("cartItemId").description("장바구니 아이템 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data").optional().ignored())));
    }

    @Test
    @DisplayName("성공 - 장바구니 비우기")
    void clearCart_success() throws Exception {
        mockMvc.perform(delete("/carts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.CART_CLEARED.getCode()))
                .andDo(
                        document(
                                "cart/clear-cart",
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data").optional().ignored())));
    }
}
