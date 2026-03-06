package com.project.baedalsodae.auth.dto.response;

import lombok.*;

import java.util.UUID;

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
