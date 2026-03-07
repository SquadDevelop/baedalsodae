
package com.project.baedalsodae.menu.dto.responseDto.item;

import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import java.util.List;
import java.util.UUID;

public record MenuItemResponseDto(
        UUID id,
        String name,
        String description,
        int price,
        int orderNo,
        boolean isPopular,
        MenuStatus menuStatus,
        MenuCategoryResponseDto category,
        List<String> tagNames) {

    public static MenuItemResponseDto fromEntity(MenuItem item, List<String> tagNames) {
        return new MenuItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getOrderNo(),
                item.isPopular(),
                item.getMenuStatus(),
                MenuCategoryResponseDto.fromEntity(item.getMenuCategory()),
                tagNames);
    }

    public static MenuItemResponseDto fromEntity(MenuItem item) {
        return fromEntity(item, List.of());
    }
}
