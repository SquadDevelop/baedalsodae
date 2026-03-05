package com.project.baedalsodae.menu.controller;

import com.project.baedalsodae.global.common.ApiResponse;
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
            @PathVariable UUID menuCategoryId, @RequestBody MenuCategoryPutRequestDto request) {
        MenuCategoryResponseDto response =
                menuCategoryService.updateMenuCategory(menuCategoryId, request);
        return ResponseEntity.ok(ApiResponse.success("", response));
    }

    @PatchMapping("/{menuCategoryId}/orders")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> updateMenuCategoryOrder(
            @PathVariable UUID menuCategoryId,
            @Valid @RequestBody MenuCategoryPatchRequestDto request) {
        MenuCategoryResponseDto response =
                menuCategoryService.updateMenuCategoryOrder(menuCategoryId, request);
        return ResponseEntity.ok(ApiResponse.success("", response));
    }

    @DeleteMapping("/{menuCategoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuCategory(@PathVariable UUID menuCategoryId) {
        menuCategoryService.deleteMenuCategory(menuCategoryId);
        return ResponseEntity.ok(ApiResponse.success(""));
    }

    @GetMapping("/{menuCategoryId}/menu-items")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDto>>> getMenuItem(
            @PathVariable UUID menuCategoryId) {
        List<MenuItemResponseDto> response = menuItemService.getMenuItem(menuCategoryId);
        return ResponseEntity.ok(ApiResponse.success("", response));
    }

    @PostMapping("/{menuCategoryId}/menu-items")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> createMenuItem(
            @PathVariable UUID menuCategoryId, @RequestBody MenuItemPostRequestDto request) {
        MenuItemResponseDto response = menuItemService.createMenuItem(menuCategoryId, request);
        return ResponseEntity.ok(ApiResponse.success("", response));
    }

    @GetMapping("/{menuCategoryId}/menu-items/duplicate-check")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicateMenuItemName(
            @PathVariable UUID menuCategoryId, @RequestParam String name) {
        boolean isDuplicate = menuItemService.isDuplicateMenuItemName(menuCategoryId, name);
        return ResponseEntity.ok(ApiResponse.success("", isDuplicate));
    }
}
