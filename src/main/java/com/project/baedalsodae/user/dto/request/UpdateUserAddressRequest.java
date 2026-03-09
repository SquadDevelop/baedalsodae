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

    @NotBlank(message = "시도 코드는 필수입니다")
    private String sidoCode;

    @NotBlank(message = "시도 이름은 필수입니다")
    private String sidoName;

    @NotBlank(message = "시군구 코드는 필수입니다")
    private String sigunguCode;

    @NotBlank(message = "시군구 이름은 필수입니다")
    private String sigunguName;

    @NotBlank(message = "읍면동 코드는 필수입니다")
    private String dongCode;

    @NotBlank(message = "읍면동 이름은 필수입니다")
    private String dongName;

    @NotBlank(message = "도로명 주소는 필수입니다")
    private String roadAddress;

    @NotBlank(message = "상세 주소는 필수입니다")
    private String detailAddress;

    private String description;
}
