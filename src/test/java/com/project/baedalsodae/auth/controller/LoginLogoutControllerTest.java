package com.project.baedalsodae.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.config.AuthConfig;
import com.project.baedalsodae.auth.dto.request.LoginRequest;
import com.project.baedalsodae.auth.dto.response.LoginResponse;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.auth.service.AuthService;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@Import(AuthConfig.class)
public class LoginLogoutControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;

    @MockitoBean private AuthService authService;

    @MockitoBean private JwtProvider jwtProvider;

    @MockitoBean private UserDetailsService userDetailsService;

    @MockitoBean private TokenRedisUtil tokenRedisUtil;

    private final String CONTEXT_PATH = "/api/v1";
    private final String BASE_URL = CONTEXT_PATH + "/auth";

    @Test
    @DisplayName("성공 - 로그인 시 컨트롤러가 작동하여 헤더에 토큰을 담아준다")
    void loginSuccess() throws Exception {
        // given
        LoginRequest request = new LoginRequest("tester123", "Password123!");
        LoginResponse response = LoginResponse.from("Bearer mock-token", "mock-refresh-token");
        given(authService.login(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        post(BASE_URL + "/login")
                                .contextPath(CONTEXT_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer mock-token"));
    }

    @Test
    @DisplayName("실패 - 비밀번호가 틀리면 401 Unauthorized를 반환한다")
    void loginFail() throws Exception {
        // given
        LoginRequest request = new LoginRequest("tester123", "wrong-password");
        given(authService.login(any())).willThrow(new BusinessException(ErrorCode.LOGIN_FAILED));

        // when & then
        mockMvc.perform(
                        post(BASE_URL + "/login")
                                .contextPath(CONTEXT_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("성공 - 유효한 리프레시 토큰으로 재발급 요청 시 200 OK를 반환한다")
    void reissueSuccess() throws Exception {
        // given
        com.project.baedalsodae.auth.dto.request.ReissueRequest request = 
                new com.project.baedalsodae.auth.dto.request.ReissueRequest("valid-refresh-token");
        LoginResponse response = LoginResponse.from("Bearer new-access-token", "new-refresh-token");
        given(authService.reissue(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        post(BASE_URL + "/reissue")
                                .contextPath(CONTEXT_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer new-access-token"));
    }

    @Test
    @DisplayName("실패 - 리프레시 토큰이 비어있으면 400 Bad Request를 반환한다")
    void reissueFailEmptyToken() throws Exception {
        // given
        com.project.baedalsodae.auth.dto.request.ReissueRequest request = 
                new com.project.baedalsodae.auth.dto.request.ReissueRequest("");

        // when & then
        mockMvc.perform(
                        post(BASE_URL + "/reissue")
                                .contextPath(CONTEXT_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공 - 인증된 사용자는 로그아웃할 수 있다")
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    void logoutSuccess() throws Exception {
        // given
        given(jwtProvider.resolveToken(any())).willReturn("mock-token");

        // when & then
        mockMvc.perform(
                        post(BASE_URL + "/logout")
                                .contextPath(CONTEXT_PATH)
                                .header("Authorization", "Bearer mock-token")
                                .with(csrf()))
                .andExpect(status().isOk());
    }
}
