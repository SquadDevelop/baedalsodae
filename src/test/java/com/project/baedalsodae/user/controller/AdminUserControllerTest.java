package com.project.baedalsodae.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.config.AuthConfig;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.user.dto.request.CreateUserRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(AdminUserController.class)
@Import(AuthConfig.class)
public class AdminUserControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;

    @MockitoBean private JwtProvider jwtProvider;

    @MockitoBean private UserDetailsService userDetailsService;

    @MockitoBean private TokenRedisUtil tokenRedisUtil;

    private final String BASE_URL = "/admin";

    @Test
    @DisplayName("성공 - MASTER 권한으로 관리자 계정 생성 시도")
    void createManager_ByMaster_Success() throws Exception {
        // given
        UserDetailsImpl master = createUserDetails(UUID.randomUUID(), UserRole.MASTER);
        CreateUserRequest request = createManagerRequest("manager12");
        UserDetailResponse mockResponse = createUserDetailResponse(UUID.randomUUID(), "manager12", UserRole.MANAGER);

        given(userService.createUser(any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post(BASE_URL + "/managers")
                        .with(user(master))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_CREATED.getCode()));
    }

    @Test
    @DisplayName("실패 - MANAGER 권한으로 다른 관리자 계정 생성 시도 시 거부(403)")
    void createManager_ByManager_Forbidden() throws Exception {
        // given
        UserDetailsImpl manager = createUserDetails(UUID.randomUUID(), UserRole.MANAGER);
        CreateUserRequest request = createManagerRequest("manager12");

        // when & then
        mockMvc.perform(post(BASE_URL + "/managers")
                        .with(user(manager))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("성공 - MASTER 권한으로 관리자 목록 페이징 조회")
    void getManagers_ByMaster_Success() throws Exception {
        // given
        UserDetailsImpl master = createUserDetails(UUID.randomUUID(), UserRole.MASTER);
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserDetailResponse> mockPage = new PageImpl<>(List.of(), pageable, 0);

        given(userService.getUsers(eq(UserRole.MANAGER), any(), any(Pageable.class))).willReturn(mockPage);

        // when & then
        mockMvc.perform(get(BASE_URL + "/managers")
                        .with(user(master)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_FOUND.getCode()))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("성공 - MANAGER 권한으로 본인 정보 조회")
    void getAdminDetail_ByManager_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDetailsImpl userDetails = createUserDetails(userId, UserRole.MANAGER);
        UserDetailResponse mockResponse = createUserDetailResponse(userId, "admin", UserRole.MANAGER);

        given(userService.getUser(userId)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get(BASE_URL + "/me")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_FOUND.getCode()));
    }

    // Helper Methods
    private UserDetailsImpl createUserDetails(UUID userId, UserRole role) {
        return UserDetailsImpl.from(userId, "testUser", "password", role, false);
    }

    private CreateUserRequest createManagerRequest(String username) {
        return CreateUserRequest.builder()
                .username(username)
                .phone("010-1234-5678")
                .email("test@test.com")
                .password("Password123!")
                .name("관리자")
                .nickname("어드민")
                .role(UserRole.MANAGER)
                .build();
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
}
