package com.project.baedalsodae.store.service.impl;

import static com.project.baedalsodae.store.entity.enums.StoreStatus.PENDING_APPROVAL;
import static com.project.baedalsodae.store.entity.enums.StoreStatus.SUSPENDED;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.store.dto.request.CreateStoreRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreRequest;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.entity.enums.StoreStatus;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.store.service.StoreCommandService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreCommandServiceImpl implements StoreCommandService {
    private final StoreRepository storeRepository;
    private final StoreCategoryRepository storeCategoryRepository;

    @Override
    @Transactional
    public void createStore(CreateStoreRequest request, UUID userId) {
        if (storeRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new BusinessException(ErrorCode.STORE_DUPLICATED_BUSINESS_NUMBER);
        }

        StoreCategory storeCategory = getStoreCategory(request.getStoreCategoryId());
        Address address = request.getAddress().toEntity();
        Store store =
                Store.createStore(
                        userId,
                        storeCategory,
                        request.getStoreName(),
                        request.getBusinessNumber(),
                        request.getStorePhone(),
                        address,
                        request.getDescription());

        storeRepository.save(store);
    }

    @Override
    @Transactional
    public void updateStore(UpdateStoreRequest request, UUID storeId, UUID userId) {
        Store store = getStore(storeId);
        validateStoreOwner(store.getUserId(), userId);

        StoreCategory storeCategory = getStoreCategory(request.getStoreCategoryId());
        Address address = request.getAddress().toEntity();
        store.updateStore(
                storeCategory,
                request.getStoreName(),
                request.getStorePhone(),
                address,
                request.getDescription());
    }

    @Override
    @Transactional
    public void deleteStore(UUID storeId, UUID userId) {
        Store store = getStore(storeId);
        validateStoreOwner(store.getUserId(), userId);

        store.softDelete(userId);
    }

    @Override
    @Transactional
    public void updateStoreOpened(UUID storeId, StoreStatus status, UUID userId) {
        if (status == SUSPENDED || status == PENDING_APPROVAL) {
            throw new BusinessException(ErrorCode.STORE_STATUS_CHANGE_FORBIDDEN);
        }

        Store store = getStore(storeId);
        validateStoreOwner(store.getUserId(), userId);

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

    private void validateStoreOwner(UUID storeOwnerId, UUID userId) {
        if (!userId.equals(storeOwnerId)) {
            throw new BusinessException(ErrorCode.STORE_FORBIDDEN);
        }
    }
}
