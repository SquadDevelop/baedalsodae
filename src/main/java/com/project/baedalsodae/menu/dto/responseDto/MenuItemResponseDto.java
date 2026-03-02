package com.project.baedalsodae.menu.dto.responseDto;

import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import java.util.UUID;

public record MenuItemResponseDto(
      UUID id,
      String name,
      String description,
      int price,
      int orderNo,
      boolean isPopular,
      MenuStatus menuStatus,
      UUID categoryId,
      String categoryName) {
    public static MenuItemResponseDto fromEntity(MenuItem item) {
      return new MenuItemResponseDto(
          item.getId(),
          item.getName(),
          item.getDescription(),
          item.getPrice(),
          item.getOrderNo(),
          item.isPopular(),
          item.getMenuStatus(),
          item.getMenuCategory().getId(),
          item.getMenuCategory().getName());
    }

}
