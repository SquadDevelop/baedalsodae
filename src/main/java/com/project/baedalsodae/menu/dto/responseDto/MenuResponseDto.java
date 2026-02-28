package com.project.baedalsodae.menu.dto.responseDto;

import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import java.util.UUID;

public class MenuResponseDto {

  public record MenuItemResponse(
      UUID id,
      String name,
      String description,
      int price,
      int orderNo,
      boolean isPopular,
      MenuStatus menuStatus,
      UUID categoryId,
      String categoryName) {
    public static MenuItemResponse fromEntity(MenuItem item) {
      return new MenuItemResponse(
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

  public record MenuCategoryResponse(UUID id, String name, int orderNo) {
    public static MenuCategoryResponse fromEntity(MenuCategory category) {
      return new MenuCategoryResponse(category.getId(), category.getName(), category.getOrderNo());
    }
  }
}
