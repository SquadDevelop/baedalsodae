package com.project.baedalsodae.allowedRegion.controller;

import static com.project.baedalsodae.allowedRegion.fixture.AllowedRegionFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.allowedRegion.dto.AllowedRegionCursorRequest;
import com.project.baedalsodae.allowedRegion.dto.AllowedRegionPageResponse;
import com.project.baedalsodae.allowedRegion.dto.AllowedRegionRequestDto;
import com.project.baedalsodae.allowedRegion.service.AllowedRegionService;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AllowedRegionController.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class AllowedRegionControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AllowedRegionService allowedRegionService;

    private UserDetailsImpl managerDetails;

    @BeforeEach
    void setUp() {
        managerDetails = createManagerUserDetails();
    }

    @Test
    @DisplayName("허용 지역 목록 조회 - 성공")
    void getAllowedRegions_success() throws Exception {
        AllowedRegionPageResponse pageResponse = createAllowedRegionPageResponse();

        given(allowedRegionService.getAllowedRegions(any(AllowedRegionCursorRequest.class)))
                .willReturn(pageResponse);

        mockMvc.perform(
                        get("/allowed-regions")
                                .with(user(managerDetails))
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.allowedRegionCount").value(1))
                .andExpect(jsonPath("$.data.allowedRegions[0].sidoName").value(SIDO_NAME))
                .andDo(
                        document(
                                "allowed-region/get-list",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                queryParameters(
                                        parameterWithName("lastId")
                                                .description("마지막 커서 ID (UUID)")
                                                .optional(),
                                        parameterWithName("sortType")
                                                .description(
                                                        "정렬 타입 (LATEST / SIDO_NAME / ACTIVE, 기본값: ACTIVE)")
                                                .optional(),
                                        parameterWithName("lastCreatedAt")
                                                .description("마지막 생성일시 (LATEST / ACTIVE 정렬 시)")
                                                .optional(),
                                        parameterWithName("lastSidoName")
                                                .description("마지막 시도 이름 (SIDO_NAME 정렬 시)")
                                                .optional(),
                                        parameterWithName("lastSigunguName")
                                                .description("마지막 시군구 이름 (SIDO_NAME 정렬 시)")
                                                .optional(),
                                        parameterWithName("lastIsActive")
                                                .description("마지막 활성화 여부 (ACTIVE 정렬 시)")
                                                .optional(),
                                        parameterWithName("sidoCode")
                                                .description("시도 코드 필터")
                                                .optional(),
                                        parameterWithName("activeFilter")
                                                .description("활성화 여부 필터 (true / false)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (기본값 10)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                        fieldWithPath("data.allowedRegionCount")
                                                .description("현재 페이지 지역 수"),
                                        fieldWithPath("data.lastCursorId").description("마지막 커서 ID"),
                                        fieldWithPath("data.allowedRegions[].allowedRegionId")
                                                .description("허용 지역 UUID"),
                                        fieldWithPath("data.allowedRegions[].sidoCode")
                                                .description("시도 코드"),
                                        fieldWithPath("data.allowedRegions[].sidoName")
                                                .description("시도 이름"),
                                        fieldWithPath("data.allowedRegions[].sigunguCode")
                                                .description("시군구 코드"),
                                        fieldWithPath("data.allowedRegions[].sigunguName")
                                                .description("시군구 이름"),
                                        fieldWithPath("data.allowedRegions[].isActive")
                                                .description("활성화 여부"))));
    }

    @Test
    @DisplayName("허용 지역 등록 - 성공")
    void createAllowedRegion_success() throws Exception {
        AllowedRegionRequestDto request =
                new AllowedRegionRequestDto(SIDO_CODE, SIDO_NAME, SIGUNGU_CODE, SIGUNGU_NAME);

        given(allowedRegionService.createAllowedRegion(any(AllowedRegionRequestDto.class)))
                .willReturn(createResponseDto());

        mockMvc.perform(
                        post("/allowed-regions")
                                .with(user(managerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sidoName").value(SIDO_NAME))
                .andExpect(jsonPath("$.data.sigunguName").value(SIGUNGU_NAME))
                .andDo(
                        document(
                                "allowed-region/create",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("sidoCode").description("시도 코드"),
                                        fieldWithPath("sidoName").description("시도 이름"),
                                        fieldWithPath("sigunguCode").description("시군구 코드"),
                                        fieldWithPath("sigunguName").description("시군구 이름")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.allowedRegionId")
                                                .description("생성된 허용 지역 UUID"),
                                        fieldWithPath("data.sidoCode").description("시도 코드"),
                                        fieldWithPath("data.sidoName").description("시도 이름"),
                                        fieldWithPath("data.sigunguCode").description("시군구 코드"),
                                        fieldWithPath("data.sigunguName").description("시군구 이름"),
                                        fieldWithPath("data.isActive").description("활성화 여부"))));
    }

    @Test
    @DisplayName("허용 지역 등록 - 실패 (중복 시군구 코드 409)")
    void createAllowedRegion_conflict() throws Exception {
        AllowedRegionRequestDto request =
                new AllowedRegionRequestDto(SIDO_CODE, SIDO_NAME, SIGUNGU_CODE, SIGUNGU_NAME);

        given(allowedRegionService.createAllowedRegion(any(AllowedRegionRequestDto.class)))
                .willThrow(new BusinessException(ErrorCode.ALLOWED_REGION_CODE_DUPLICATED));

        mockMvc.perform(
                        post("/allowed-regions")
                                .with(user(managerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.ALLOWED_REGION_CODE_DUPLICATED.getCode()));
    }

    @Test
    @DisplayName("허용 지역 활성화/비활성화 토글 - 성공 (활성화)")
    void toggleAllowedRegionActivation_activate_success() throws Exception {
        given(allowedRegionService.toggleAllowedRegionActivation(eq(ALLOWED_REGION_ID), eq(true)))
                .willReturn(createResponseDto());

        mockMvc.perform(
                        patch(
                                        "/allowed-regions/{allowedRegionId}/activation?activation=true",
                                        ALLOWED_REGION_ID)
                                .with(user(managerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andDo(
                        document(
                                "allowed-region/toggle-activation",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("allowedRegionId")
                                                .description("허용 지역 UUID")),
                                queryParameters(
                                        parameterWithName("activation")
                                                .description("활성화 여부 (true: 활성화, false: 비활성화)")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.allowedRegionId")
                                                .description("허용 지역 UUID"),
                                        fieldWithPath("data.sidoCode").description("시도 코드"),
                                        fieldWithPath("data.sidoName").description("시도 이름"),
                                        fieldWithPath("data.sigunguCode").description("시군구 코드"),
                                        fieldWithPath("data.sigunguName").description("시군구 이름"),
                                        fieldWithPath("data.isActive").description("변경된 활성화 여부"))));
    }

    @Test
    @DisplayName("허용 지역 활성화/비활성화 토글 - 성공 (비활성화)")
    void toggleAllowedRegionActivation_deactivate_success() throws Exception {
        given(allowedRegionService.toggleAllowedRegionActivation(eq(ALLOWED_REGION_ID), eq(false)))
                .willReturn(createInactiveResponseDto());

        mockMvc.perform(
                        patch(
                                        "/allowed-regions/{allowedRegionId}/activation?activation=false",
                                        ALLOWED_REGION_ID)
                                .with(user(managerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @DisplayName("허용 지역 활성화/비활성화 토글 - 실패 (존재하지 않는 지역 404)")
    void toggleAllowedRegionActivation_notFound() throws Exception {
        UUID unknownId = UUID.randomUUID();

        given(allowedRegionService.toggleAllowedRegionActivation(eq(unknownId), eq(true)))
                .willThrow(new BusinessException(ErrorCode.ALLOWED_REGION_NOT_FOUND));

        mockMvc.perform(
                        patch(
                                        "/allowed-regions/{allowedRegionId}/activation?activation=true",
                                        unknownId)
                                .with(user(managerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ALLOWED_REGION_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("허용 지역 목록 조회 - 성공 (빈 목록)")
    void getAllowedRegions_empty() throws Exception {
        given(allowedRegionService.getAllowedRegions(any(AllowedRegionCursorRequest.class)))
                .willReturn(createEmptyPageResponse());

        mockMvc.perform(
                        get("/allowed-regions")
                                .with(user(managerDetails))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.allowedRegionCount").value(0))
                .andExpect(jsonPath("$.data.allowedRegions").isEmpty());
    }
}