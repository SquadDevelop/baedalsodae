package com.project.baedalsodae.store.service;

import com.project.baedalsodae.store.dto.request.CreateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.response.StoreCategoryDetailResponse;
import com.project.baedalsodae.store.dto.response.StoreCategoryListResponse;
import java.util.UUID;

public interface StoreCategoryService {
    StoreCategoryListResponse getActiveStoreCategories();

    StoreCategoryDetailResponse getStoreCategoryDetail(UUID storeCategoryId);

    void createStoreCategory(CreateStoreCategoryRequest request);

    void updateStoreCategory(UpdateStoreCategoryRequest request, UUID storeCategoryId);

    void deleteStoreCategory(UUID storeCategoryId);
}
