package com.project.baedalsodae.order.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private OrderService orderService;

    @MockitoBean private ReviewService reviewService;

    private UUID userId;
    private UserDetailsImpl customerDetails;
    private UserDetailsImpl ownerDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        customerDetails =
                UserDetailsImpl.builder()
                        .userId(userId)
                        .username("customerUser")
                        .password(null)
                        .userRole(UserRole.CUSTOMER)
                        .isDeleted(false)
                        .build();
        ownerDetails =
                UserDetailsImpl.builder()
                        .userId(userId)
                        .username("ownerUser")
                        .password(null)
                        .userRole(UserRole.OWNER)
                        .isDeleted(false)
                        .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(UserDetailsImpl userDetails) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        userDetails, null, userDetails.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("성공 - 주문 생성")
    void createOrder_success() throws Exception {
        setSecurityContext(customerDetails);

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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_CREATED.getCode()))
                .andDo(
                        document(
                                "order/create-order",
                                requestFields(
                                        fieldWithPath("cartId").description("장바구니 ID"),
                                        fieldWithPath("addressId").description("배달 주소 ID"),
                                        fieldWithPath("storeRequestMessage")
                                                .optional()
                                                .description("가게 요청 메시지"),
                                        fieldWithPath("deliveryRequestMessage")
                                                .optional()
                                                .description("배달 요청 메시지")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("생성된 주문 ID"),
                                        fieldWithPath("data.status").description("주문 상태"))));
    }

    @Test
    @DisplayName("실패 - 주문 생성 시 장바구니 없음")
    void createOrder_fail_cartNotFound() throws Exception {
        setSecurityContext(customerDetails);

        CreateOrderRequest request =
                CreateOrderRequest.builder()
                        .cartId(UUID.randomUUID())
                        .addressId(UUID.randomUUID())
                        .build();

        given(orderService.createOrder(userId, request))
                .willThrow(new BusinessException(ErrorCode.CART_NOT_FOUND));

        mockMvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.CART_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("성공 - CUSTOMER 주문 목록 조회 (빈 목록)")
    void getOrders_customer_emptyList() throws Exception {
        setSecurityContext(customerDetails);

        given(
                        orderService.getOrders(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq("CUSTOMER"),
                                ArgumentMatchers.any(OrderListRequest.class)))
                .willReturn(OrderListResponse.empty());

        mockMvc.perform(get("/orders"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_LIST.getCode()))
                .andExpect(jsonPath("$.data.orders").isEmpty())
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("성공 - CUSTOMER 주문 목록 조회 (페이징, hasNext=true)")
    void getOrders_customer_paged() throws Exception {
        setSecurityContext(customerDetails);

        OrderSummaryResponse summary =
                OrderSummaryResponse.builder()
                        .orderId(UUID.randomUUID())
                        .storeId(UUID.randomUUID())
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

        mockMvc.perform(get("/orders").param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_LIST.getCode()))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.orders[0].orderNo").value("ORD-001"))
                .andDo(
                        document(
                                "order/get-orders",
                                queryParameters(
                                        parameterWithName("size")
                                                .optional()
                                                .description("페이지 크기 (기본값: 10)"),
                                        parameterWithName("status")
                                                .optional()
                                                .description("주문 상태 필터"),
                                        parameterWithName("startDate")
                                                .optional()
                                                .description("검색 시작 날짜 (yyyy-MM-dd)"),
                                        parameterWithName("endDate")
                                                .optional()
                                                .description("검색 종료 날짜 (yyyy-MM-dd)"),
                                        parameterWithName("keyword")
                                                .optional()
                                                .description("가게명/메뉴명 검색어"),
                                        parameterWithName("storeId")
                                                .optional()
                                                .description("가게 ID (OWNER 역할에서 필요)"),
                                        parameterWithName("orderNo")
                                                .optional()
                                                .description("주문번호 검색어 (OWNER용)"),
                                        parameterWithName("cursorCreatedAt")
                                                .optional()
                                                .description("커서 기반 페이징 - 기준 생성일시"),
                                        parameterWithName("cursorId")
                                                .optional()
                                                .description("커서 기반 페이징 - 기준 주문 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orders").description("주문 목록"),
                                        fieldWithPath("data.orders[].orderId").description("주문 ID"),
                                        fieldWithPath("data.orders[].storeId").description("가게 ID"),
                                        fieldWithPath("data.orders[].orderNo").description("주문 번호"),
                                        fieldWithPath("data.orders[].status").description("주문 상태"),
                                        fieldWithPath("data.orders[].storeNameSnapshot")
                                                .description("가게명"),
                                        fieldWithPath("data.orders[].finalAmount")
                                                .description("최종 결제 금액"),
                                        fieldWithPath("data.orders[].createdAtCursor")
                                                .description("생성일시 (커서용)"),
                                        fieldWithPath("data.orders[].createdAt")
                                                .description("생성일시"),
                                        fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                        fieldWithPath("data.nextCursorCreatedAt")
                                                .optional()
                                                .description("다음 페이지 커서 - 기준 생성일시"),
                                        fieldWithPath("data.nextCursorId")
                                                .optional()
                                                .description("다음 페이지 커서 - 기준 주문 ID"))));
    }

    @Test
    @DisplayName("실패 - 잘못된 날짜 범위")
    void getOrders_fail_invalidDateRange() throws Exception {
        setSecurityContext(customerDetails);

        given(
                        orderService.getOrders(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq("CUSTOMER"),
                                ArgumentMatchers.any(OrderListRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_DATE_RANGE));

        mockMvc.perform(
                        get("/orders")
                                .param("startDate", "2025-01-31")
                                .param("endDate", "2025-01-01"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_DATE_RANGE.getCode()));
    }

    @Test
    @DisplayName("성공 - OWNER 가게 주문 목록 조회")
    void getOrders_owner_success() throws Exception {
        setSecurityContext(ownerDetails);

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

        mockMvc.perform(get("/orders").param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_LIST.getCode()))
                .andExpect(jsonPath("$.data.orders[0].orderNo").value("ORD-002"));
    }

    @Test
    @DisplayName("실패 - OWNER 존재하지 않는 가게")
    void getOrders_owner_fail_storeNotFound() throws Exception {
        setSecurityContext(ownerDetails);

        UUID storeId = UUID.randomUUID();

        given(
                        orderService.getOrders(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq("OWNER"),
                                ArgumentMatchers.any(OrderListRequest.class)))
                .willThrow(new BusinessException(ErrorCode.STORE_NOT_FOUND));

        mockMvc.perform(get("/orders").param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.STORE_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("실패 - OWNER 본인 가게 주문이 아닌 조회")
    void getOrders_owner_fail_forbidden() throws Exception {
        setSecurityContext(ownerDetails);

        UUID storeId = UUID.randomUUID();

        given(
                        orderService.getOrders(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq("OWNER"),
                                ArgumentMatchers.any(OrderListRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN));

        mockMvc.perform(get("/orders").param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_STORE_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("성공 - CUSTOMER 주문 상세 조회")
    void getOrderDetail_success() throws Exception {
        setSecurityContext(customerDetails);

        UUID orderId = UUID.randomUUID();
        OrderDetailResponse response =
                OrderDetailResponse.builder()
                        .orderId(orderId)
                        .orderNo("ORD-20260310-0001")
                        .userNickname("테스트고객")
                        .userPhone("010-1234-5678")
                        .storeId(UUID.randomUUID())
                        .storeName("테스트 가게")
                        .status(OrderStatus.CREATED)
                        .storeRequestNote("요청사항 없음")
                        .deliveryRequestNote("문 앞에 놓아주세요")
                        .deliveryAddressSnapshot("서울특별시 강남구 테헤란로 1")
                        .totalAmount(BigDecimal.valueOf(10000))
                        .deliveryFee(BigDecimal.valueOf(3000))
                        .discountAmount(BigDecimal.ZERO)
                        .finalAmount(BigDecimal.valueOf(13000))
                        .items(List.of())
                        .orderCreatedAt(LocalDateTime.now())
                        .orderedAt(LocalDateTime.now())
                        .build();

        given(
                        orderService.getOrderDetail(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq(UserRole.CUSTOMER),
                                ArgumentMatchers.isNull(),
                                ArgumentMatchers.eq(orderId)))
                .willReturn(response);

        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_DETAIL.getCode()))
                .andDo(
                        document(
                                "order/get-order-detail",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                queryParameters(
                                        parameterWithName("storeId")
                                                .optional()
                                                .description("가게 ID (OWNER 역할에서 필요)")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.orderNo").description("주문 번호"),
                                        fieldWithPath("data.userNickname").description("주문자 닉네임"),
                                        fieldWithPath("data.userPhone").description("주문자 전화번호"),
                                        fieldWithPath("data.storeId").description("가게 ID"),
                                        fieldWithPath("data.storeName").description("가게명"),
                                        fieldWithPath("data.status").description("주문 상태"),
                                        fieldWithPath("data.storeRequestNote")
                                                .description("가게 요청 사항"),
                                        fieldWithPath("data.deliveryRequestNote")
                                                .description("배달 요청 사항"),
                                        fieldWithPath("data.deliveryAddressSnapshot")
                                                .description("배달 주소"),
                                        fieldWithPath("data.totalAmount").description("총 금액"),
                                        fieldWithPath("data.deliveryFee").description("배달비"),
                                        fieldWithPath("data.discountAmount").description("할인 금액"),
                                        fieldWithPath("data.finalAmount").description("최종 결제 금액"),
                                        fieldWithPath("data.items").description("주문 아이템 목록"),
                                        fieldWithPath("data.orderCreatedAt").description("주문 생성일시"),
                                        fieldWithPath("data.orderedAt").description("주문 요청일시"))));
    }

    @Test
    @DisplayName("성공 - 주문 상태 조회")
    void getOrderStatus_success() throws Exception {
        setSecurityContext(customerDetails);

        UUID orderId = UUID.randomUUID();

        OrderStatusResponse response =
                new OrderStatusResponse(orderId, OrderStatus.CREATED, List.of());

        given(
                        orderService.getOrderStatus(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq(UserRole.CUSTOMER),
                                ArgumentMatchers.isNull(),
                                ArgumentMatchers.eq(orderId)))
                .willReturn(response);

        mockMvc.perform(get("/orders/{orderId}/status", orderId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_STATUS.getCode()))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andDo(
                        document(
                                "order/get-order-status",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                queryParameters(
                                        parameterWithName("storeId")
                                                .optional()
                                                .description("가게 ID (OWNER 역할에서 필요)")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.currentStatus").description("현재 주문 상태"),
                                        fieldWithPath("data.histories")
                                                .description("상태 전이 이력 목록"))));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 주문 상태 조회")
    void getOrderStatus_fail_orderNotFound() throws Exception {
        setSecurityContext(customerDetails);

        UUID orderId = UUID.randomUUID();

        given(
                        orderService.getOrderStatus(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq(UserRole.CUSTOMER),
                                ArgumentMatchers.isNull(),
                                ArgumentMatchers.eq(orderId)))
                .willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(get("/orders/{orderId}/status", orderId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("실패 - 본인 주문 상태 조회 아님")
    void getOrderStatus_fail_forbidden() throws Exception {
        setSecurityContext(customerDetails);

        UUID orderId = UUID.randomUUID();

        given(
                        orderService.getOrderStatus(
                                ArgumentMatchers.eq(userId),
                                ArgumentMatchers.eq(UserRole.CUSTOMER),
                                ArgumentMatchers.isNull(),
                                ArgumentMatchers.eq(orderId)))
                .willThrow(new BusinessException(ErrorCode.ORDER_FORBIDDEN));

        mockMvc.perform(get("/orders/{orderId}/status", orderId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 요청")
    void requestOrder_success() throws Exception {
        setSecurityContext(customerDetails);

        UUID orderId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.REQUESTED)
                        .build();

        given(orderService.requestOrder(userId, orderId)).willReturn(response);

        mockMvc.perform(post("/orders/{orderId}/request", orderId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_REQUESTED.getCode()))
                .andDo(
                        document(
                                "order/request-order",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.orderStatus").description("변경된 주문 상태"),
                                        fieldWithPath("data.paymentStatus")
                                                .optional()
                                                .description("결제 상태"))));
    }

    @Test
    @DisplayName("실패 - 주문 요청 시 존재하지 않는 주문")
    void requestOrder_fail_orderNotFound() throws Exception {
        setSecurityContext(customerDetails);

        UUID orderId = UUID.randomUUID();

        given(orderService.requestOrder(userId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(post("/orders/{orderId}/request", orderId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 수락")
    void acceptOrder_success() throws Exception {
        setSecurityContext(ownerDetails);

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.ACCEPTED)
                        .build();

        given(orderService.acceptOrder(userId, UserRole.OWNER, storeId, orderId))
                .willReturn(response);

        mockMvc.perform(
                        post("/orders/{orderId}/accept", orderId)
                                .queryParam("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_ACCEPTED.getCode()))
                .andDo(
                        document(
                                "order/accept-order",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                queryParameters(parameterWithName("storeId").description("가게 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.orderStatus").description("변경된 주문 상태"),
                                        fieldWithPath("data.paymentStatus")
                                                .optional()
                                                .description("결제 상태"))));
    }

    @Test
    @DisplayName("실패 - 주문 수락 시 본인 가게 주문 아님")
    void acceptOrder_fail_storeForbidden() throws Exception {
        setSecurityContext(ownerDetails);

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderService.acceptOrder(userId, UserRole.OWNER, storeId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_STORE_FORBIDDEN));

        mockMvc.perform(
                        post("/orders/{orderId}/accept", orderId)
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_STORE_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 거절")
    void rejectOrder_success() throws Exception {
        setSecurityContext(ownerDetails);

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.REJECTED)
                        .build();

        given(orderService.rejectOrder(userId, UserRole.OWNER, storeId, orderId, null))
                .willReturn(response);

        mockMvc.perform(
                        post("/orders/{orderId}/reject", orderId)
                                .queryParam("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_REJECTED.getCode()))
                .andDo(
                        document(
                                "order/reject-order",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                queryParameters(
                                        parameterWithName("storeId").description("가게 ID"),
                                        parameterWithName("reason")
                                                .optional()
                                                .description("거절 사유")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.orderStatus").description("변경된 주문 상태"),
                                        fieldWithPath("data.paymentStatus")
                                                .optional()
                                                .description("결제 상태"))));
    }

    @Test
    @DisplayName("실패 - 주문 거절 시 잘못된 상태")
    void rejectOrder_fail_invalidStatus() throws Exception {
        setSecurityContext(ownerDetails);

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderService.rejectOrder(userId, UserRole.OWNER, storeId, orderId, null))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(
                        post("/orders/{orderId}/reject", orderId)
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_STATUS.getCode()));
    }

    @Test
    @DisplayName("성공 - 조리 완료")
    void completeCookingOrder_success() throws Exception {
        setSecurityContext(ownerDetails);

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
                                .queryParam("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_COOKING_COMPLETED.getCode()))
                .andDo(
                        document(
                                "order/complete-cooking",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                queryParameters(parameterWithName("storeId").description("가게 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.orderStatus").description("변경된 주문 상태"),
                                        fieldWithPath("data.paymentStatus")
                                                .optional()
                                                .description("결제 상태"))));
    }

    @Test
    @DisplayName("실패 - 조리 완료 시 잘못된 상태")
    void completeCookingOrder_fail_invalidStatus() throws Exception {
        setSecurityContext(ownerDetails);

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderService.completeCookingOrder(userId, UserRole.OWNER, storeId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(
                        post("/orders/{orderId}/cooked", orderId)
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_STATUS.getCode()));
    }

    @Test
    @DisplayName("성공 - 배달 시작")
    void startDeliveryOrder_success() throws Exception {
        setSecurityContext(ownerDetails);

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
                                .queryParam("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_DELIVERING.getCode()))
                .andDo(
                        document(
                                "order/start-delivery",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                queryParameters(parameterWithName("storeId").description("가게 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.orderStatus").description("변경된 주문 상태"),
                                        fieldWithPath("data.paymentStatus")
                                                .optional()
                                                .description("결제 상태"))));
    }

    @Test
    @DisplayName("실패 - 배달 시작 시 잘못된 상태")
    void startDeliveryOrder_fail_invalidStatus() throws Exception {
        setSecurityContext(ownerDetails);

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderService.startDeliveryOrder(userId, UserRole.OWNER, storeId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(
                        post("/orders/{orderId}/delivering", orderId)
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_STATUS.getCode()));
    }

    @Test
    @DisplayName("성공 - 배달 완료")
    void completeDeliveryOrder_success() throws Exception {
        setSecurityContext(ownerDetails);

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
                                .queryParam("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_DELIVERED.getCode()))
                .andDo(
                        document(
                                "order/complete-delivery",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                queryParameters(parameterWithName("storeId").description("가게 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.orderStatus").description("변경된 주문 상태"),
                                        fieldWithPath("data.paymentStatus")
                                                .optional()
                                                .description("결제 상태"))));
    }

    @Test
    @DisplayName("실패 - 배달 완료 시 잘못된 상태")
    void completeDeliveryOrder_fail_invalidStatus() throws Exception {
        setSecurityContext(ownerDetails);

        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(orderService.completeDeliveryOrder(userId, UserRole.OWNER, storeId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(
                        post("/orders/{orderId}/delivered", orderId)
                                .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_STATUS.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 취소 요청")
    void cancelRequestOrder_success() throws Exception {
        setSecurityContext(customerDetails);

        UUID orderId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.CANCEL_REQUESTED)
                        .build();

        given(orderService.cancelRequestOrder(userId, UserRole.CUSTOMER, null, orderId, null))
                .willReturn(response);

        mockMvc.perform(post("/orders/{orderId}/cancel-request", orderId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_CANCEL_REQUESTED.getCode()))
                .andDo(
                        document(
                                "order/cancel-request-order",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                queryParameters(
                                        parameterWithName("storeId")
                                                .optional()
                                                .description("가게 ID (OWNER 역할에서 필요)"),
                                        parameterWithName("reason")
                                                .optional()
                                                .description("취소 사유")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.orderStatus").description("변경된 주문 상태"),
                                        fieldWithPath("data.paymentStatus")
                                                .optional()
                                                .description("결제 상태"))));
    }

    @Test
    @DisplayName("실패 - 주문 취소 요청 시 본인 주문 아님")
    void cancelRequestOrder_fail_forbidden() throws Exception {
        setSecurityContext(customerDetails);

        UUID orderId = UUID.randomUUID();

        given(orderService.cancelRequestOrder(userId, UserRole.CUSTOMER, null, orderId, null))
                .willThrow(new BusinessException(ErrorCode.ORDER_FORBIDDEN));

        mockMvc.perform(post("/orders/{orderId}/cancel-request", orderId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("성공 - 주문 취소")
    void completeCancelOrder_success() throws Exception {
        setSecurityContext(customerDetails);

        UUID orderId = UUID.randomUUID();

        OrderActionStatusResponse response =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.CANCELED)
                        .build();

        given(orderService.completeCancelOrder(userId, orderId)).willReturn(response);

        mockMvc.perform(post("/orders/{orderId}/cancel", orderId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_CANCELED.getCode()))
                .andDo(
                        document(
                                "order/complete-cancel-order",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.orderStatus").description("변경된 주문 상태"),
                                        fieldWithPath("data.paymentStatus")
                                                .optional()
                                                .description("결제 상태"))));
    }

    @Test
    @DisplayName("실패 - 주문 취소 시 CANCEL_REQUESTED 상태가 아님")
    void completeCancelOrder_fail_invalidStatus() throws Exception {
        setSecurityContext(customerDetails);

        UUID orderId = UUID.randomUUID();

        given(orderService.completeCancelOrder(userId, orderId))
                .willThrow(new BusinessException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(post("/orders/{orderId}/cancel", orderId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_INVALID_STATUS.getCode()));
    }
}
