package com.project.baedalsodae.store.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPostRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.service.MenuCategoryService;
import com.project.baedalsodae.menu.service.MenuItemService;
import com.project.baedalsodae.store.dto.request.CreateStoreRequest;
import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreStatusRequest;
import com.project.baedalsodae.store.dto.response.StoreDetailResponse;
import com.project.baedalsodae.store.dto.response.StorePageResponse;
import com.project.baedalsodae.store.dto.response.StoreResponse;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.store.service.StoreCommandService;
import com.project.baedalsodae.store.service.StoreQueryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
    private final MenuCategoryService menuCategoryService;
    private final MenuItemService menuItemService;

    // TODO 인증 도메인 완료되면, userId 추가
    @GetMapping
    public ResponseEntity<ApiResponse<StorePageResponse>> getStorePageByStoreCategory(
            @RequestParam UUID storeCategoryId,
            @ModelAttribute StoreCursorRequest cursorRequest,
            @RequestParam SortType sortType) {
        StorePageResponse response =
                storeQueryService.getStorePage(storeCategoryId, cursorRequest, sortType);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_LIST_FOUND, response));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreDetail(
            @PathVariable("storeId") UUID storeId) {
        StoreDetailResponse response = storeQueryService.getStoreDetail(storeId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_DETAIL_FOUND, response));
    }

    @GetMapping("/{storeId}/me")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreForOwner(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable("storeId") UUID storeId) {
        StoreResponse response = storeQueryService.getStoreForOwner(storeId, userId, role);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_FOUND_FOR_OWNER, response));
    }

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

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @PostMapping("/{storeId}/menu-categories")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> createMenuCategory(
            @PathVariable UUID storeId,
            @RequestBody @Valid MenuCategoryPostRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MenuCategoryResponseDto response =
                menuCategoryService.createMenuCategory(storeId, request, userDetails);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_CATEGORY_CREATED, response));
    }

    @GetMapping("/{storeId}/menu-categories")
    public ResponseEntity<ApiResponse<List<MenuCategoryResponseDto>>> getMenuCategories(
            @PathVariable UUID storeId) {
        List<MenuCategoryResponseDto> response = menuCategoryService.getMenuCategories(storeId);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MENU_CATEGORY_LIST_FOUND, response));
    }

    @GetMapping("/{storeId}/menu-categories/duplicate-check")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicateMenuCategoryName(
            @PathVariable UUID storeId, @RequestParam String name) {
        boolean isDuplicate = menuCategoryService.isDuplicateMenuCategoryName(storeId, name);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MENU_CATEGORY_NAME_DUPLICATE_CHECKED, isDuplicate));
    }

    @GetMapping("/{storeId}/menu-items/duplicate-check")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicateMenuItemName(
            @PathVariable UUID storeId, @RequestParam String name) {
        boolean isDuplicate = menuItemService.isDuplicateMenuItemName(storeId, name);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MENU_ITEM_NAME_DUPLICATE_CHECKED, isDuplicate));
    }
}
