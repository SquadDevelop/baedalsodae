package com.project.baedalsodae.store.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.store.dto.request.storeCategory.CreateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.request.storeCategory.UpdateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.response.storeCategory.StoreCategoryDetailResponse;
import com.project.baedalsodae.store.dto.response.storeCategory.StoreCategoryListResponse;
import com.project.baedalsodae.store.service.StoreCategoryService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store-categories")
public class StoreCategoryController {
    private final StoreCategoryService storeCategoryService;

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponse<StoreCategoryListResponse>>
            getStoreCategoryListForCustomer() {
        StoreCategoryListResponse response = storeCategoryService.getActiveStoreCategories();
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.STORE_CATEGORY_LIST_FOUND, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    @GetMapping("/{storeCategoryId}")
    public ResponseEntity<ApiResponse<StoreCategoryDetailResponse>> getStoreCategoryDetail(
            @PathVariable UUID storeCategoryId) {
        StoreCategoryDetailResponse response =
                storeCategoryService.getStoreCategoryDetail(storeCategoryId);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.STORE_CATEGORY_DETAIL_FOUND, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createStoreCategory(
            @RequestBody @Valid CreateStoreCategoryRequest request) {
        storeCategoryService.createStoreCategory(request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_CATEGORY_CREATED, null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    @PatchMapping("/{storeCategoryId}")
    public ResponseEntity<ApiResponse<Void>> updateStoreCategory(
            @RequestBody @Valid UpdateStoreCategoryRequest request,
            @PathVariable UUID storeCategoryId) {
        storeCategoryService.updateStoreCategory(request, storeCategoryId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_CATEGORY_PATCHED, null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    @DeleteMapping("/{storeCategoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteStoreCategory(
            @PathVariable UUID storeCategoryId) {
        storeCategoryService.deleteStoreCategory(storeCategoryId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_CATEGORY_DELETED, null));
    }
}
