package com.project.baedalsodae.store.service.impl;

import com.project.baedalsodae.allowedRegion.service.AllowedRegionService;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryItemsResponse;
import com.project.baedalsodae.menu.repository.custom.MenuCategoryCustomRepository;
import com.project.baedalsodae.review.dto.query.ReviewSummary;
import com.project.baedalsodae.review.dto.response.ReviewDetailResponse;
import com.project.baedalsodae.review.repository.custom.ReviewCustomRepository;
import com.project.baedalsodae.store.dto.request.store.StoreCursorRequest;
import com.project.baedalsodae.store.dto.response.store.*;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.store.repository.custom.StoreCustomRepository;
import com.project.baedalsodae.store.service.StoreQueryService;
import com.project.baedalsodae.user.entity.UserAddress;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.service.UserAddressService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreQueryServiceImpl implements StoreQueryService {
    private final StoreRepository storeRepository;
    private final StoreCustomRepository storeCustomRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final MenuCategoryCustomRepository menuCategoryCustomRepository;
    private final ReviewCustomRepository reviewCustomRepository;
    private final AllowedRegionService allowedRegionService;
    private final UserAddressService userAddressService;

    @Override
    public StorePageResponse getStorePage(UUID storeCategoryId, StoreCursorRequest cursorRequest) {
        SortType sortType = cursorRequest.sortType();
        validateSortType(sortType);
        StoreCursorRequest initializedCursor = cursorRequest.initCursor(sortType);

        StoreCategory storeCategory = getStoreCategory(storeCategoryId);

        Slice<Store> storeSlice =
                storeCustomRepository.findStoresByCursor(
                        storeCategoryId, initializedCursor, sortType);

        return StorePageResponse.of(storeCategory.getId(), storeCategory.getName(), storeSlice);
    }

    @Override
    public StoreReviewResponse getStoreReview(UUID storeId, UUID userId) {
        getStore(storeId);
        List<ReviewDetailResponse> reviews = reviewCustomRepository.findByStoreId(storeId, userId);
        ReviewSummary reviewSummary = reviewCustomRepository.getSummary(storeId);
        return StoreReviewResponse.of(reviews, reviewSummary);
    }

    @Override
    public StoreSearchPageResponse getStoreByKeyword(
            String keyword, Pageable pageable, SortType sortType) {
        validateSortType(sortType);
        List<Store> content =
                storeCustomRepository.searchStoreByKeyword(keyword, pageable, sortType);

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) content.remove(content.size() - 1);

        Long totalCount = null;
        if (pageable.getPageNumber() == 0) {
            totalCount = storeCustomRepository.countStoresByKeyword(keyword);
        }

        return StoreSearchPageResponse.of(content, totalCount, pageable, hasNext);
    }

    @Override
    public StoreDetailResponse getStoreDetail(UUID storeId, UUID userId) {
        Store store = getStore(storeId);

        String sigunguCode = store.getAddress().getSigunguCode();
        boolean isAllowedRegion = allowedRegionService.isAllowedByCode(sigunguCode);
        List<MenuCategoryItemsResponse> storeMenuCategoryItemsList =
                menuCategoryCustomRepository.getStoreCategoryItems(storeId);
        UserAddress userAddress = userAddressService.getMainUserAddress(userId);
        boolean isDeliverable =
                allowedRegionService.isAllowedByCode(userAddress.getAddress().getSigunguCode());

        return StoreDetailResponse.of(
                StoreSummaryResponse.fromEntity(store),
                storeMenuCategoryItemsList,
                isAllowedRegion,
                isDeliverable);
    }

    @Override
    public OwnerStoreResponse getOwnerStore(UUID storeId, UUID userId, UserRole role) {
        Store store = getStore(storeId);
        validateStoreOwner(store.getUserId(), userId, role);

        return OwnerStoreResponse.fromEntity(store);
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

    private void validateStoreOwner(UUID storeOwnerId, UUID userId, UserRole role) {
        if (!userId.equals(storeOwnerId) || !role.getRole().equals(UserRole.OWNER.getRole())) {
            throw new BusinessException(ErrorCode.STORE_FORBIDDEN);
        }
    }

    private void validateSortType(SortType sortType) {
        if (!sortType.isStoreListSort()) {
            throw new BusinessException(ErrorCode.SORT_UNSUPPORTED);
        }
    }
}
