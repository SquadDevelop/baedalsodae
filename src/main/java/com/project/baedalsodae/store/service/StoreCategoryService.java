package com.project.baedalsodae.store.service;

import com.project.baedalsodae.store.dto.request.CreateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.request.PatchStoreCategoryRequest;
import com.project.baedalsodae.store.dto.response.StoreCategoryDetailResponse;
import com.project.baedalsodae.store.dto.response.StoreCategoryListResponse;

import java.util.UUID;

public interface StoreCategoryService {
    StoreCategoryListResponse storeCategoryList();
//    StoreCategoryListResponse storeCategoryList();
    StoreCategoryDetailResponse storeCategoryDetail(UUID storeCategoryId);
    void createStoreCategory(CreateStoreCategoryRequest request);
    void patchStoreCategory(PatchStoreCategoryRequest request, UUID storeCategoryId);
    void deleteStoreCategory(UUID storeCategoryId);
}
