package com.project.baedalsodae.menu.dto.requestDto.category;

import jakarta.validation.constraints.NotBlank;

public record MenuCategoryPutRequestDto(@NotBlank String name) {}
