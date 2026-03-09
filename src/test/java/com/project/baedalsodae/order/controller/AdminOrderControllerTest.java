package com.project.baedalsodae.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.baedalsodae.auth.config.AuthConfig;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.order.dto.request.OrderListRequest;
import com.project.baedalsodae.order.dto.response.OrderDetailResponse;
import com.project.baedalsodae.order.dto.response.OrderListResponse;
import com.project.baedalsodae.order.dto.response.OrderStatusResponse;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(AdminOrderController.class)
@Import(AuthConfig.class)
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
        // given
        UUID adminId = UUID.randomUUID();
        UserDetailsImpl admin = createUserDetails(adminId, UserRole.MANAGER);
        OrderListResponse mockResponse = OrderListResponse.empty();

        given(
                        orderService.getOrders(
                                eq(adminId),
                                eq(UserRole.MANAGER.getRole()),
                                any(OrderListRequest.class)))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get(BASE_URL).with(user(admin)).param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_LIST.getCode()));
    }

    @Test
    @DisplayName("성공 - 관리자 권한으로 단일 주문 상세 조회")
    void getOrderDetail_ByAdmin_Success() throws Exception {
        // given
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserDetailsImpl admin = createUserDetails(adminId, UserRole.MANAGER);
        OrderDetailResponse mockResponse = OrderDetailResponse.builder().orderId(orderId).build();

        given(orderService.getOrderDetail(eq(adminId), eq(UserRole.MANAGER), eq(null), eq(orderId)))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get(BASE_URL + "/" + orderId).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_DETAIL.getCode()));
    }

    @Test
    @DisplayName("성공 - 관리자 권한으로 주문 상태 전이 이력 조회")
    void getOrderStatusHistories_ByAdmin_Success() throws Exception {
        // given
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UserDetailsImpl admin = createUserDetails(adminId, UserRole.MANAGER);
        OrderStatusResponse mockResponse = OrderStatusResponse.builder().orderId(orderId).build();

        given(orderService.getOrderStatus(eq(adminId), eq(UserRole.MANAGER), eq(null), eq(orderId)))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get(BASE_URL + "/" + orderId + "/status-history").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.ORDER_STATUS.getCode()));
    }

    @Test
    @DisplayName("실패 - 일반 사용자 권한으로 관리자 주문 API 접근 시 거부(403)")
    void getOrders_ByCustomer_Forbidden() throws Exception {
        // given
        UserDetailsImpl customer = createUserDetails(UUID.randomUUID(), UserRole.CUSTOMER);

        // when & then
        mockMvc.perform(get(BASE_URL).with(user(customer)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    // Helper Method
    private UserDetailsImpl createUserDetails(UUID userId, UserRole role) {
        return UserDetailsImpl.from(userId, "adminUser", "password", role, false);
    }
}
