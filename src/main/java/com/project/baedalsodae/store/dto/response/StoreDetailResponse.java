package com.project.baedalsodae.store.dto.response;

import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryItemsResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoreDetailResponse {
    private StoreSummaryResponse store;
    private List<MenuCategoryItemsResponse> storeMenuCategoryItems;

    public static StoreDetailResponse of(
            StoreSummaryResponse store, List<MenuCategoryItemsResponse> storeMenuCategoryItemsList) {
        return StoreDetailResponse.builder()
                .store(store)
                .storeMenuCategoryItems(storeMenuCategoryItemsList)
                .build();
    }
}
