package com.project.baedalsodae.auth.dto.request;

import com.project.baedalsodae.user.dto.request.CreateUserRequest;
import com.project.baedalsodae.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
    @NotBlank
    @Pattern(regexp = "^[a-z0-9]{4,10}$",
            message = "아이디는 4~10자 이내의 영문 소문자와 숫자만 가능합니다.")
    String username,

    @NotBlank
    @Pattern(regexp = "^01(?:0|1|[6-9])[-]?(\\d{3}|\\d{4})[-]?(\\d{4})$",
            message = "유효한 핸드폰 번호 형식이 아닙니다.")
    String phone,

    @NotBlank
    @Email(message = "유효한 이메일 주소가 아닙니다.")
    String email,

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,15}$",
            message = "비밀번호는 8~15자 이내의 영문, 숫자, 특수문자 조합이어야 합니다.")
    String password,

    @NotBlank
    String name,

    @NotBlank
    String nickname,

    UserRole role,

    String roadAddress,
    String detailAddress,
    String description
) {
    public CreateUserRequest toCreateUserDto() {
        return CreateUserRequest.builder()
            .username(this.username)
            .phone(this.phone)
            .email(this.email)
            .password(this.password)
            .name(this.name)
            .nickname(this.nickname)
            .role(this.role)
            .roadAddress(this.roadAddress)
            .detailAddress(this.detailAddress)
            .description(this.description)
            .build();
    }
}
