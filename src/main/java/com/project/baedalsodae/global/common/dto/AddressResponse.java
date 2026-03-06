package com.project.baedalsodae.global.common.dto;

import com.project.baedalsodae.global.common.entity.Address;

public record AddressResponse(
        String sidoCode,
        String sidoName,
        String sigunguCode,
        String sigunguName,
        String dongCode,
        String dongName,
        String roadAddress,
        String detailAddress) {
    public static AddressResponse fromEntity(Address address) {
        return new AddressResponse(
                address.getSidoCode(), address.getSidoName(),
                address.getSigunguCode(), address.getSigunguName(),
                address.getDongCode(), address.getDongName(),
                address.getRoadAddress(), address.getDetailAddress());
    }
}
