package com.project.baedalsodae.menu.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPutRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPostRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.dto.responseDto.item.MenuItemResponseDto;
import com.project.baedalsodae.menu.service.MenuCategoryService;
import com.project.baedalsodae.menu.service.MenuItemService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu-categories")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;
    private final MenuItemService menuItemService;

    @PutMapping("/{menuCategoryId}")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> updateMenuCategory(
            @PathVariable UUID menuCategoryId, @Valid @RequestBody MenuCategoryPutRequestDto request) {
        MenuCategoryResponseDto response =
                menuCategoryService.updateMenuCategory(menuCategoryId, request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_CATEGORY_UPDATED, response));
    }

    @PatchMapping("/{menuCategoryId}/orders")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> updateMenuCategoryOrder(
            @PathVariable UUID menuCategoryId,
            @Valid @RequestBody MenuCategoryPatchRequestDto request) {
        MenuCategoryResponseDto response =
                menuCategoryService.updateMenuCategoryOrder(menuCategoryId, request);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MENU_CATEGORY_ORDER_UPDATED, response));
    }

    @DeleteMapping("/{menuCategoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuCategory(@PathVariable UUID menuCategoryId) {
        menuCategoryService.deleteMenuCategory(menuCategoryId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_CATEGORY_DELETED, null));
    }

    @GetMapping("/{menuCategoryId}/menu-items")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDto>>> getMenuItem(
            @PathVariable UUID menuCategoryId) {
        List<MenuItemResponseDto> response = menuItemService.getMenuItem(menuCategoryId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_ITEM_LIST_FOUND, response));
    }

    @PostMapping("/{menuCategoryId}/menu-items")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> createMenuItem(
            @PathVariable UUID menuCategoryId, @Valid @RequestBody MenuItemPostRequestDto request) {
        MenuItemResponseDto response = menuItemService.createMenuItem(menuCategoryId, request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_ITEM_CREATED, response));
    }
}
