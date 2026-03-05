package com.project.baedalsodae.store.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.store.dto.request.CreateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.request.PatchStoreCategoryRequest;
import com.project.baedalsodae.store.dto.response.StoreCategoryDetailResponse;
import com.project.baedalsodae.store.dto.response.StoreCategoryListResponse;
import com.project.baedalsodae.store.dto.response.StoreCategoryResponse;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.service.StoreCategoryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreCategoryServiceImpl implements StoreCategoryService {
    private final StoreCategoryRepository storeCategoryRepository;

    @Override
    public StoreCategoryListResponse getActiveStoreCategories() {
        List<StoreCategoryResponse> storeCategoryResponseList =
                storeCategoryRepository.findAllByIsDeletedFalse().stream()
                        .map(StoreCategoryResponse::fromEntity)
                        .toList();

        return StoreCategoryListResponse.fromList(storeCategoryResponseList);
    }

    @Override
    public StoreCategoryDetailResponse getStoreCategoryDetail(UUID storeCategoryId) {
        StoreCategory storeCategory = getStoreCategory(storeCategoryId);
        return StoreCategoryDetailResponse.fromEntity(storeCategory);
    }

    @Override
    @Transactional
    public void createStoreCategory(CreateStoreCategoryRequest request) {
        StoreCategory storeCategory =
                StoreCategory.createStoreCategory(request.getName(), request.getDescription());
        storeCategoryRepository.save(storeCategory);
    }

    @Override
    @Transactional
    public void patchStoreCategory(PatchStoreCategoryRequest request, UUID storeCategoryId) {
        StoreCategory storeCategory = getStoreCategory(storeCategoryId);
        storeCategory.patchStoreCategory(request.getName(), request.getDescription());
    }

    @Override
    @Transactional
    public void deleteStoreCategory(UUID storeCategoryId) {
        StoreCategory storeCategory = getStoreCategory(storeCategoryId);
        storeCategory.softDelete(storeCategoryId);
    }

    private StoreCategory getStoreCategory(UUID StoreCategoryId) {
        return storeCategoryRepository
                .findById(StoreCategoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_CATEGORY_NOT_FOUND));
    }
}
