package com.project.baedalsodae.store.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.dto.AuditInfoResponse;
import com.project.baedalsodae.store.dto.request.storeCategory.CreateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.request.storeCategory.UpdateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.response.storeCategory.StoreCategoryDetailResponse;
import com.project.baedalsodae.store.dto.response.storeCategory.StoreCategoryListResponse;
import com.project.baedalsodae.store.dto.response.storeCategory.StoreCategoryResponse;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.service.StoreCategoryService;
import com.project.baedalsodae.user.entity.UserRole;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StoreCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class StoreCategoryControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private StoreCategoryService storeCategoryService;

    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final UserDetailsImpl managerDetails =
            createManagerUserDetails(UUID.randomUUID());

    private static UserDetailsImpl createManagerUserDetails(UUID userId) {
        return UserDetailsImpl.from(
                userId, "manager@test.com", "password", UserRole.MANAGER, false);
    }

    @Test
    @DisplayName("활성 카테고리 목록 조회 - 성공")
    void getStoreCategoryListForCustomer_success() throws Exception {

        StoreCategoryListResponse response =
                StoreCategoryListResponse.builder()
                        .storeCategoryList(
                                List.of(
                                        StoreCategoryResponse.builder()
                                                .id(CATEGORY_ID)
                                                .name("한식")
                                                .build()))
                        .build();

        given(storeCategoryService.getActiveStoreCategories()).willReturn(response);

        mockMvc.perform(
                        get("/store-categories")
                                .with(user(managerDetails))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storeCategoryList[0].name").value("한식"))
                .andDo(
                        document(
                                "store-category/get-list",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.storeCategoryList[].id")
                                                .description("카테고리 UUID"),
                                        fieldWithPath("data.storeCategoryList[].name")
                                                .description("카테고리 이름"),
                                        fieldWithPath("data.totalCount").description("총 개수"))));
    }

    @Test
    @DisplayName("카테고리 상세 조회 - 성공")
    void getStoreCategoryDetail_success() throws Exception {
        AuditInfoResponse auditInfo = createTestAuditInfo();

        StoreCategoryDetailResponse response =
                StoreCategoryDetailResponse.builder()
                        .id(CATEGORY_ID)
                        .name("한식")
                        .description("설명")
                        .auditInfo(auditInfo)
                        .build();

        given(storeCategoryService.getStoreCategoryDetail(CATEGORY_ID)).willReturn(response);

        mockMvc.perform(
                        get("/store-categories/{storeCategoryId}", CATEGORY_ID)
                                .with(user(managerDetails))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(CATEGORY_ID.toString()))
                .andExpect(jsonPath("$.data.name").value("한식"))
                .andExpect(jsonPath("$.data.description").value("설명"))
                .andExpect(jsonPath("$.data.auditInfo").exists())
                .andDo(
                        document(
                                "store-category/get-detail",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("storeCategoryId")
                                                .description("카테고리 UUID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.id").description("카테고리 UUID"),
                                        fieldWithPath("data.name").description("카테고리 이름"),
                                        fieldWithPath("data.description").description("카테고리 이름"),
                                        fieldWithPath("data.auditInfo.createdBy")
                                                .description("생성자 UUID"),
                                        fieldWithPath("data.auditInfo.createdAt")
                                                .description("생성일시"),
                                        fieldWithPath("data.auditInfo.updatedBy")
                                                .description("수정자 UUID"),
                                        fieldWithPath("data.auditInfo.updatedAt")
                                                .description("수정일시"),
                                        fieldWithPath("data.auditInfo.deletedBy")
                                                .description("삭제자 UUID")
                                                .optional(),
                                        fieldWithPath("data.auditInfo.deletedAt")
                                                .description("삭제일시")
                                                .optional())));
    }

    @Test
    @DisplayName("카테고리 생성 - 성공")
    void createStoreCategory_success() throws Exception {

        CreateStoreCategoryRequest request = new CreateStoreCategoryRequest("중식", "설명");

        willDoNothing()
                .given(storeCategoryService)
                .createStoreCategory(any(CreateStoreCategoryRequest.class));

        mockMvc.perform(
                        post("/store-categories")
                                .with(user(managerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store-category/create",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("name").description("생성할 카테고리 이름"),
                                        fieldWithPath("description").description("생성할 카테고리 설명")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data")
                                                .description("응답 데이터 (null)")
                                                .optional())));
    }

    @Test
    @DisplayName("카테고리 수정 - 성공")
    void updateStoreCategory_success() throws Exception {

        UpdateStoreCategoryRequest request = new UpdateStoreCategoryRequest("일식", "설명");

        willDoNothing()
                .given(storeCategoryService)
                .updateStoreCategory(any(UpdateStoreCategoryRequest.class), eq(CATEGORY_ID));

        mockMvc.perform(
                        patch("/store-categories/{storeCategoryId}", CATEGORY_ID)
                                .with(user(managerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store-category/update",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("storeCategoryId")
                                                .description("수정할 카테고리 UUID")),
                                requestFields(
                                        fieldWithPath("name").description("생성할 카테고리 이름"),
                                        fieldWithPath("description").description("생성할 카테고리 설명")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data")
                                                .description("응답 데이터 (null)")
                                                .optional())));
    }

    @Test
    @DisplayName("카테고리 삭제 - 성공")
    void deleteStoreCategory_success() throws Exception {

        willDoNothing().given(storeCategoryService).deleteStoreCategory(eq(CATEGORY_ID));

        mockMvc.perform(
                        delete("/store-categories/{storeCategoryId}", CATEGORY_ID)
                                .with(user(managerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store-category/delete",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("storeCategoryId")
                                                .description("삭제할 카테고리 UUID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data")
                                                .description("응답 데이터 (null)")
                                                .optional())));
    }

    private AuditInfoResponse createTestAuditInfo() {
        StoreCategory mockEntity = mock(StoreCategory.class);

        UUID adminId = UUID.randomUUID();
        Instant now = Instant.now();

        // 2. Mock 엔티티가 값을 반환하도록 설정
        given(mockEntity.getCreatedBy()).willReturn(adminId);
        given(mockEntity.getCreatedAt()).willReturn(now);
        given(mockEntity.getUpdatedBy()).willReturn(adminId);
        given(mockEntity.getUpdatedAt()).willReturn(now);

        return AuditInfoResponse.fromEntity(mockEntity);
    }
}
