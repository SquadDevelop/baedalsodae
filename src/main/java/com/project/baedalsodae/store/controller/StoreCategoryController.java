package com.project.baedalsodae.store.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.entity.SuccessCode;
import com.project.baedalsodae.store.dto.request.CreateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.request.PatchStoreCategoryRequest;
import com.project.baedalsodae.store.dto.response.StoreCategoryDetailResponse;
import com.project.baedalsodae.store.dto.response.StoreCategoryListResponse;
import com.project.baedalsodae.store.service.StoreCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store-categories")
public class StoreCategoryController {
    private final StoreCategoryService storeCategoryService;

    // TODO 인증 도메인 완료되면, userId 추가
    @GetMapping
    public ResponseEntity<ApiResponse<StoreCategoryListResponse>> getStoreCategoryListForCustomer() {
        StoreCategoryListResponse response = storeCategoryService.getActiveStoreCategories();
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_CATEGORY_LIST_FOUND, response));
    }

    @GetMapping("/{storeCategoryId}")
    public ResponseEntity<ApiResponse<StoreCategoryDetailResponse>> getStoreCategoryDetail(
            @PathVariable UUID storeCategoryId) {
        StoreCategoryDetailResponse response = storeCategoryService.getStoreCategoryDetail(storeCategoryId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_CATEGORY_DETAIL_FOUND, response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createStoreCategory(
            @RequestBody @Valid CreateStoreCategoryRequest request) {
        storeCategoryService.createStoreCategory(request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_CATEGORY_CREATED, null));
    }

    @PatchMapping("/{storeCategoryId}")
    public ResponseEntity<ApiResponse<Void>> patchStoreCategory(
            @RequestBody @Valid PatchStoreCategoryRequest request, @PathVariable UUID storeCategoryId) {
        storeCategoryService.patchStoreCategory(request, storeCategoryId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_CATEGORY_PATCHED, null));
    }

    @DeleteMapping("/{storeCategoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteStoreCategory(
            @PathVariable UUID storeCategoryId) {
        storeCategoryService.deleteStoreCategory(storeCategoryId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_CATEGORY_DELETED, null));
    }
}
