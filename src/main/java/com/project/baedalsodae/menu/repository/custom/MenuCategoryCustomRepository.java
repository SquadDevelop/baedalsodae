package com.project.baedalsodae.menu.repository.custom;

import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryItemsResponse;
import java.util.List;
import java.util.UUID;

public interface MenuCategoryCustomRepository {
    List<MenuCategoryItemsResponse> getStoreCategoryItems(UUID storeId);
}
