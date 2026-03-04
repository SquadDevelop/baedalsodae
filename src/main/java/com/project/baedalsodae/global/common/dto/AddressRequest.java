package com.project.baedalsodae.global.common.dto;

import com.project.baedalsodae.global.common.entity.Address;
import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank(message = "시도 코드는 필수입니다") String sidoCode,
        @NotBlank(message = "시도 이름은 필수입니다") String sidoName,
        @NotBlank(message = "시군구 코드는 필수입니다") String sigunguCode,
        @NotBlank(message = "시군구 이름은 필수입니다") String sigunguName,
        @NotBlank(message = "읍면동 코드는 필수입니다") String dongCode,
        @NotBlank(message = "읍면동 이름은 필수입니다") String dongName,
        @NotBlank(message = "도로명 주소는 필수입니다") String roadAddress,
        @NotBlank(message = "상세 주소는 필수입니다") String detailAddress) {
    public Address toEntity() {
        return Address.createAddress(
                sidoCode,
                sidoName,
                sigunguCode,
                sigunguName,
                dongCode,
                dongName,
                roadAddress,
                detailAddress);
    }
}
