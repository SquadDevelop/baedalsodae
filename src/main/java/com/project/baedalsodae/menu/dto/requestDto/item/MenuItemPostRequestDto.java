package com.project.baedalsodae.menu.dto.requestDto.item;

import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record MenuItemPostRequestDto(
        @NotBlank String name,
        String description,
        int price,
        boolean isPopular,
        @NotNull MenuStatus menuStatus,
        List<String> tagNames) {}
