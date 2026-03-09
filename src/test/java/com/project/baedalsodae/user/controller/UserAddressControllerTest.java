package com.project.baedalsodae.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.config.AuthConfig;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.service.UserAddressService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(UserAddressController.class)
@Import(AuthConfig.class)
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
        UUID userId = UUID.randomUUID();
        UserDetailsImpl user = createUserDetails(userId, UserRole.CUSTOMER);
        CreateUserAddressRequest request = createCreateAddressRequest();

        doNothing().when(userAddressService).createAddress(eq(userId), any(CreateUserAddressRequest.class));

        // when & then
        mockMvc.perform(post(BASE_URL)
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_CREATED.getCode()));
    }

    @Test
    @DisplayName("성공 - 사용자의 주소 목록 조회")
    void getAddressList_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDetailsImpl user = createUserDetails(userId, UserRole.CUSTOMER);
        given(userAddressService.getAddressList(userId)).willReturn(new ArrayList<>());

        // when & then
        mockMvc.perform(get(BASE_URL)
                        .with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_FOUND.getCode()));
    }

    @Test
    @DisplayName("성공 - 대표 주소 조회")
    void getMainAddress_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDetailsImpl user = createUserDetails(userId, UserRole.CUSTOMER);
        given(userAddressService.getMainAddress(userId)).willReturn(null);

        // when & then
        mockMvc.perform(get(BASE_URL + "/main")
                        .with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_FOUND.getCode()));
    }

    @Test
    @DisplayName("성공 - 특정 주소 수정")
    void updateAddress_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UserDetailsImpl user = createUserDetails(userId, UserRole.CUSTOMER);
        UpdateUserAddressRequest request = createUpdateUserAddressRequest(addressId);
        UserAddressResponse response = createUserAddressResponse(addressId, true);

        given(userAddressService.updateAddress(eq(userId), eq(addressId), any(UpdateUserAddressRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put(BASE_URL + "/" + addressId)
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_UPDATED.getCode()));
    }

    @Test
    @DisplayName("성공 - 주소 목록 일괄 수정")
    void updateAddressList_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDetailsImpl user = createUserDetails(userId, UserRole.CUSTOMER);
        List<UpdateUserAddressRequest> requests = List.of(createUpdateUserAddressRequest(UUID.randomUUID()));

        doNothing().when(userAddressService).updateAddressList(eq(userId), anyList());

        // when & then
        mockMvc.perform(put(BASE_URL)
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_BULK_UPDATED.getCode()));
    }

    @Test
    @DisplayName("성공 - 특정 주소 삭제")
    void deleteAddress_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UserDetailsImpl user = createUserDetails(userId, UserRole.CUSTOMER);

        doNothing().when(userAddressService).deleteAddress(userId, addressId);

        // when & then
        mockMvc.perform(delete(BASE_URL + "/" + addressId)
                        .with(user(user))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_ADDRESS_DELETED.getCode()));
    }

    @Test
    @DisplayName("성공 - 대표 주소 변경")
    void changeMainAddress_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UserDetailsImpl user = createUserDetails(userId, UserRole.CUSTOMER);

        doNothing().when(userAddressService).setMainAddress(userId, addressId);

        // when & then
        mockMvc.perform(patch(BASE_URL + "/" + addressId + "/main")
                        .with(user(user))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_MAIN_ADDRESS_CHANGED.getCode()));
    }

    // Helper Methods
    private UserDetailsImpl createUserDetails(UUID userId, UserRole role) {
        return UserDetailsImpl.from(userId, "testUser", "password", role, false);
    }

    private CreateUserAddressRequest createCreateAddressRequest() {
        return CreateUserAddressRequest.builder()
                .roadAddress("서울시 강남구")
                .detailAddress("101호")
                .sidoCode("11")
                .sidoName("서울")
                .sigunguCode("110")
                .sigunguName("강남구")
                .dongCode("11010")
                .dongName("역삼동")
                .build();
    }

    private UpdateUserAddressRequest createUpdateUserAddressRequest(UUID addressId) {
        return UpdateUserAddressRequest.builder()
                .userAddressId(addressId)
                .roadAddress("수정된 도로명")
                .detailAddress("수정된 상세주소")
                .sidoCode("11")
                .sidoName("서울")
                .sigunguCode("110")
                .sigunguName("강남구")
                .dongCode("11010")
                .dongName("역삼동")
                .build();
    }

    private UserAddressResponse createUserAddressResponse(UUID addressId, boolean isMain) {
        return UserAddressResponse.builder()
                .userAddressId(addressId)
                .roadAddress("도로명")
                .detailAddress("상세")
                .isMainAddress(isMain)
                .build();
    }
}
