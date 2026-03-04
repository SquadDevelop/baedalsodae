package com.project.baedalsodae.order.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.order.dto.request.CreateOrderRequest;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.order.service.OrderService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private OrderService orderService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("성공 - 주문 생성")
    void createOrder_success() throws Exception {
        UUID orderId = UUID.randomUUID();
        CreateOrderRequest request =
                CreateOrderRequest.builder()
                        .cartId(UUID.randomUUID())
                        .addressId(UUID.randomUUID())
                        .build();

        CreateOrderResponse response = new CreateOrderResponse(orderId, OrderStatus.CREATED);
        given(orderService.createOrder(userId, request)).willReturn(response);

        mockMvc.perform(
                        post("/orders")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_CREATED.getCode()));
    }

    @Test
    @DisplayName("실패 - 주문 생성 시 장바구니 없음")
    void createOrder_fail_cartNotFound() throws Exception {
        CreateOrderRequest request =
                CreateOrderRequest.builder()
                        .cartId(UUID.randomUUID())
                        .addressId(UUID.randomUUID())
                        .build();

        given(orderService.createOrder(userId, request))
                .willThrow(new BusinessException(ErrorCode.CART_NOT_FOUND));

        mockMvc.perform(
                        post("/orders")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.CART_NOT_FOUND.getCode()));
        ;
    }
}
