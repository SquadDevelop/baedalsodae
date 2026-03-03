package com.project.baedalsodae.store.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PatchStoreCategoryRequest {
    @NotEmpty
    @NotNull
    @Size(min = 2, max = 50)
    private String name;

    @Size(max = 200)
    private String description;
}
