package com.project.baedalsodae.menu.dto.requestDto;

import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import java.util.UUID;

public record MenuPatchRequestDto(
    String name,
    String description,
    Integer price,
    Integer orderNo,
    Boolean isPopular,
    UUID categoryId,
    MenuStatus menuStatus) {}
