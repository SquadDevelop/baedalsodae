package com.project.baedalsodae.menu.dto.responseDto.category;

import com.project.baedalsodae.menu.entity.MenuItem;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MenuCategoryItemsResponse {
    private MenuCategoryResponseDto menuCategory;
    private List<MenuItemSummary> menuItems;

    public static MenuCategoryItemsResponse of(
            MenuCategoryResponseDto menuCategory, List<MenuItem> menuItems) {
        return MenuCategoryItemsResponse.builder()
                .menuCategory(menuCategory)
                .menuItems(MenuItemSummary.fromList(menuItems))
                .build();
    }

    @Getter
    @Builder
    public static class MenuItemSummary {
        private UUID id;
        private String name;
        private int price;
        private String description;
        private int orderNo;
        private boolean popular;

        public static MenuItemSummary fromEntity(MenuItem menuItem) {
            return MenuItemSummary.builder()
                    .id(menuItem.getId())
                    .name(menuItem.getName())
                    .price(menuItem.getPrice())
                    .description(menuItem.getDescription())
                    .orderNo(menuItem.getOrderNo())
                    .popular(menuItem.isPopular())
                    .build();
        }

        public static List<MenuItemSummary> fromList(List<MenuItem> menuItems) {
            return menuItems.stream().map(MenuItemSummary::fromEntity).toList();
        }
    }
}
