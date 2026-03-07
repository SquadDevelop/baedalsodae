package com.project.baedalsodae.user.dto.request;

import com.project.baedalsodae.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateUserRequest {

    @NotBlank
    @Pattern(regexp = "^[a-z0-9]{4,10}$")
    private String username;

    @NotBlank
    @Pattern(regexp = "^01(?:0|1|[6-9])[-]?(\\d{3}|\\d{4})[-]?(\\d{4})$")
    private String phone;

    @NotBlank @Email private String email;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,15}$")
    private String password;

    @NotBlank private String name;

    @NotBlank private String nickname;

    private UserRole role;

    private String roadAddress;
    private String detailAddress;
    private String description;
}
