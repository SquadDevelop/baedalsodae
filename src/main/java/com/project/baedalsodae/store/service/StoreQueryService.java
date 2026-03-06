package com.project.baedalsodae.store.service;

import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.dto.response.StoreDetailResponse;
import com.project.baedalsodae.store.dto.response.StorePageResponse;
import com.project.baedalsodae.store.dto.response.StoreResponse;
import com.project.baedalsodae.store.entity.enums.SortType;

import java.util.UUID;

public interface StoreQueryService {
    StoreDetailResponse getStoreDetail(UUID storeId);
    StoreResponse getStoreForOwner(UUID storeId, UUID userId, String role);

    StorePageResponse getStorePage(
            UUID storeCategoryId, StoreCursorRequest cursorRequest, SortType sortType);
}
