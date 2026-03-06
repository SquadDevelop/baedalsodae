package com.project.baedalsodae.store.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryItemsResponse;
import com.project.baedalsodae.menu.repository.custom.MenuCategoryCustomRepository;
import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.dto.response.StoreDetailResponse;
import com.project.baedalsodae.store.dto.response.StorePageResponse;
import com.project.baedalsodae.store.dto.response.StoreResponse;
import com.project.baedalsodae.store.dto.response.StoreSummaryResponse;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.store.repository.custom.StoreCustomRepository;
import com.project.baedalsodae.store.service.StoreQueryService;

import java.util.List;
import java.util.UUID;

import com.project.baedalsodae.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreQueryServiceImpl implements StoreQueryService {
    private final StoreRepository storeRepository;
    private final StoreCustomRepository storeCustomRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final MenuCategoryCustomRepository menuCategoryCustomRepository;

    @Override
    public StorePageResponse getStorePage(
            UUID storeCategoryId, StoreCursorRequest cursorRequest, SortType sortType) {
        cursorRequest.normalize(sortType);

        StoreCategory storeCategory = getStoreCategory(storeCategoryId);

        Slice<Store> result =
                storeCustomRepository.findStoresByCursor(storeCategoryId, cursorRequest, sortType);

        return StorePageResponse.of(storeCategory.getId(), storeCategory.getName(), result);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetail(UUID storeId) {
        Store store = getStore(storeId);

        List<MenuCategoryItemsResponse> storeMenuCategoryItemsList
                = menuCategoryCustomRepository.getStoreCategoryItems(storeId);

        return StoreDetailResponse.of(StoreSummaryResponse.fromEntity(store), storeMenuCategoryItemsList);
    }

    @Override
    public StoreResponse getStoreForOwner(UUID storeId, UUID userId, String role) {
        Store store = getStore(storeId);
        validateStoreOwner(store.getUserId(), userId, role);

        return StoreResponse.fromEntity(store);
    }

    private StoreCategory getStoreCategory(UUID StoreCategoryId) {
        return storeCategoryRepository
                .findById(StoreCategoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_CATEGORY_NOT_FOUND));
    }

    private Store getStore(UUID storeId) {
        return storeRepository
                .findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }

    private void validateStoreOwner(UUID storeOwnerId, UUID userId, String role) {
        if (!userId.equals(storeOwnerId) || !role.equals(UserRole.OWNER.name())) {
            throw new BusinessException(ErrorCode.STORE_FORBIDDEN);
        }
    }
}
