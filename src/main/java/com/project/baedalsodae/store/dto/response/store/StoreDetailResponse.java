package com.project.baedalsodae.store.dto.response.store;

import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryItemsResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreDetailResponse {
    private StoreSummaryResponse store;
    private List<MenuCategoryItemsResponse> storeMenuCategoryItems;
    private boolean isAllowedRegion;
    private boolean isDeliverableToUser;


    public static StoreDetailResponse of(
            StoreSummaryResponse store,
            List<MenuCategoryItemsResponse> storeMenuCategoryItemsList,
            boolean isAllowedRegion, boolean isDeliverableToUser) {
        return StoreDetailResponse.builder()
                .store(store)
                .storeMenuCategoryItems(storeMenuCategoryItemsList)
                .isAllowedRegion(isAllowedRegion)
                .isDeliverableToUser(isDeliverableToUser)
                .build();
    }
}
