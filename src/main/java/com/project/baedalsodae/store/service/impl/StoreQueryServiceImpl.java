package com.project.baedalsodae.store.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.dto.response.StorePageResponse;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.repository.custom.StoreCustomRepository;
import com.project.baedalsodae.store.service.StoreQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreQueryServiceImpl implements StoreQueryService {
    private final StoreCustomRepository storeCustomRepository;
    private final StoreCategoryRepository storeCategoryRepository;

    @Override
    public void getStoreDetail(UUID storeId, UUID userId) {}

    @Override
    public StorePageResponse getStorePage(
            UUID storeCategoryId, StoreCursorRequest cursorRequest, SortType sortType) {
        cursorRequest.validate(sortType);

        StoreCategory storeCategory = getStoreCategory(storeCategoryId);

        Slice<Store> result =
                storeCustomRepository.findStoresByCursor(storeCategoryId, cursorRequest, sortType);

        return StorePageResponse.of(storeCategory.getId(), storeCategory.getName(), result);
    }

    private StoreCategory getStoreCategory(UUID StoreCategoryId) {
        return storeCategoryRepository
                .findById(StoreCategoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_CATEGORY_NOT_FOUND));
    }
}
