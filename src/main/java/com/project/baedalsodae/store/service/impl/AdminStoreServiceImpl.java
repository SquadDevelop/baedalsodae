package com.project.baedalsodae.store.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.store.dto.request.store.UpdateStoreRequest;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.entity.enums.StoreStatus;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.store.service.AdminStoreService;
import com.project.baedalsodae.store.service.StoreHoursService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminStoreServiceImpl implements AdminStoreService {
    private final StoreRepository storeRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final StoreHoursService storeHoursService;

    @Override
    public void updateStore(UpdateStoreRequest request, UUID storeId) {
        Store store = getStore(storeId);

        StoreCategory storeCategory = getStoreCategory(request.getStoreCategoryId());
        Address address = request.getAddress().toEntity();
        store.updateInfo(
                storeCategory,
                request.getStoreName(),
                request.getStorePhone(),
                address,
                request.getDescription());
    }

    @Override
    public void deleteStore(UUID storeId, UUID userId) {
        Store store = getStore(storeId);
        storeHoursService.bulkDeleteStoreHours(storeId);

        store.softDelete(userId);
    }

    @Override
    public void updateStoreStatus(UUID storeId, StoreStatus status) {
        Store store = getStore(storeId);
        store.patchStoreOpened(status);
    }

    private Store getStore(UUID storeId) {
        return storeRepository
                .findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }

    private StoreCategory getStoreCategory(UUID storeCategoryId) {
        return storeCategoryRepository
                .findById(storeCategoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_CATEGORY_NOT_FOUND));
    }
}
