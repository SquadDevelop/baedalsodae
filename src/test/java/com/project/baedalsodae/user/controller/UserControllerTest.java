package com.project.baedalsodae.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.config.AuthConfig;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.user.dto.request.UpdateUserRequest;
import com.project.baedalsodae.user.dto.response.UserDeleteResponse;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.service.UserService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
@Import(AuthConfig.class)
public class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;

    @MockitoBean private JwtProvider jwtProvider;

    @MockitoBean private UserDetailsService userDetailsService;

    @MockitoBean private TokenRedisUtil tokenRedisUtil;

    private final String BASE_URL = "/users";

    @Test
    @DisplayName("성공 - CUSTOMER 권한으로 본인 정보 조회")
    void getMe_ByCustomer_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDetailsImpl customer = createUserDetails(userId, UserRole.CUSTOMER);
        UserDetailResponse mockResponse = createUserDetailResponse(userId, "customer", UserRole.CUSTOMER);

        given(userService.getUser(userId)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get(BASE_URL + "/me")
                        .with(user(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_FOUND.getCode()));
    }

    @Test
    @DisplayName("성공 - 본인 정보 수정")
    void updateMe_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDetailsImpl customer = createUserDetails(userId, UserRole.CUSTOMER);
        UpdateUserRequest updateRequest = createUpdateUserRequest();
        UserDetailResponse mockResponse = createUserDetailResponse(userId, "customer", UserRole.CUSTOMER);

        given(userService.updateUser(eq(userId), any(UpdateUserRequest.class))).willReturn(mockResponse);

        // when & then
        mockMvc.perform(put(BASE_URL + "/me")
                        .with(user(customer))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_UPDATED.getCode()));
    }

    @Test
    @DisplayName("성공 - 회원 탈퇴 (소프트 딜리트)")
    void deleteMe_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDetailsImpl customer = createUserDetails(userId, UserRole.CUSTOMER);
        UserDeleteResponse mockResponse = createUserDeleteResponse(userId);

        given(userService.deleteUser(userId)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(delete(BASE_URL + "/me")
                        .with(user(customer))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_DELETED.getCode()));
    }

    // Helper Methods
    private UserDetailsImpl createUserDetails(UUID userId, UserRole role) {
        return UserDetailsImpl.from(userId, "testUser", "password", role, false);
    }

    private UserDetailResponse createUserDetailResponse(UUID userId, String username, UserRole role) {
        return UserDetailResponse.builder()
                .userId(userId)
                .username(username)
                .role(role)
                .addresses(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private UpdateUserRequest createUpdateUserRequest() {
        return UpdateUserRequest.builder()
                .nickname("newNickname")
                .phone("010-1234-5678")
                .email("new@test.com")
                .addresses(List.of())
                .build();
    }

    private UserDeleteResponse createUserDeleteResponse(UUID userId) {
        return UserDeleteResponse.builder()
                .id(userId)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
