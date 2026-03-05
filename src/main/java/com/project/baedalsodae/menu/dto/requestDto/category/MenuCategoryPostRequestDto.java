package com.project.baedalsodae.menu.dto.requestDto.category;

import jakarta.validation.constraints.NotBlank;

public record MenuCategoryPostRequestDto(@NotBlank String name) {}
