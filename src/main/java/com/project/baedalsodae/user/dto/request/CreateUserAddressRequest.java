package com.project.baedalsodae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateUserAddressRequest {

    @NotBlank
    private String roadAddress;

    @NotBlank
    private String detailAddress;

    private String description;

    public static CreateUserAddressRequest from(String roadAddress, String detailAddress, String description) {
        return CreateUserAddressRequest.builder()
                .roadAddress(roadAddress)
                .detailAddress(detailAddress)
                .description(description)
                .build();
    }
}
