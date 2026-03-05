package com.project.baedalsodae.menu.dto.requestDto.category;

import jakarta.validation.constraints.Positive;

public record MenuCategoryPatchRequestDto(@Positive int orderNo) {}
