package com.project.baedalsodae.store.service;

import com.project.baedalsodae.store.dto.request.storeCategory.CreateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.request.storeCategory.UpdateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.response.storeCategory.StoreCategoryDetailResponse;
import com.project.baedalsodae.store.dto.response.storeCategory.StoreCategoryListResponse;
import java.util.UUID;

public interface StoreCategoryService {
    StoreCategoryListResponse getActiveStoreCategories();

    StoreCategoryDetailResponse getStoreCategoryDetail(UUID storeCategoryId);

    void createStoreCategory(CreateStoreCategoryRequest request);

    void updateStoreCategory(UpdateStoreCategoryRequest request, UUID storeCategoryId);

    void deleteStoreCategory(UUID storeCategoryId);
}
