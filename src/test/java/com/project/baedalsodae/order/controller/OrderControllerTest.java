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
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.*;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.review.service.ReviewService;
import com.project.baedalsodae.user.entity.UserRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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

    @MockitoBean private ReviewService reviewService;

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

    @Test
    @DisplayName("성공 - CUSTOMER 주문 목록 조회 (빈 목록)")
    void getOrders_customer_emptyList() throws Exception {
        given(
                        orderService.getOrders(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq("CUSTOMER"),
                                ArgumentMatchers.any(OrderListRequest.class)))
                .willReturn(OrderListResponse.empty());

        mockMvc.perform(
                        get("/orders")
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "CUSTOMER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_LIST.getCode()))
                .andExpect(jsonPath("$.data.orders").isEmpty())
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("성공 - CUSTOMER 주문 목록 조회 (페이징, hasNext=true)")
    void getOrders_customer_paged() throws Exception {
        OrderSummaryResponse summary =
                OrderSummaryResponse.builder()
                        .orderId(UUID.randomUUID())
                        .orderNo("ORD-001")
                        .status(OrderStatus.CREATED)
                        .storeNameSnapshot("테스트 가게")
                        .finalAmount(BigDecimal.valueOf(20000))
                        .createdAt(LocalDateTime.now())
                        .createdAtCursor(Instant.now())
                        .build();

        OrderListResponse response = OrderListResponse.from(List.of(summary), true);

        given(
                        orderService.getOrders(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq("CUSTOMER"),
                                ArgumentMatchers.any(OrderListRequest.class)))
                .willReturn(response);

        mockMvc.perform(
                        get("/orders")
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "CUSTOMER")
                                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_LIST.getCode()))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.orders[0].orderNo").value("ORD-001"));
    }

    @Test
    @DisplayName("실패 - 잘못된 날짜 범위")
    void getOrders_fail_invalidDateRange() throws Exception {
        given(
                        orderService.getOrders(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq("CUSTOMER"),
                                ArgumentMatchers.any(OrderListRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_DATE_RANGE));

        mockMvc.perform(
                        get("/orders")
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "CUSTOMER")
                                .param("startDate", "2025-01-31")
                                .param("endDate", "2025-01-01"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_DATE_RANGE.getCode()));
    }

    @Test
    @DisplayName("성공 - OWNER 가게 주문 목록 조회")
    void getOrders_owner_success() throws Exception {
        UUID storeId = UUID.randomUUID();

        OrderSummaryResponse summary =
                OrderSummaryResponse.builder()
                        .orderId(UUID.randomUUID())
                        .orderNo("ORD-002")
                        .status(OrderStatus.CREATED)
                        .storeNameSnapshot("사장 가게")
                        .finalAmount(BigDecimal.valueOf(15000))
                        .createdAt(LocalDateTime.now())
                        .createdAtCursor(Instant.now())
                        .build();

        OrderListResponse response = OrderListResponse.from(List.of(summary), false);

        given(
                        orderService.getOrders(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq("OWNER"),
                                ArgumentMatchers.any(OrderListRequest.class)))
                .willReturn(response);

        mockMvc.perform(
                        get("/orders")
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_LIST.getCode()))
                .andExpect(jsonPath("$.data.orders[0].orderNo").value("ORD-002"));
    }

    @Test
    @DisplayName("실패 - OWNER 존재하지 않는 가게")
    void getOrders_owner_fail_storeNotFound() throws Exception {
        UUID storeId = UUID.randomUUID();

        given(
                        orderService.getOrders(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq("OWNER"),
                                ArgumentMatchers.any(OrderListRequest.class)))
                .willThrow(new BusinessException(ErrorCode.STORE_NOT_FOUND));

        mockMvc.perform(
                        get("/orders")
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.STORE_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("실패 - OWNER 본인 가게 주문이 아닌 조회")
    void getOrders_owner_fail_forbidden() throws Exception {
        UUID storeId = UUID.randomUUID();

        given(
                        orderService.getOrders(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq("OWNER"),
                                ArgumentMatchers.any(OrderListRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN));

        mockMvc.perform(
                        get("/orders")
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_STORE_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 상태 조회")
    void getOrderStatus_success() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        OrderStatusResponse response =
                new OrderStatusResponse(orderId, OrderStatus.CREATED, List.of());

        given(
                        orderService.getOrderStatus(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq(UserRole.CUSTOMER),
                                ArgumentMatchers.isNull(),
                                ArgumentMatchers.eq(orderId)))
                .willReturn(response);

        mockMvc.perform(
                        get("/orders/{orderId}/status", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "CUSTOMER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_STATUS.getCode()))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 주문 상태 조회")
    void getOrderStatus_fail_orderNotFound() throws Exception {

        UUID orderId = UUID.randomUUID();

        given(
                        orderService.getOrderStatus(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq(UserRole.CUSTOMER),
                                ArgumentMatchers.isNull(),
                                ArgumentMatchers.eq(orderId)))
                .willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(
                        get("/orders/{orderId}/status", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "CUSTOMER"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("실패 - 본인 주문 상태 조회 아님")
    void getOrderStatus_fail_forbidden() throws Exception {

        UUID orderId = UUID.randomUUID();

        given(
                        orderService.getOrderStatus(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq(UserRole.CUSTOMER),
                                ArgumentMatchers.isNull(),
                                ArgumentMatchers.eq(orderId)))
                .willThrow(new BusinessException(ErrorCode.ORDER_FORBIDDEN));

        mockMvc.perform(
                        get("/orders/{orderId}/status", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "CUSTOMER"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 요청")
    void requestOrder_success() throws Exception {

        UUID orderId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.ACCEPTED)
                        .build();

        given(orderService.requestOrder(userId, orderId)).willReturn(response);

        mockMvc.perform(post("/orders/{orderId}/request", orderId).header("X-User-Id", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_REQUESTED.getCode()));
    }

    @Test
    @DisplayName("실패 - 주문 요청 시 존재하지 않는 주문")
    void requestOrder_fail_orderNotFound() throws Exception {

        UUID orderId = UUID.randomUUID();

        given(orderService.requestOrder(userId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(post("/orders/{orderId}/request", orderId).header("X-User-Id", userId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 수락")
    void acceptOrder_success() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.REJECTED)
                        .build();

        given(orderService.acceptOrder(userId, UserRole.OWNER, storeId, orderId))
                .willReturn(response);

        mockMvc.perform(
                        post("/orders/{orderId}/accept", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_ACCEPTED.getCode()));
    }

    @Test
    @DisplayName("실패 - 주문 수락 시 본인 가게 주문 아님")
    void acceptOrder_fail_storeForbidden() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderService.acceptOrder(userId, UserRole.OWNER, storeId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN));

        mockMvc.perform(
                        post("/orders/{orderId}/accept", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_STORE_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 거절")
    void rejectOrder_success() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.REQUESTED)
                        .build();

        given(orderService.rejectOrder(userId, UserRole.OWNER, storeId, orderId, null))
                .willReturn(response);

        mockMvc.perform(
                        post("/orders/{orderId}/reject", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_REJECTED.getCode()));
    }

    @Test
    @DisplayName("실패 - 주문 거절 시 잘못된 상태")
    void rejectOrder_fail_invalidStatus() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderService.rejectOrder(userId, UserRole.OWNER, storeId, orderId, null))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(
                        post("/orders/{orderId}/reject", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_STATUS.getCode()));
    }

    @Test
    @DisplayName("성공 - 조리 완료")
    void completeCookingOrder_success() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.COOKED)
                        .build();

        given(orderService.completeCookingOrder(userId, UserRole.OWNER, storeId, orderId))
                .willReturn(response);

        mockMvc.perform(
                        post("/orders/{orderId}/cooked", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_COOKING_COMPLETED.getCode()));
    }

    @Test
    @DisplayName("실패 - 조리 완료 시 잘못된 상태")
    void completeCookingOrder_fail_invalidStatus() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderService.completeCookingOrder(userId, UserRole.OWNER, storeId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(
                        post("/orders/{orderId}/cooked", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_STATUS.getCode()));
    }

    @Test
    @DisplayName("성공 - 배달 시작")
    void startDeliveryOrder_success() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.DELIVERING)
                        .build();

        given(orderService.startDeliveryOrder(userId, UserRole.OWNER, storeId, orderId))
                .willReturn(response);

        mockMvc.perform(
                        post("/orders/{orderId}/delivering", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_DELIVERING.getCode()));
    }

    @Test
    @DisplayName("실패 - 배달 시작 시 잘못된 상태")
    void startDeliveryOrder_fail_invalidStatus() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderService.startDeliveryOrder(userId, UserRole.OWNER, storeId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(
                        post("/orders/{orderId}/delivering", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_STATUS.getCode()));
    }

    @Test
    @DisplayName("성공 - 배달 완료")
    void completeDeliveryOrder_success() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.DELIVERED)
                        .build();

        given(orderService.completeDeliveryOrder(userId, UserRole.OWNER, storeId, orderId))
                .willReturn(response);

        mockMvc.perform(
                        post("/orders/{orderId}/delivered", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_DELIVERED.getCode()));
    }

    @Test
    @DisplayName("실패 - 배달 완료 시 잘못된 상태")
    void completeDeliveryOrder_fail_invalidStatus() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderService.completeDeliveryOrder(userId, UserRole.OWNER, storeId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(
                        post("/orders/{orderId}/delivered", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "OWNER")
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_STATUS.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 취소 요청")
    void cancelRequestOrder_success() throws Exception {

        UUID orderId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.CANCEL_REQUESTED)
                        .build();

        given(orderService.cancelRequestOrder(userId, UserRole.CUSTOMER, null, orderId, null))
                .willReturn(response);

        mockMvc.perform(
                        post("/orders/{orderId}/cancel-request", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "CUSTOMER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_CANCEL_REQUESTED.getCode()));
    }

    @Test
    @DisplayName("실패 - 주문 취소 요청 시 본인 주문 아님")
    void cancelRequestOrder_fail_forbidden() throws Exception {

        UUID orderId = UUID.randomUUID();

        given(orderService.cancelRequestOrder(userId, UserRole.CUSTOMER, null, orderId, null))
                .willThrow(new BusinessException(ErrorCode.ORDER_FORBIDDEN));

        mockMvc.perform(
                        post("/orders/{orderId}/cancel-request", orderId)
                                .header("X-User-Id", userId)
                                .header("X-User-Role", "CUSTOMER"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 취소")
    void completeCancelOrder_success() throws Exception {

        UUID orderId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.CANCELED)
                        .build();

        given(orderService.completeCancelOrder(userId, orderId)).willReturn(response);

        mockMvc.perform(post("/orders/{orderId}/cancel", orderId).header("X-User-Id", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_CANCELED.getCode()));
    }

    @Test
    @DisplayName("실패 - 주문 취소 시 CANCEL_REQUESTED 상태가 아님")
    void completeCancelOrder_fail_invalidStatus() throws Exception {

        UUID orderId = UUID.randomUUID();

        given(orderService.completeCancelOrder(userId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(post("/orders/{orderId}/cancel", orderId).header("X-User-Id", userId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_STATUS.getCode()));
    }
}
