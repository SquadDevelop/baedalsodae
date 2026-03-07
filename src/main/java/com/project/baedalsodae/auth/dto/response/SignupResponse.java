package com.project.baedalsodae.auth.dto.response;

import java.util.UUID;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignupResponse {

    private UUID userId;
    private String username;
    private String nickname;

    public static SignupResponse from(UUID userId, String username, String nickname) {
        return new SignupResponse(userId, username, nickname);
    }
}
