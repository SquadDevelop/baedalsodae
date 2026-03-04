package com.project.baedalsodae.user.dto.response;

import com.project.baedalsodae.user.entity.UserAddress;
import java.util.Objects;
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
public class UserAddressResponse {

    private UUID userAddressId;
    private String roadAddress;
    private String detailAddress;
    private String description;

    private boolean isMainAddress;

    public static UserAddressResponse from(UserAddress userAddress, UUID userMainAddressId) {
        return UserAddressResponse.builder()
                .userAddressId(userAddress.getId())
                .roadAddress(userAddress.getRoadAddress())
                .detailAddress(userAddress.getDetailAddress())
                .description(userAddress.getDescription())
                .isMainAddress(Objects.equals(userMainAddressId, userAddress.getId()))
                .build();
    }
}
