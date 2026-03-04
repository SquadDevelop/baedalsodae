package com.project.baedalsodae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateUserAddressRequest {

    private UUID userAddressId;

    @NotBlank
    private String roadAddress;

    @NotBlank
    private String detailAddress;

    private String description;
}
