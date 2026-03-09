package com.project.baedalsodae.auth.controller;

import static com.project.baedalsodae.auth.fixture.AuthFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.dto.request.LoginRequest;
import com.project.baedalsodae.auth.dto.request.ReissueRequest;
import com.project.baedalsodae.auth.dto.request.SignupRequest;
import com.project.baedalsodae.auth.dto.response.LoginResponse;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.auth.service.AuthService;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.global.config.TestSecurityConfig;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureRestDocs
public class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;

    @MockitoBean private AuthService authService;

    @MockitoBean private JwtProvider jwtProvider;

    @MockitoBean private UserDetailsService userDetailsService;

    @MockitoBean private TokenRedisUtil tokenRedisUtil;

    private final String BASE_URL = "/auth";

    @Test
    @DisplayName("성공 - 회원가입")
    void signup_Success() throws Exception {
        SignupRequest request = createSignupRequest();
        UUID mockUserId = UUID.randomUUID();
        UserDetailResponse mockDetail = createSignupUserDetailResponse(mockUserId);

        given(userService.createUser(any())).willReturn(mockDetail);

        mockMvc.perform(
                        post(BASE_URL + "/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_CREATED.getCode()))
                .andDo(
                        document(
                                "auth/signup",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("username").description("아이디 (4-10자 소문자/숫자)"),
                                        fieldWithPath("phone").description("전화번호 (010-XXXX-XXXX)"),
                                        fieldWithPath("email").description("이메일"),
                                        fieldWithPath("password").description("비밀번호 (8-15자 영문/숫자/특수문자)"),
                                        fieldWithPath("name").description("실명"),
                                        fieldWithPath("nickname").description("닉네임"),
                                        fieldWithPath("role").description("사용자 권한 (CUSTOMER, OWNER 등)"),
                                        fieldWithPath("address").description("기본 주소 정보"),
                                        fieldWithPath("address.roadAddress").description("도로명 주소"),
                                        fieldWithPath("address.detailAddress").description("상세 주소"),
                                        fieldWithPath("address.sidoCode").description("시도 코드"),
                                        fieldWithPath("address.sidoName").description("시도 이름"),
                                        fieldWithPath("address.sigunguCode").description("시군구 코드"),
                                        fieldWithPath("address.sigunguName").description("시군구 이름"),
                                        fieldWithPath("address.dongCode").description("동 코드"),
                                        fieldWithPath("address.dongName").description("동 이름"),
                                        fieldWithPath("address.description").description("장소 설명 (선택)").optional()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.userId").description("생성된 사용자 UUID"),
                                        fieldWithPath("data.username").description("사용자 아이디"),
                                        fieldWithPath("data.nickname").description("닉네임"))));
    }

    @Test
    @DisplayName("실패 - 이미 존재하는 아이디로 회원가입 시도")
    void signup_Fail_DuplicatedUsername() throws Exception {
        SignupRequest request = createSignupRequest();
        given(userService.createUser(any())).willThrow(new BusinessException(ErrorCode.USER_DUPLICATED_USERNAME));

        mockMvc.perform(
                        post(BASE_URL + "/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(
                        document(
                                "auth/signup-fail-username",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("에러 발생 시각"),
                                        fieldWithPath("data").description("응답 데이터 (null)").optional())));
    }

    @Test
    @DisplayName("실패 - 이미 존재하는 이메일로 회원가입 시도")
    void signup_Fail_DuplicatedEmail() throws Exception {
        SignupRequest request = createSignupRequest();
        given(userService.createUser(any())).willThrow(new BusinessException(ErrorCode.USER_DUPLICATED_EMAIL));

        mockMvc.perform(
                        post(BASE_URL + "/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(
                        document(
                                "auth/signup-fail-email",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("에러 발생 시각"),
                                        fieldWithPath("data").description("응답 데이터 (null)").optional())));
    }

    @Test
    @DisplayName("성공 - 로그인")
    void login_Success() throws Exception {
        LoginRequest request = createLoginRequest();
        LoginResponse response = createLoginResponse();
        given(authService.login(any())).willReturn(response);

        mockMvc.perform(
                        post(BASE_URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.LOGIN_SUCCESS.getCode()))
                .andDo(
                        document(
                                "auth/login",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("username").description("아이디"),
                                        fieldWithPath("password").description("비밀번호")),
                                responseHeaders(
                                        headerWithName("Authorization").description("Access Token (Bearer)")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.accessToken").description("Access Token"),
                                        fieldWithPath("data.refreshToken").description("Refresh Token"))));
    }

    @Test
    @DisplayName("실패 - 잘못된 비밀번호로 로그인 시도")
    void login_Fail_InvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest("tester123", "wrongPassword!");
        given(authService.login(any())).willThrow(new BusinessException(ErrorCode.LOGIN_FAILED));

        mockMvc.perform(
                        post(BASE_URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(
                        document(
                                "auth/login-fail",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("에러 발생 시각"),
                                        fieldWithPath("data").description("응답 데이터 (null)").optional())));
    }

    @Test
    @DisplayName("성공 - 토큰 재발급")
    void reissue_Success() throws Exception {
        ReissueRequest request = createReissueRequest();
        LoginResponse response = createLoginResponse();
        given(authService.reissue(any())).willReturn(response);

        mockMvc.perform(
                        post(BASE_URL + "/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "auth/reissue",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("refreshToken").description("유효한 Refresh Token")),
                                responseHeaders(
                                        headerWithName("Authorization").description("새로운 Access Token (Bearer)")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.accessToken").description("새로운 Access Token"),
                                        fieldWithPath("data.refreshToken").description("새로운 Refresh Token"))));
    }

    @Test
    @DisplayName("실패 - 만료된 리프레시 토큰으로 재발급 시도")
    void reissue_Fail_ExpiredToken() throws Exception {
        ReissueRequest request = new ReissueRequest("expired-token");
        given(authService.reissue(any())).willThrow(new BusinessException(ErrorCode.JWT_EXPIRED));

        mockMvc.perform(
                        post(BASE_URL + "/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(
                        document(
                                "auth/reissue-fail-expired",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("에러 발생 시각"),
                                        fieldWithPath("data").description("응답 데이터 (null)").optional())));
    }

    @Test
    @DisplayName("실패 - 유효하지 않은 리프레시 토큰으로 재발급 시도")
    void reissue_Fail_InvalidToken() throws Exception {
        ReissueRequest request = new ReissueRequest("invalid-token");
        given(authService.reissue(any())).willThrow(new BusinessException(ErrorCode.JWT_INVALID));

        mockMvc.perform(
                        post(BASE_URL + "/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(
                        document(
                                "auth/reissue-fail-invalid",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("에러 발생 시각"),
                                        fieldWithPath("data").description("응답 데이터 (null)").optional())));
    }

    @Test
    @DisplayName("성공 - 로그아웃")
    void logout_Success() throws Exception {
        UserDetailsImpl user = UserDetailsImpl.from(UUID.randomUUID(), "tester", "pass", UserRole.CUSTOMER, false);

        mockMvc.perform(
                        post(BASE_URL + "/logout")
                                .with(user(user))
                                .header("Authorization", "Bearer " + ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "auth/logout",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data").description("응답 데이터 (null)").optional())));
    }

    @Test
    @DisplayName("실패 - 인증 토큰 없이 로그아웃 시도")
    void logout_Fail_Unauthorized() throws Exception {
        doThrow(new BusinessException(ErrorCode.UNAUTHORIZED)).when(authService).logout(any());

        mockMvc.perform(post(BASE_URL + "/logout"))
                .andExpect(status().isUnauthorized())
                .andDo(
                        document(
                                "auth/logout-fail-unauthorized",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("에러 발생 시각"),
                                        fieldWithPath("data").description("응답 데이터 (null)").optional())));
    }
}
