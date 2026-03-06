package com.project.baedalsodae.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;

    public static LoginResponse from(String accessToken) {
        return new LoginResponse(accessToken);
    }
}
