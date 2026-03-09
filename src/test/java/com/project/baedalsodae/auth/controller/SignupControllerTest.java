package com.project.baedalsodae.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.dto.request.SignupRequest;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.auth.service.AuthService;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SignupControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;

    @MockitoBean private AuthService authService;

    @MockitoBean private JwtProvider jwtProvider;

    @MockitoBean private UserDetailsService userDetailsService;

    @MockitoBean private TokenRedisUtil tokenRedisUtil;

    private final String BASE_URL = "/auth";

    @Test
    @DisplayName("성공 - 회원가입 서비스 호출 및 응답 검증")
    void signupSuccess() throws Exception {
        // given
        CreateUserAddressRequest addressRequest = CreateUserAddressRequest.from(
                "11", "서울", "110", "강남구", "11010", "역삼동", "도로명", "상세주소", "집"
        );
        SignupRequest request =
                new SignupRequest(
                        "tester123",
                        "010-1234-5678",
                        "test@test.com",
                        "Password123!",
                        "테스터",
                        "닉네임",
                        UserRole.CUSTOMER,
                        addressRequest);
        
        UUID mockUserId = UUID.randomUUID();
        UserDetailResponse mockDetail =
                UserDetailResponse.builder()
                        .userId(mockUserId)
                        .username("tester123")
                        .nickname("닉네임")
                        .build();

        given(userService.createUser(any())).willReturn(mockDetail);

        // when & then
        mockMvc.perform(
                        post(BASE_URL + "/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_CREATED.getCode()))
                .andExpect(jsonPath("$.data.userId").value(mockUserId.toString()))
                .andExpect(jsonPath("$.data.username").value("tester123"))
                .andExpect(jsonPath("$.data.nickname").value("닉네임"));
    }

    @Test
    @DisplayName("실패 - 유효하지 않은 아이디 형식으로 회원가입 시도")
    void signupFailInvalidUsername() throws Exception {
        // given
        CreateUserAddressRequest addressRequest = CreateUserAddressRequest.from(
                "11", "서울", "110", "강남구", "11010", "역삼동", "도로명", "상세주소", "집"
        );
        SignupRequest request =
                new SignupRequest(
                        "bad",
                        "010-1234-5678",
                        "test@test.com",
                        "Password123!",
                        "테스터",
                        "닉네임",
                        UserRole.CUSTOMER,
                        addressRequest);

        // when & then
        mockMvc.perform(
                        post(BASE_URL + "/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
