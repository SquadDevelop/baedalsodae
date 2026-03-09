package com.project.baedalsodae.user.controller;

import static com.project.baedalsodae.user.fixture.UserFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.global.config.TestSecurityConfig;
import com.project.baedalsodae.user.dto.request.UpdateUserRequest;
import com.project.baedalsodae.user.dto.response.UserDeleteResponse;
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
@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureRestDocs
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
        UserDetailsImpl customer = createUserDetails(USER_ID, UserRole.CUSTOMER);
        UserDetailResponse mockResponse = createUserDetailResponse(USER_ID, "customerUser", UserRole.CUSTOMER);

        given(userService.getUser(USER_ID)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get(BASE_URL + "/me").with(user(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_FOUND.getCode()))
                .andDo(
                        document(
                                "user/get-me",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.userId").description("사용자 UUID"),
                                        fieldWithPath("data.username").description("사용자 아이디"),
                                        fieldWithPath("data.phone").description("전화번호"),
                                        fieldWithPath("data.email").description("이메일"),
                                        fieldWithPath("data.name").description("이름"),
                                        fieldWithPath("data.nickname").description("닉네임"),
                                        fieldWithPath("data.role").description("사용자 권한"),
                                        fieldWithPath("data.userMainAddressId")
                                                .description("대표 주소 ID")
                                                .optional(),
                                        fieldWithPath("data.addresses").description("주소 목록"),
                                        fieldWithPath("data.createdAt").description("생성일시"),
                                        fieldWithPath("data.updatedAt").description("수정일시").optional(),
                                        fieldWithPath("data.createdBy").description("생성자 ID").optional(),
                                        fieldWithPath("data.updatedBy").description("수정자 ID").optional())));
    }

    @Test
    @DisplayName("성공 - 본인 정보 수정")
    void updateMe_Success() throws Exception {
        // given
        UserDetailsImpl customer = createUserDetails(USER_ID, UserRole.CUSTOMER);
        UpdateUserRequest updateRequest = createUpdateUserRequest();
        UserDetailResponse mockResponse = createUserDetailResponse(USER_ID, "customerUser", UserRole.CUSTOMER);

        given(userService.updateUser(eq(USER_ID), any(UpdateUserRequest.class)))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(
                        put(BASE_URL + "/me")
                                .with(user(customer))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_UPDATED.getCode()))
                .andDo(
                        document(
                                "user/update-me",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("phone").description("변경할 전화번호").optional(),
                                        fieldWithPath("email").description("변경할 이메일").optional(),
                                        fieldWithPath("password")
                                                .description("변경할 비밀번호")
                                                .optional(),
                                        fieldWithPath("nickname")
                                                .description("변경할 닉네임")
                                                .optional(),
                                        fieldWithPath("addresses")
                                                .description("변경할 주소 목록")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.userId").description("사용자 UUID"),
                                        fieldWithPath("data.username").description("사용자 아이디"),
                                        fieldWithPath("data.phone").description("전화번호"),
                                        fieldWithPath("data.email").description("이메일"),
                                        fieldWithPath("data.name").description("이름"),
                                        fieldWithPath("data.nickname").description("닉네임"),
                                        fieldWithPath("data.role").description("사용자 권한"),
                                        fieldWithPath("data.userMainAddressId")
                                                .description("대표 주소 ID")
                                                .optional(),
                                        fieldWithPath("data.addresses").description("주소 목록"),
                                        fieldWithPath("data.createdAt").description("생성일시"),
                                        fieldWithPath("data.updatedAt").description("수정일시").optional(),
                                        fieldWithPath("data.createdBy").description("생성자 ID").optional(),
                                        fieldWithPath("data.updatedBy").description("수정자 ID").optional())));
    }

    @Test
    @DisplayName("성공 - 회원 탈퇴 (소프트 딜리트)")
    void deleteMe_Success() throws Exception {
        // given
        UserDetailsImpl customer = createUserDetails(USER_ID, UserRole.CUSTOMER);
        UserDeleteResponse mockResponse = createUserDeleteResponse(USER_ID);

        given(userService.deleteUser(USER_ID)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(delete(BASE_URL + "/me").with(user(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_DELETED.getCode()))
                .andDo(
                        document(
                                "user/delete-me",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.id").description("탈퇴 처리된 사용자 UUID"),
                                        fieldWithPath("data.deletedAt").description("탈퇴 일시"),
                                        fieldWithPath("data.deletedBy")
                                                .description("탈퇴 처리자 ID"))));
    }
}
