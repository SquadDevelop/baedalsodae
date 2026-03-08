package com.project.baedalsodae.menu.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.item.MenuItemResponseDto;
import com.project.baedalsodae.menu.service.MenuItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @PutMapping("/{menuItemId}")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> updateMenuItem(
            @PathVariable UUID menuItemId,
            @Valid @RequestBody MenuItemPutRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MenuItemResponseDto response =
                menuItemService.updateMenuItem(menuItemId, request, userDetails);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_ITEM_UPDATED, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @PatchMapping("/{menuItemId}")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> patchMenuItem(
            @PathVariable UUID menuItemId,
            @Valid @RequestBody MenuItemPatchRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MenuItemResponseDto response =
                menuItemService.patchMenuItem(menuItemId, request, userDetails);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_ITEM_UPDATED, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @PatchMapping("/{menuItemId}/orders")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> updateMenuItemOrder(
            @PathVariable UUID menuItemId,
            @RequestParam @Validated @Positive Integer order,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MenuItemResponseDto response =
                menuItemService.updateMenuItemOrder(menuItemId, order, userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.MENU_ITEM_ORDER_UPDATED, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @DeleteMapping("/{menuItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(
            @PathVariable UUID menuItemId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        menuItemService.deleteMenuItem(menuItemId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.MENU_ITEM_DELETED, null));
    }
}
