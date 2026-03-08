package com.project.baedalsodae.menu.dto.requestDto.item;

import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MenuItemPutRequestDto(
        @NotBlank String name,
        String description,
        BigDecimal price,
        boolean isPopular,
        @NotNull UUID categoryId,
        @NotNull MenuStatus menuStatus,
        List<String> tagNames) {}
