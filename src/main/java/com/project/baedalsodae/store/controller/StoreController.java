package com.project.baedalsodae.store.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPostRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.service.MenuCategoryService;
import com.project.baedalsodae.store.dto.request.CreateStoreRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreStatusRequest;
import com.project.baedalsodae.store.service.StoreCommandService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {
    private final StoreCommandService storeCommandService;
    private final MenuCategoryService menuCategoryService;

    // TODO 인증 도메인 완료되면, userId 추가
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createStore(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid CreateStoreRequest request) {
        storeCommandService.createStore(request, userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_CREATED, null));
    }

    @PatchMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> updateStore(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid UpdateStoreRequest request,
            @PathVariable UUID storeId) {
        storeCommandService.updateStore(request, storeId, userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_UPDATED, null));
    }

    @PatchMapping("/{storeId}/status")
    public ResponseEntity<ApiResponse<Void>> updateStoreStatus(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid UpdateStoreStatusRequest request,
            @PathVariable UUID storeId) {
        storeCommandService.updateStoreOpened(storeId, request.getStatus(), userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_STATUS_UPDATED, null));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
            @RequestHeader("X-User-Id") UUID userId, @PathVariable UUID storeId) {
        storeCommandService.deleteStore(storeId, userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_DELETED, null));
    }

    @PostMapping("/{storeId}/menu-categories")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> createMenuCategory(
            @PathVariable UUID storeId, @RequestBody @Valid MenuCategoryPostRequestDto request) {
        MenuCategoryResponseDto response = menuCategoryService.createMenuCategory(storeId, request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_CATEGORY_CREATED, response));
    }
}
