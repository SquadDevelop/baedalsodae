package com.project.baedalsodae.store.service;

import com.project.baedalsodae.store.dto.request.store.StoreCursorRequest;
import com.project.baedalsodae.store.dto.response.store.*;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface StoreQueryService {
    StoreSearchPageResponse getStoreByKeyword(String keyword, Pageable pageable, SortType sortType);

    StoreDetailResponse getStoreDetail(UUID storeId, UUID userId);

    OwnerStoreResponse getOwnerStore(UUID storeId, UUID userId, UserRole role);

    StorePageResponse getStorePage(UUID storeCategoryId, StoreCursorRequest cursorRequest);

    StoreReviewResponse getStoreReview(UUID storeId, UUID userId);
}
