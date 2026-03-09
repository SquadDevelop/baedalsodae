package com.project.baedalsodae.store.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.store.dto.request.store.CreateStoreRequest;
import com.project.baedalsodae.store.dto.request.store.StoreCursorRequest;
import com.project.baedalsodae.store.dto.request.store.UpdateStoreRequest;
import com.project.baedalsodae.store.dto.request.store.UpdateStoreStatusRequest;
import com.project.baedalsodae.store.dto.response.store.*;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.store.service.StoreCommandService;
import com.project.baedalsodae.store.service.StoreQueryService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {
    private final StoreCommandService storeCommandService;
    private final StoreQueryService storeQueryService;

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MANAGER')")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<StorePageResponse>> getStorePageByStoreCategory(
            @RequestParam UUID storeCategoryId, @ModelAttribute StoreCursorRequest cursorRequest) {
        StorePageResponse response = storeQueryService.getStorePage(storeCategoryId, cursorRequest);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_LIST_FOUND, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MANAGER')")
    @GetMapping("/keywords")
    public ResponseEntity<ApiResponse<StoreSearchPageResponse>> getStorePageByKeyword(
            @RequestParam String keyword, @RequestParam SortType sortType, Pageable pageable) {
        StoreSearchPageResponse response =
                storeQueryService.getStoreByKeyword(keyword, pageable, sortType);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.STORE_LIST_FOUND_BY_KEYWORD, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MANAGER')")
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("storeId") UUID storeId) {
        StoreDetailResponse response =
                storeQueryService.getStoreDetail(storeId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_DETAIL_FOUND, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MANAGER')")
    @GetMapping("/{storeId}/reviews")
    public ResponseEntity<ApiResponse<StoreReviewResponse>> getStoreReviews(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("storeId") UUID storeId) {
        StoreReviewResponse response =
                storeQueryService.getStoreReview(storeId, userDetails.getUserId());
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.STORE_REVIEW_LIST_FOUND, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_MANAGER')")
    @GetMapping("/{storeId}/manage")
    public ResponseEntity<ApiResponse<OwnerStoreResponse>> getOwnerStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("storeId") UUID storeId) {
        OwnerStoreResponse response =
                storeQueryService.getOwnerStore(
                        storeId, userDetails.getUserId(), userDetails.getUserRole());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_FOUND_FOR_OWNER, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid CreateStoreRequest request) {
        storeCommandService.createStore(request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_CREATED, null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER')")
    @PatchMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> updateStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid UpdateStoreRequest request,
            @PathVariable UUID storeId) {
        storeCommandService.updateStore(request, storeId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_UPDATED, null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER')")
    @PatchMapping("/{storeId}/status")
    public ResponseEntity<ApiResponse<Void>> updateStoreStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid UpdateStoreStatusRequest request,
            @PathVariable UUID storeId) {
        storeCommandService.updateStoreOpened(
                storeId, request.getStatus(), userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_STATUS_UPDATED, null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER')")
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable UUID storeId) {
        storeCommandService.deleteStore(storeId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_DELETED, null));
    }
}
