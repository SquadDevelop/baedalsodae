package com.project.baedalsodae.store.dto.request.store;

import com.project.baedalsodae.global.common.dto.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateStoreRequest {
    @NotNull(message = "가게 카테고리는 필수입니다.")
    private UUID storeCategoryId;

    @NotBlank(message = "가게 이름은 필수입니다.")
    @Size(max = 50, message = "가게 이름은 50자 이내여야 합니다.")
    private String storeName;

    @NotBlank(message = "사업자 번호는 필수입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자 번호 형식이 올바르지 않습니다. (예: 000-00-00000)")
    private String businessNumber;

    @NotBlank(message = "가게 전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String storePhone;

    @Valid
    @NotNull(message = "주소 정보는 필수입니다.")
    private AddressRequest address;

    private String description;
}
