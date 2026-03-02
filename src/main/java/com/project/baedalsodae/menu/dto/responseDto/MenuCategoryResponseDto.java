package com.project.baedalsodae.menu.dto.responseDto;

import com.project.baedalsodae.menu.entity.MenuCategory;
import java.util.UUID;

public record MenuCategoryResponseDto(UUID id, String name, int orderNo) {
    public static MenuCategoryResponseDto fromEntity(MenuCategory category) {
      return new MenuCategoryResponseDto(category.getId(), category.getName(), category.getOrderNo());
    }
  }

