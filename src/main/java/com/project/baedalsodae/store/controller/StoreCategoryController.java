package com.project.baedalsodae.store.controller;

import com.project.baedalsodae.global.common.ApiResponse;
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

    @GetMapping
    public ResponseEntity<ApiResponse<StoreCategoryListResponse>> getStoreCategoryListForCustomer() {
        StoreCategoryListResponse response = storeCategoryService.getActiveStoreCategories();
        return ResponseEntity.ok(ApiResponse.success("Store categories retrieved.", response));
    }

    @GetMapping("{/storeCategoryId}")
    public ResponseEntity<ApiResponse<StoreCategoryDetailResponse>> getStoreCategoryDetail(
            @PathVariable UUID storeCategoryId) {
        StoreCategoryDetailResponse response = storeCategoryService.getStoreCategoryDetail(storeCategoryId);
        return ResponseEntity.ok(ApiResponse.success("Store category detail retrieved.", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createStoreCategory(
            @RequestBody @Valid CreateStoreCategoryRequest request) {
        storeCategoryService.createStoreCategory(request);
        return ResponseEntity.ok(ApiResponse.success("Store category created."));
    }

    @PatchMapping("{/storeCategoryId}")
    public ResponseEntity<ApiResponse<String>> patchStoreCategory(
            @RequestBody @Valid PatchStoreCategoryRequest request, @PathVariable UUID storeCategoryId) {
        storeCategoryService.patchStoreCategory(request, storeCategoryId);
        return ResponseEntity.ok(ApiResponse.success("Store category patched."));
    }

    @DeleteMapping("{/storeCategoryId}")
    public ResponseEntity<ApiResponse<String>> deleteStoreCategory(
            @PathVariable UUID storeCategoryId) {
        storeCategoryService.deleteStoreCategory(storeCategoryId);
        return ResponseEntity.ok(ApiResponse.success("Store category deleted."));
    }
}
