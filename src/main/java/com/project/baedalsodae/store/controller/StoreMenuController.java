package com.project.baedalsodae.store.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPostRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.service.MenuCategoryService;
import com.project.baedalsodae.menu.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreMenuController {
    private final MenuCategoryService menuCategoryService;
    private final MenuItemService menuItemService;

    @PostMapping("/{storeId}/menu-categories")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> createMenuCategory(
            @PathVariable UUID storeId,
            @RequestBody @Valid MenuCategoryPostRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MenuCategoryResponseDto response = menuCategoryService.createMenuCategory(storeId, request, userDetails);
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
