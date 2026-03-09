package com.project.baedalsodae.store.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
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
import com.project.baedalsodae.global.common.dto.AddressRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreStatusRequest;
import com.project.baedalsodae.store.entity.enums.StoreStatus;
import com.project.baedalsodae.store.service.AdminStoreService;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminStoreController.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class AdminStoreControllerTest {

    @Autowired private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private AdminStoreService adminStoreService;

    @Autowired private ObjectMapper objectMapper;

    private final UUID STORE_ID = UUID.randomUUID();
    private UserDetailsImpl managerDetails;

    @BeforeEach
    void setUp() {
        // 관리자 권한을 가진 UserDetails 생성
        managerDetails =
                UserDetailsImpl.from(
                        UUID.randomUUID(), "admin_user", "password", UserRole.MANAGER, false);
    }

    @Test
    @DisplayName("관리자: 가게 정보 수정 - 성공")
    void updateStore_success() throws Exception {
        // given
        UpdateStoreRequest request =
                new UpdateStoreRequest(
                        UUID.randomUUID(),
                        "수정된 가게명",
                        "02-1111-2222",
                        new AddressRequest(
                                "11", "서울특별시", "110", "강남구", "11010", "역삼동", "테헤란로 427", "위워크"),
                        "가게 설명 수정");
        willDoNothing()
                .given(adminStoreService)
                .updateStore(any(UpdateStoreRequest.class), any(UUID.class));

        // when & then
        mockMvc.perform(
                        patch("/admins/stores/{storeId}", STORE_ID)
                                .with(user(managerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "admin/store/update",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("storeId").description("수정할 가게 UUID")),
                                requestFields(
                                        fieldWithPath("storeCategoryId").description("카테고리 UUID"),
                                        fieldWithPath("storeName").description("수정할 가게 이름"),
                                        fieldWithPath("storePhone").description("가게 전화번호"),
                                        fieldWithPath("description").description("가게 설명"),
                                        fieldWithPath("address").description("가게 주소"),
                                        fieldWithPath("address.sidoCode").description("시도 코드"),
                                        fieldWithPath("address.sidoName").description("시도 이름"),
                                        fieldWithPath("address.sigunguCode").description("시군구 코드"),
                                        fieldWithPath("address.sigunguName").description("시군구 이름"),
                                        fieldWithPath("address.dongCode").description("동 코드"),
                                        fieldWithPath("address.dongName").description("동 이름"),
                                        fieldWithPath("address.roadAddress").description("도로명 주소"),
                                        fieldWithPath("address.detailAddress")
                                                .description("상세 주소")),
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
    @DisplayName("관리자: 가게 상태 수정 - 성공")
    void updateStoreStatus_success() throws Exception {
        // given
        UpdateStoreStatusRequest request =
                new UpdateStoreStatusRequest(StoreStatus.SUSPENDED); // 영업중으로 변경
        willDoNothing()
                .given(adminStoreService)
                .updateStoreStatus(eq(STORE_ID), eq(StoreStatus.SUSPENDED));

        // when & then
        mockMvc.perform(
                        patch("/admins/stores/{storeId}/status", STORE_ID)
                                .with(user(managerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "admin/store/update-status",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("storeId").description("가게 UUID")),
                                requestFields(
                                        fieldWithPath("status")
                                                .description("변경할 상태 (true: 영업중, false: 준비중)")),
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
    @DisplayName("관리자: 가게 삭제 - 성공")
    void deleteStore_success() throws Exception {
        // given
        willDoNothing().given(adminStoreService).deleteStore(any(UUID.class), any(UUID.class));

        // when & then
        mockMvc.perform(
                        delete("/admins/stores/{storeId}", STORE_ID)
                                .with(user(managerDetails))
                                .with(csrf()))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "admin/store/delete",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("storeId").description("삭제할 가게 UUID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data")
                                                .description("응답 데이터 (null)")
                                                .optional())));
    }
}
