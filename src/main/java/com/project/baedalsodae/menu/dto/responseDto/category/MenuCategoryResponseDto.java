package com.project.baedalsodae.menu.dto.responseDto.category;

import com.project.baedalsodae.menu.entity.MenuCategory;
import java.util.List;
import java.util.UUID;

public record MenuCategoryResponseDto(UUID id, String name, int orderNo) {
    public static MenuCategoryResponseDto fromEntity(MenuCategory category) {
        return new MenuCategoryResponseDto(
                category.getId(), category.getName(), category.getOrderNo());
    }

    public static List<MenuCategoryResponseDto> fromEntityList(List<MenuCategory> menuCategories) {
        return menuCategories.stream().map(MenuCategoryResponseDto::fromEntity).toList();
    }
}
