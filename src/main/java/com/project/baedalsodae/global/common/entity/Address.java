package com.project.baedalsodae.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Address {

    @Column(name = "sido_code", length = 20, nullable = false)
    private String sidoCode;

    @Column(name = "sido_name", length = 50, nullable = false)
    private String sidoName;

    @Column(name = "sigungu_code", length = 20, nullable = false)
    private String sigunguCode;

    @Column(name = "sigungu_name", length = 50, nullable = false)
    private String sigunguName;

    @Column(name = "dong_code", length = 20, nullable = false)
    private String dongCode;

    @Column(name = "dong_name", length = 50, nullable = false)
    private String dongName;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    public static Address createAddress(
            String sidoCode,
            String sidoName,
            String sigunguCode,
            String sigunguName,
            String dongCode,
            String dongName,
            String roadAddress,
            String detailAddress) {
        return new Address(
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
