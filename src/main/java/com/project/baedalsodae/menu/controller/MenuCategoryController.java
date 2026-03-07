package com.project.baedalsodae.menu.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu-categories")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;
    private final MenuItemService menuItemService;

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @PutMapping("/{menuCategoryId}")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> updateMenuCategory(
            @PathVariable UUID menuCategoryId,
            @Valid @RequestBody MenuCategoryPutRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MenuCategoryResponseDto response =
                menuCategoryService.updateMenuCategory(menuCategoryId, request, userDetails);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_CATEGORY_UPDATED, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @PatchMapping("/{menuCategoryId}/orders")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> updateMenuCategoryOrder(
            @PathVariable UUID menuCategoryId,
            @Valid @RequestBody MenuCategoryPatchRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MenuCategoryResponseDto response =
                menuCategoryService.updateMenuCategoryOrder(menuCategoryId, request, userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MENU_CATEGORY_ORDER_UPDATED, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @DeleteMapping("/{menuCategoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuCategory(
            @PathVariable UUID menuCategoryId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        menuCategoryService.deleteMenuCategory(menuCategoryId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_CATEGORY_DELETED, null));
    }

    @GetMapping("/{menuCategoryId}/menu-items")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDto>>> getMenuItem(
            @PathVariable UUID menuCategoryId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<MenuItemResponseDto> response =
                menuItemService.getMenuItem(menuCategoryId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_ITEM_LIST_FOUND, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @PostMapping("/{menuCategoryId}/menu-items")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> createMenuItem(
            @PathVariable UUID menuCategoryId,
            @Valid @RequestBody MenuItemPostRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MenuItemResponseDto response =
                menuItemService.createMenuItem(menuCategoryId, request, userDetails);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_ITEM_CREATED, response));
    }
}
