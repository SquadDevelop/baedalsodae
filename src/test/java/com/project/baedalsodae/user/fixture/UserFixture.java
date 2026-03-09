package com.project.baedalsodae.user.fixture;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.CreateUserRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import com.project.baedalsodae.user.dto.response.UserDeleteResponse;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.entity.UserRole;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserFixture {

    public static final UUID USER_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    public static final UUID MANAGER_ID = UUID.fromString("62f6b8b1-3843-4e4f-b649-6a9d7b9e6f3d");
    public static final UUID MASTER_ID = UUID.fromString("8d7b3c2a-1e4d-4a2b-9c8f-5a6b7c8d9e0f");
    public static final UUID ADDRESS_ID = UUID.fromString("a1b2c3d4-e5f6-4a5b-b6c7-d8e9f0a1b2c3");

    // UserDetails 픽스처
    public static UserDetailsImpl createUserDetails(UUID userId, UserRole role) {
        return UserDetailsImpl.from(userId, "testUser_" + userId.toString().substring(0, 8), "password", role, false);
    }

    // 상세 정보 응답 픽스처
    public static UserDetailResponse createUserDetailResponse(UUID userId, String username, UserRole role) {
        return UserDetailResponse.builder()
                .userId(userId)
                .username(username)
                .phone("010-1234-5678")
                .email(username + "@baedalsodae.com")
                .name("사용자_" + username)
                .nickname("닉네임_" + username)
                .role(role)
                .addresses(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .createdBy(MASTER_ID)
                .build();
    }

    // 수정 요청 픽스처
    public static UpdateUserRequest createUpdateUserRequest() {
        return UpdateUserRequest.builder()
                .nickname("변경된닉네임")
                .phone("010-9999-8888")
                .email("updated@baedalsodae.com")
                .addresses(List.of())
                .build();
    }

    // 삭제 응답 픽스처
    public static UserDeleteResponse createUserDeleteResponse(UUID userId) {
        return UserDeleteResponse.builder()
                .id(userId)
                .deletedAt(LocalDateTime.now())
                .deletedBy(MASTER_ID)
                .build();
    }

    // 관리자 생성 요청 픽스처
    public static CreateUserRequest createManagerRequest(String username) {
        String validUsername = "manager123"; // 제약 조건: 4-10자, 소문자/숫자
        return CreateUserRequest.builder()
                .username(validUsername)
                .phone("010-5555-4444")
                .email(validUsername + "@admin.com")
                .password("Admin123!")
                .name("매니저관리자")
                .nickname("어드민매니저")
                .role(UserRole.MANAGER)
                .address(createCreateAddressRequest())
                .build();
    }

    // 주소 생성 요청 픽스처
    public static CreateUserAddressRequest createCreateAddressRequest() {
        return CreateUserAddressRequest.builder()
                .roadAddress("서울특별시 강남구 테헤란로 427")
                .detailAddress("위워크 타워 10층")
                .sidoCode("11")
                .sidoName("서울특별시")
                .sigunguCode("11680")
                .sigunguName("강남구")
                .dongCode("1168010100")
                .dongName("역삼동")
                .description("회사")
                .build();
    }

    // 주소 수정 요청 픽스처
    public static UpdateUserAddressRequest createUpdateUserAddressRequest(UUID addressId) {
        return UpdateUserAddressRequest.builder()
                .userAddressId(addressId)
                .roadAddress("경기도 성남시 분당구 판교역로 166")
                .detailAddress("카카오 판교 오피스")
                .sidoCode("41")
                .sidoName("경기도")
                .sigunguCode("41135")
                .sigunguName("성남시 분당구")
                .dongCode("4113510900")
                .dongName("백현동")
                .description("집")
                .build();
    }

    // 주소 응답 픽스처
    public static UserAddressResponse createUserAddressResponse(UUID addressId, boolean isMain) {
        return UserAddressResponse.builder()
                .userAddressId(addressId)
                .sidoCode("11")
                .sidoName("서울특별시")
                .sigunguCode("11680")
                .sigunguName("강남구")
                .dongCode("1168010100")
                .dongName("역삼동")
                .roadAddress("서울특별시 강남구 테헤란로 427")
                .detailAddress("위워크 타워 10층")
                .description("회사")
                .isMainAddress(isMain)
                .build();
    }
}
