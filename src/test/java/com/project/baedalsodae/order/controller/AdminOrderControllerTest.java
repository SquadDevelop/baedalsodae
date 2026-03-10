package com.project.baedalsodae.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.baedalsodae.auth.config.AuthConfig;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.OrderActionStatusResponse;
import com.project.baedalsodae.order.dto.response.OrderDetailResponse;
import com.project.baedalsodae.order.dto.response.OrderListResponse;
import com.project.baedalsodae.order.dto.response.OrderStatusResponse;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.user.entity.UserRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(AdminOrderController.class)
@Import(AuthConfig.class)
@AutoConfigureRestDocs
public class AdminOrderControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private OrderService orderService;

    @MockitoBean private JwtProvider jwtProvider;

    @MockitoBean private UserDetailsService userDetailsService;

    @MockitoBean private TokenRedisUtil tokenRedisUtil;

    private final String BASE_URL = "/admins/orders";

    @Test
    @DisplayName("성공 - 관리자 권한으로 전체 주문 목록 조회")
    void getOrders_ByAdmin_Success() throws Exception {
        UUID adminId = UUID.randomUUID();
        UserDetailsImpl admin = createUserDetails(adminId, UserRole.MANAGER);
        OrderListResponse mockResponse = OrderListResponse.empty();

        given(
                        orderService.getOrders(
                                eq(adminId),
                                eq(UserRole.MANAGER.getRole()),
                                any(OrderListRequest.class)))
                .willReturn(mockResponse);

        mockMvc.perform(get(BASE_URL).with(user(admin)).param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_LIST.getCode()))
                .andDo(
                        document(
                                "admin-order/get-orders",
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
                                                .description("가게 ID 필터"),
                                        parameterWithName("orderNo")
                                                .optional()
                                                .description("주문번호 검색어"),
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
                                        fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                        fieldWithPath("data.nextCursorCreatedAt")
                                                .optional()
                                                .description("다음 페이지 커서 - 기준 생성일시"),
                                        fieldWithPath("data.nextCursorId")
                                                .optional()
                                                .description("다음 페이지 커서 - 기준 주문 ID"))));
    }

    @Test
    @DisplayName("성공 - 관리자 권한으로 단일 주문 상세 조회")
    void getOrderDetail_ByAdmin_Success() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserDetailsImpl admin = createUserDetails(adminId, UserRole.MANAGER);
        OrderDetailResponse mockResponse =
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

        given(orderService.getOrderDetail(eq(adminId), eq(UserRole.MANAGER), eq(null), eq(orderId)))
                .willReturn(mockResponse);

        mockMvc.perform(get(BASE_URL + "/{orderId}", orderId).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_DETAIL.getCode()))
                .andDo(
                        document(
                                "admin-order/get-order-detail",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
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
    @DisplayName("성공 - 관리자 권한으로 주문 상태 전이 이력 조회")
    void getOrderStatusHistories_ByAdmin_Success() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserDetailsImpl admin = createUserDetails(adminId, UserRole.MANAGER);
        OrderStatusResponse mockResponse = OrderStatusResponse.builder().orderId(orderId).build();

        given(orderService.getOrderStatus(eq(adminId), eq(UserRole.MANAGER), eq(null), eq(orderId)))
                .willReturn(mockResponse);

        mockMvc.perform(get(BASE_URL + "/{orderId}/status-history", orderId).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_STATUS.getCode()))
                .andDo(
                        document(
                                "admin-order/get-order-status-history",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.currentStatus")
                                                .optional()
                                                .description("현재 주문 상태"),
                                        fieldWithPath("data.histories")
                                                .optional()
                                                .description("상태 전이 이력 목록"))));
    }

    @Test
    @DisplayName("실패 - 일반 사용자 권한으로 관리자 주문 API 접근 시 거부(403)")
    void getOrders_ByCustomer_Forbidden() throws Exception {
        UserDetailsImpl customer = createUserDetails(UUID.randomUUID(), UserRole.CUSTOMER);

        mockMvc.perform(get(BASE_URL).with(user(customer)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("성공 - 관리자 권한으로 주문 취소 요청")
    void cancelRequestOrder_ByAdmin_Success() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserDetailsImpl admin = createUserDetails(adminId, UserRole.MANAGER);
        OrderActionStatusResponse mockResponse =
                OrderActionStatusResponse.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.CANCEL_REQUESTED)
                        .build();

        given(
                        orderService.cancelRequestOrder(
                                eq(adminId),
                                eq(UserRole.MANAGER),
                                eq(null),
                                eq(orderId),
                                eq("관리자 취소 사유")))
                .willReturn(mockResponse);

        mockMvc.perform(
                        post(BASE_URL + "/{orderId}/cancel-request", orderId)
                                .with(user(admin))
                                .with(csrf())
                                .param("reason", "관리자 취소 사유"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_CANCEL_REQUESTED.getCode()))
                .andDo(
                        document(
                                "admin-order/cancel-request-order",
                                pathParameters(parameterWithName("orderId").description("주문 ID")),
                                queryParameters(
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

    private UserDetailsImpl createUserDetails(UUID userId, UserRole role) {
        return UserDetailsImpl.from(userId, "adminUser", "password", role, false);
    }
}
