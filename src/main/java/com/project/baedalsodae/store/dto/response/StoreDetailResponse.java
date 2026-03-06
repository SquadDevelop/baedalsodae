package com.project.baedalsodae.store.dto.response;

import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryItemsResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreDetailResponse {
    private StoreSummaryResponse store;
    private List<MenuCategoryItemsResponse> storeMenuCategoryItems;

    public static StoreDetailResponse of(
            StoreSummaryResponse store,
            List<MenuCategoryItemsResponse> storeMenuCategoryItemsList) {
        return StoreDetailResponse.builder()
                .store(store)
                .storeMenuCategoryItems(storeMenuCategoryItemsList)
                .build();
    }
}
