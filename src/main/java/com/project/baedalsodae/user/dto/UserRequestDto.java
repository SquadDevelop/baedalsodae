package com.project.baedalsodae.user.dto;

import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class UserRequestDto {

    @Getter
    @Setter
    @Builder
    public static class Create {

        @NotBlank
        @Pattern(regexp = "^[a-z0-9]{4,10}$")
        private String username;

        @NotBlank
        @Pattern(regexp = "^01(?:0|1|[6-9])[-]?(\\d{3}|\\d{4})[-]?(\\d{4})$")
        private String phone;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,15}$")
        private String password;

        @NotBlank
        private String name;

        @NotBlank
        private String nickname;

        private UserRole role;

        private String roadAddress;
        private String detailAddress;

        public User toEntity(String encodedPassword) {
            return User.builder()
                    .username(this.username)
                    .phone(this.phone)
                    .email(this.email)
                    .password(encodedPassword)
                    .name(this.name)
                    .nickname(this.nickname)
                    .role(this.role)
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    public static class Update {

        private String phone;
        private String email;
        private String password;
        private String nickname;
        private String roadAddress;
        private String detailAddress;
    }
}
