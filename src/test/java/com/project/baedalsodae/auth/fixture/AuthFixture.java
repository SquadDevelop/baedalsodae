package com.project.baedalsodae.auth.fixture;

import com.project.baedalsodae.auth.dto.request.LoginRequest;
import com.project.baedalsodae.auth.dto.request.ReissueRequest;
import com.project.baedalsodae.auth.dto.request.SignupRequest;
import com.project.baedalsodae.auth.dto.response.LoginResponse;
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.UUID;

public class AuthFixture {

    public static final String ACCESS_TOKEN =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0ZXIxMjMiLCJhdXRoIjoiUk9MRV_Q1VSTE9NRVIiLCJpYXQiOjE3MTI3MTIzNDUsImV4cCI6MTcxMjcxNTk0NX0.mock-signature";
    public static final String REFRESH_TOKEN = "def-456-refresh-token-mock";

    public static SignupRequest createSignupRequest() {
        CreateUserAddressRequest address =
                CreateUserAddressRequest.from(
                        "11",
                        "서울특별시",
                        "11680",
                        "강남구",
                        "1168010100",
                        "역삼동",
                        "서울특별시 강남구 테헤란로 427",
                        "위워크 타워 10층",
                        "회사");
        return new SignupRequest(
                "tester123",
                "010-1234-5678",
                "tester123@baedalsodae.com",
                "Password123!",
                "테스터",
                "닉네임",
                UserRole.CUSTOMER,
                address);
    }

    public static UserDetailResponse createSignupUserDetailResponse(UUID userId) {
        return UserDetailResponse.builder()
                .userId(userId)
                .username("tester123")
                .nickname("닉네임")
                .build();
    }

    public static LoginRequest createLoginRequest() {
        return new LoginRequest("tester123", "Password123!");
    }

    public static LoginResponse createLoginResponse() {
        return LoginResponse.from("Bearer " + ACCESS_TOKEN, REFRESH_TOKEN);
    }

    public static ReissueRequest createReissueRequest() {
        return new ReissueRequest(REFRESH_TOKEN);
    }
}
