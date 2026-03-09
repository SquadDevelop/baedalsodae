package com.project.baedalsodae.user.controller;

import static com.project.baedalsodae.user.fixture.UserFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
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
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.service.UserAddressService;
import java.util.List;
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
@WebMvcTest(UserAddressController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureRestDocs
public class UserAddressControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserAddressService userAddressService;

    @MockitoBean private JwtProvider jwtProvider;

    @MockitoBean private UserDetailsService userDetailsService;

    @MockitoBean private TokenRedisUtil tokenRedisUtil;

    private final String BASE_URL = "/user-addresses";

    @Test
    @DisplayName("성공 - 신규 주소 등록")
    void createAddress_Success() throws Exception {
        // given
        UserDetailsImpl user = createUserDetails(USER_ID, UserRole.CUSTOMER);
        CreateUserAddressRequest request = createCreateAddressRequest();

        doNothing()
                .when(userAddressService)
                .createAddress(eq(USER_ID), any(CreateUserAddressRequest.class));

        // when & then
        mockMvc.perform(
                        post(BASE_URL)
                                .with(user(user))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_CREATED.getCode()))
                .andDo(
                        document(
                                "user-address/create",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("roadAddress").description("도로명 주소"),
                                        fieldWithPath("detailAddress").description("상세 주소"),
                                        fieldWithPath("sidoCode").description("시도 코드"),
                                        fieldWithPath("sidoName").description("시도 이름"),
                                        fieldWithPath("sigunguCode").description("시군구 코드"),
                                        fieldWithPath("sigunguName").description("시군구 이름"),
                                        fieldWithPath("dongCode").description("동 코드"),
                                        fieldWithPath("dongName").description("동 이름"),
                                        fieldWithPath("description").description("장소 설명").optional()),
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
    @DisplayName("성공 - 사용자의 주소 목록 조회")
    void getAddressList_Success() throws Exception {
        // given
        UserDetailsImpl user = createUserDetails(USER_ID, UserRole.CUSTOMER);
        UserAddressResponse addressResponse = createUserAddressResponse(ADDRESS_ID, true);
        given(userAddressService.getAddressList(USER_ID)).willReturn(List.of(addressResponse));

        // when & then
        mockMvc.perform(get(BASE_URL).with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_FOUND.getCode()))
                .andDo(
                        document(
                                "user-address/get-list",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data[].userAddressId").description("주소 UUID"),
                                        fieldWithPath("data[].sidoCode").description("시도 코드"),
                                        fieldWithPath("data[].sidoName").description("시도 이름"),
                                        fieldWithPath("data[].sigunguCode").description("시군구 코드"),
                                        fieldWithPath("data[].sigunguName").description("시군구 이름"),
                                        fieldWithPath("data[].dongCode").description("동 코드"),
                                        fieldWithPath("data[].dongName").description("동 이름"),
                                        fieldWithPath("data[].roadAddress").description("도로명 주소"),
                                        fieldWithPath("data[].detailAddress").description("상세 주소"),
                                        fieldWithPath("data[].description").description("장소 설명"),
                                        fieldWithPath("data[].mainAddress")
                                                .description("대표 주소 여부"))));
    }

    @Test
    @DisplayName("성공 - 대표 주소 조회")
    void getMainAddress_Success() throws Exception {
        // given
        UserDetailsImpl user = createUserDetails(USER_ID, UserRole.CUSTOMER);
        UserAddressResponse addressResponse = createUserAddressResponse(ADDRESS_ID, true);
        given(userAddressService.getMainAddress(USER_ID)).willReturn(addressResponse);

        // when & then
        mockMvc.perform(get(BASE_URL + "/main").with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_FOUND.getCode()))
                .andDo(
                        document(
                                "user-address/get-main",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.userAddressId").description("주소 UUID"),
                                        fieldWithPath("data.sidoCode").description("시도 코드"),
                                        fieldWithPath("data.sidoName").description("시도 이름"),
                                        fieldWithPath("data.sigunguCode").description("시군구 코드"),
                                        fieldWithPath("data.sigunguName").description("시군구 이름"),
                                        fieldWithPath("data.dongCode").description("동 코드"),
                                        fieldWithPath("data.dongName").description("동 이름"),
                                        fieldWithPath("data.roadAddress").description("도로명 주소"),
                                        fieldWithPath("data.detailAddress").description("상세 주소"),
                                        fieldWithPath("data.description").description("장소 설명"),
                                        fieldWithPath("data.mainAddress")
                                                .description("대표 주소 여부"))));
    }

    @Test
    @DisplayName("성공 - 특정 주소 수정")
    void updateAddress_Success() throws Exception {
        // given
        UserDetailsImpl user = createUserDetails(USER_ID, UserRole.CUSTOMER);
        UpdateUserAddressRequest request = createUpdateUserAddressRequest(ADDRESS_ID);
        UserAddressResponse response = createUserAddressResponse(ADDRESS_ID, true);

        given(
                        userAddressService.updateAddress(
                                eq(USER_ID), eq(ADDRESS_ID), any(UpdateUserAddressRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        put(BASE_URL + "/{addressId}", ADDRESS_ID)
                                .with(user(user))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_UPDATED.getCode()))
                .andDo(
                        document(
                                "user-address/update",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("addressId").description("수정할 주소 UUID")),
                                requestFields(
                                        fieldWithPath("userAddressId").description("주소 UUID"),
                                        fieldWithPath("roadAddress").description("도로명 주소"),
                                        fieldWithPath("detailAddress").description("상세 주소"),
                                        fieldWithPath("sidoCode").description("시도 코드"),
                                        fieldWithPath("sidoName").description("시도 이름"),
                                        fieldWithPath("sigunguCode").description("시군구 코드"),
                                        fieldWithPath("sigunguName").description("시군구 이름"),
                                        fieldWithPath("dongCode").description("동 코드"),
                                        fieldWithPath("dongName").description("동 이름"),
                                        fieldWithPath("description").description("장소 설명").optional()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.userAddressId").description("주소 UUID"),
                                        fieldWithPath("data.sidoCode").description("시도 코드"),
                                        fieldWithPath("data.sidoName").description("시도 이름"),
                                        fieldWithPath("data.sigunguCode").description("시군구 코드"),
                                        fieldWithPath("data.sigunguName").description("시군구 이름"),
                                        fieldWithPath("data.dongCode").description("동 코드"),
                                        fieldWithPath("data.dongName").description("동 이름"),
                                        fieldWithPath("data.roadAddress").description("도로명 주소"),
                                        fieldWithPath("data.detailAddress").description("상세 주소"),
                                        fieldWithPath("data.description").description("장소 설명"),
                                        fieldWithPath("data.mainAddress")
                                                .description("대표 주소 여부"))));
    }

    @Test
    @DisplayName("성공 - 주소 목록 일괄 수정")
    void updateAddressList_Success() throws Exception {
        // given
        UserDetailsImpl user = createUserDetails(USER_ID, UserRole.CUSTOMER);
        List<UpdateUserAddressRequest> requests = List.of(createUpdateUserAddressRequest(ADDRESS_ID));

        doNothing().when(userAddressService).updateAddressList(eq(USER_ID), anyList());

        // when & then
        mockMvc.perform(
                        put(BASE_URL)
                                .with(user(user))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code").value(SuccessCode.USER_ADDRESS_BULK_UPDATED.getCode()))
                .andDo(
                        document(
                                "user-address/update-bulk",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("[].userAddressId").description("주소 UUID"),
                                        fieldWithPath("[].roadAddress").description("도로명 주소"),
                                        fieldWithPath("[].detailAddress").description("상세 주소"),
                                        fieldWithPath("[].sidoCode").description("시도 코드"),
                                        fieldWithPath("[].sidoName").description("시도 이름"),
                                        fieldWithPath("[].sigunguCode").description("시군구 코드"),
                                        fieldWithPath("[].sigunguName").description("시군구 이름"),
                                        fieldWithPath("[].dongCode").description("동 코드"),
                                        fieldWithPath("[].dongName").description("동 이름"),
                                        fieldWithPath("[].description")
                                                .description("장소 설명")
                                                .optional()),
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
    @DisplayName("성공 - 특정 주소 삭제")
    void deleteAddress_Success() throws Exception {
        // given
        UserDetailsImpl user = createUserDetails(USER_ID, UserRole.CUSTOMER);

        doNothing().when(userAddressService).deleteAddress(USER_ID, ADDRESS_ID);

        // when & then
        mockMvc.perform(delete(BASE_URL + "/{addressId}", ADDRESS_ID).with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_DELETED.getCode()))
                .andDo(
                        document(
                                "user-address/delete",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("addressId").description("삭제할 주소 UUID")),
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
    @DisplayName("성공 - 대표 주소 변경")
    void changeMainAddress_Success() throws Exception {
        // given
        UserDetailsImpl user = createUserDetails(USER_ID, UserRole.CUSTOMER);

        doNothing().when(userAddressService).setMainAddress(USER_ID, ADDRESS_ID);

        // when & then
        mockMvc.perform(
                        patch(BASE_URL + "/{addressId}/main", ADDRESS_ID)
                                .with(user(user)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code").value(SuccessCode.USER_MAIN_ADDRESS_CHANGED.getCode()))
                .andDo(
                        document(
                                "user-address/change-main",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("addressId")
                                                .description("대표로 설정할 주소 UUID")),
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
