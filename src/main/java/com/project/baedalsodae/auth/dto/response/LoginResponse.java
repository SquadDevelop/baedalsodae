package com.project.baedalsodae.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;

    public static LoginResponse from(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, refreshToken);
    }
}
