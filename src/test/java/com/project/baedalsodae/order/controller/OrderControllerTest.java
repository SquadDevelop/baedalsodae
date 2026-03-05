package com.project.baedalsodae.order.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.order.dto.response.CreateOrderResponse;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.order.service.OrderService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest extends OrderControllerTestHelper {

  @MockitoBean private OrderService orderService;

  @Test
  @DisplayName("성공 - 주문 생성")
  void createOrder_success() throws Exception {
    // Given
    given(orderService.createOrder(userId, ORDER_FIXTURE)).willReturn(getOrderMockResponse());

    // When
    var resultActions =
        mockMvc.perform(
            post(ORDER_BASE_URL)
                .header(X_USER_ID_HEADER, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ORDER_FIXTURE)));

    // Then
    resultActions
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_CREATED.getCode()));
  }

  @Test
  @DisplayName("실패 - 주문 생성 시 장바구니 없음")
  void createOrder_fail_cartNotFound() throws Exception {
    given(orderService.createOrder(userId, ORDER_FIXTURE))
        .willThrow(new BusinessException(ErrorCode.CART_NOT_FOUND));

    mockMvc
        .perform(
            post(ORDER_BASE_URL)
                .header(X_USER_ID_HEADER, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ORDER_FIXTURE)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.CART_NOT_FOUND.getCode()));
    ;
  }

  private static CreateOrderResponse getOrderMockResponse() {
    UUID orderId = UUID.randomUUID();
    CreateOrderResponse response = new CreateOrderResponse(orderId, OrderStatus.CREATED);
    return response;
  }
}
