package com.project.baedalsodae.store.service;

import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.dto.response.StorePageResponse;
import com.project.baedalsodae.store.entity.enums.SortType;
import java.util.UUID;

public interface StoreQueryService {
    void getStoreDetail(UUID storeId, UUID userId);
    StorePageResponse getStorePage(
            UUID storeCategoryId, StoreCursorRequest cursorRequest, SortType sortType);
}
