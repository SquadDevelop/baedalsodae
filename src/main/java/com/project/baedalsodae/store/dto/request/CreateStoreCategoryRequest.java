package com.project.baedalsodae.store.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateStoreCategoryRequest {
    @NotEmpty(message = "카테고리 이름은 필수입니다.")
    @NotNull(message = "카테고리 이름은 필수입니다.")
    private String name;

    @Size(max = 200, message = "설명은 200자 이내로 작성해주세요.")
    private String description;
}
