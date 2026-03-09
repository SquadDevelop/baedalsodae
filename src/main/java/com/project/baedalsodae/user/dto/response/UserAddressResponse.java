package com.project.baedalsodae.user.dto.response;

import com.project.baedalsodae.global.common.entity.Address;
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
    
    private String sidoCode;
    private String sidoName;
    private String sigunguCode;
    private String sigunguName;
    private String dongCode;
    private String dongName;
    
    private String roadAddress;
    private String detailAddress;
    private String description;

    private boolean isMainAddress;

    public static UserAddressResponse from(UserAddress userAddress, UUID userMainAddressId) {
        Address address = userAddress.getAddress();
        return UserAddressResponse.builder()
                .userAddressId(userAddress.getId())
                .sidoCode(address.getSidoCode())
                .sidoName(address.getSidoName())
                .sigunguCode(address.getSigunguCode())
                .sigunguName(address.getSigunguName())
                .dongCode(address.getDongCode())
                .dongName(address.getDongName())
                .roadAddress(address.getRoadAddress())
                .detailAddress(address.getDetailAddress())
                .description(userAddress.getDescription())
                .isMainAddress(Objects.equals(userMainAddressId, userAddress.getId()))
                .build();
    }
}
