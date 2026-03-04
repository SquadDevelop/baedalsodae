package com.project.baedalsodae.menu.dto.requestDto.item;

import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import java.util.List;
import java.util.UUID;

public record MenuItemPutRequestDto(
    String name,
    String description,
    int price,
    boolean isPopular,
    UUID categoryId,
    MenuStatus menuStatus,
    List<String> tagNames) {}
