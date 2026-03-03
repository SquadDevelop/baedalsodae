package com.project.baedalsodae.menu.dto.requestDto.item;

import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import java.util.List;

public record MenuItemPostRequestDto(
    String name,
    String description,
    int price,
    boolean isPopular,
    MenuStatus menuStatus,
    List<String> tagNames) {}
