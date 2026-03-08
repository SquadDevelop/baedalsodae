package com.project.baedalsodae.store.service;

import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.dto.response.StoreDetailResponse;
import com.project.baedalsodae.store.dto.response.StorePageResponse;
import com.project.baedalsodae.store.dto.response.OwnerStoreResponse;
import com.project.baedalsodae.store.dto.response.StoreSearchPageResponse;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.user.entity.UserRole;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoreQueryService {
    StoreSearchPageResponse getStoreByKeyword(String keyword, Pageable pageable, SortType sortType);

    StoreDetailResponse getStoreDetail(UUID storeId);

    OwnerStoreResponse getOwnerStore(UUID storeId, UUID userId, UserRole role);

    StorePageResponse getStorePage(
            UUID storeCategoryId, StoreCursorRequest cursorRequest);
}
