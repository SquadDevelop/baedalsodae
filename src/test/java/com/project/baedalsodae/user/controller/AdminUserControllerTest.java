package com.project.baedalsodae.user.controller;

import static com.project.baedalsodae.user.fixture.UserFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.global.config.TestSecurityConfig;
import com.project.baedalsodae.user.dto.request.CreateUserRequest;
import com.project.baedalsodae.user.dto.request.UserSearchRequest;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
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
@Import(TestSecurityConfig.class)
@AutoConfigureRestDocs
public class AdminUserControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;

    @MockitoBean private JwtProvider jwtProvider;

    @MockitoBean private UserDetailsService userDetailsService;

    @MockitoBean private TokenRedisUtil tokenRedisUtil;

    private final String BASE_URL = "/admins";

    @Test
    @DisplayName("성공 - MASTER 권한으로 관리자 계정 생성 시도")
    void createManager_ByMaster_Success() throws Exception {
        // given
        UserDetailsImpl master = createUserDetails(MASTER_ID, UserRole.MASTER);
        CreateUserRequest request = createManagerRequest("managerUser");
        UserDetailResponse mockResponse = createUserDetailResponse(MANAGER_ID, "managerUser", UserRole.MANAGER);

        given(userService.createUser(any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(
                        post(BASE_URL + "/managers")
                                .with(user(master))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_CREATED.getCode()))
                .andDo(
                        document(
                                "admin-user/create-manager",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("username").description("관리자 아이디"),
                                        fieldWithPath("password").description("비밀번호"),
                                        fieldWithPath("email").description("이메일"),
                                        fieldWithPath("name").description("이름"),
                                        fieldWithPath("phone").description("전화번호"),
                                        fieldWithPath("nickname").description("닉네임"),
                                        fieldWithPath("role").description("사용자 권한 (MANAGER)"),
                                        fieldWithPath("address").description("주소 정보"),
                                        fieldWithPath("address.sidoCode").description("시도 코드"),
                                        fieldWithPath("address.sidoName").description("시도 이름"),
                                        fieldWithPath("address.sigunguCode").description("시군구 코드"),
                                        fieldWithPath("address.sigunguName").description("시군구 이름"),
                                        fieldWithPath("address.dongCode").description("동 코드"),
                                        fieldWithPath("address.dongName").description("동 이름"),
                                        fieldWithPath("address.roadAddress").description("도로명 주소"),
                                        fieldWithPath("address.detailAddress").description("상세 주소"),
                                        fieldWithPath("address.description")
                                                .description("장소 설명")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.userId").description("사용자 UUID"),
                                        fieldWithPath("data.username").description("사용자 아이디"),
                                        fieldWithPath("data.email").description("이메일"),
                                        fieldWithPath("data.name").description("이름"),
                                        fieldWithPath("data.nickname").description("닉네임"),
                                        fieldWithPath("data.role").description("사용자 권한"),
                                        fieldWithPath("data.createdAt").description("생성일시"),
                                        fieldWithPath("data.createdBy")
                                                .description("생성자 ID")
                                                .optional(),
                                        fieldWithPath("data.phone").description("전화번호").optional(),
                                        fieldWithPath("data.address")
                                                .description("주소 정보")
                                                .optional(),
                                        fieldWithPath("data.updatedAt")
                                                .description("수정일시")
                                                .optional(),
                                        fieldWithPath("data.updatedBy")
                                                .description("수정자 ID")
                                                .optional())));
    }

    @Test
    @DisplayName("성공 - MASTER 권한으로 관리자 목록 페이징 조회")
    void getManagers_ByMaster_Success() throws Exception {
        // given
        UserDetailsImpl master = createUserDetails(MASTER_ID, UserRole.MASTER);
        Pageable pageable = PageRequest.of(0, 10);
        UserDetailResponse managerResponse = createUserDetailResponse(MANAGER_ID, "managerUser", UserRole.MANAGER);
        Page<UserDetailResponse> mockPage = new PageImpl<>(List.of(managerResponse), pageable, 1);

        given(
                        userService.getUsers(
                                eq(UserRole.MANAGER),
                                any(UserSearchRequest.class),
                                any(Pageable.class)))
                .willReturn(mockPage);

        // when & then
        mockMvc.perform(
                        get(BASE_URL + "/managers")
                                .with(user(master))
                                .param("page", "0")
                                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_FOUND.getCode()))
                .andDo(
                        document(
                                "admin-user/get-managers",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                queryParameters(
                                        parameterWithName("page")
                                                .description("페이지 번호 (0-based)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기")
                                                .optional()),
                                relaxedResponseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.content[].userId")
                                                .description("사용자 UUID"),
                                        fieldWithPath("data.content[].username")
                                                .description("사용자 아이디"),
                                        fieldWithPath("data.content[].email")
                                                .description("이메일"),
                                        fieldWithPath("data.content[].name").description("이름"),
                                        fieldWithPath("data.content[].nickname")
                                                .description("닉네임"),
                                        fieldWithPath("data.content[].role")
                                                .description("사용자 권한"),
                                        fieldWithPath("data.content[].createdAt")
                                                .description("생성일시"),
                                        fieldWithPath("data.totalElements").description("전체 요소 수"),
                                        fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                        fieldWithPath("data.size").description("페이지 크기"),
                                        fieldWithPath("data.number").description("현재 페이지 번호"))));
    }

    @Test
    @DisplayName("성공 - MASTER 권한으로 특정 관리자 상세 정보 조회")
    void getManagerDetail_ByMaster_Success() throws Exception {
        // given
        UserDetailsImpl master = createUserDetails(MASTER_ID, UserRole.MASTER);
        UserDetailResponse mockResponse = createUserDetailResponse(MANAGER_ID, "managerUser", UserRole.MANAGER);

        given(userService.getUser(MANAGER_ID)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get(BASE_URL + "/managers/{managerId}", MANAGER_ID).with(user(master)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_FOUND.getCode()))
                .andDo(
                        document(
                                "admin-user/get-manager-detail",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("managerId").description("조회할 관리자 UUID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.userId").description("사용자 UUID"),
                                        fieldWithPath("data.username").description("사용자 아이디"),
                                        fieldWithPath("data.email").description("이메일"),
                                        fieldWithPath("data.name").description("이름"),
                                        fieldWithPath("data.nickname").description("닉네임"),
                                        fieldWithPath("data.role").description("사용자 권한"),
                                        fieldWithPath("data.createdAt").description("생성일시"),
                                        fieldWithPath("data.createdBy")
                                                .description("생성자 ID")
                                                .optional(),
                                        fieldWithPath("data.phone").description("전화번호").optional(),
                                        fieldWithPath("data.address")
                                                .description("주소 정보")
                                                .optional(),
                                        fieldWithPath("data.updatedAt")
                                                .description("수정일시")
                                                .optional(),
                                        fieldWithPath("data.updatedBy")
                                                .description("수정자 ID")
                                                .optional())));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 관리자 상세 정보 조회")
    void getManagerDetail_Fail_NotFound() throws Exception {
        // given
        UserDetailsImpl master = createUserDetails(MASTER_ID, UserRole.MASTER);
        UUID invalidManagerId = UUID.randomUUID();

        given(userService.getUser(invalidManagerId))
                .willThrow(new com.project.baedalsodae.global.common.BusinessException(com.project.baedalsodae.global.common.ErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(get(BASE_URL + "/managers/{managerId}", invalidManagerId).with(user(master)))
                .andExpect(status().isNotFound())
                .andDo(
                        document(
                                "admin-user/get-manager-detail-fail-notfound",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("managerId").description("조회할 관리자 UUID")),
                                responseFields(
                                        fieldWithPath("code").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("에러 발생 시각"),
                                        fieldWithPath("data").description("응답 데이터 (null)").optional())));
    }

    @Test
    @DisplayName("성공 - MANAGER 권한으로 본인 정보 조회")
    void getAdminDetail_ByManager_Success() throws Exception {
        // given
        UserDetailsImpl userDetails = createUserDetails(MANAGER_ID, UserRole.MANAGER);
        UserDetailResponse mockResponse = createUserDetailResponse(MANAGER_ID, "managerUser", UserRole.MANAGER);

        given(userService.getUser(MANAGER_ID)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get(BASE_URL + "/me").with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_FOUND.getCode()))
                .andDo(
                        document(
                                "admin-user/get-me",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.userId").description("사용자 UUID"),
                                        fieldWithPath("data.username").description("사용자 아이디"),
                                        fieldWithPath("data.email").description("이메일"),
                                        fieldWithPath("data.name").description("이름"),
                                        fieldWithPath("data.nickname").description("닉네임"),
                                        fieldWithPath("data.role").description("사용자 권한"),
                                        fieldWithPath("data.createdAt").description("생성일시"),
                                        fieldWithPath("data.createdBy")
                                                .description("생성자 ID")
                                                .optional(),
                                        fieldWithPath("data.phone").description("전화번호").optional(),
                                        fieldWithPath("data.address")
                                                .description("주소 정보")
                                                .optional(),
                                        fieldWithPath("data.updatedAt")
                                                .description("수정일시")
                                                .optional(),
                                        fieldWithPath("data.updatedBy")
                                                .description("수정자 ID")
                                                .optional())));
    }
}
