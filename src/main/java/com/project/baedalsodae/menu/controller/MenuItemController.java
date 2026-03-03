package com.project.baedalsodae.menu.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.item.MenuItemResponseDto;
import com.project.baedalsodae.menu.service.MenuItemService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

  private final MenuItemService menuItemService;

  @PutMapping("/{menuItemId}")
  public ResponseEntity<ApiResponse<MenuItemResponseDto>> updateMenuItem(
      @PathVariable UUID menuItemId, @RequestBody MenuItemPutRequestDto request) {
    MenuItemResponseDto response = menuItemService.updateMenuItem(menuItemId, request);
    return ResponseEntity.ok(ApiResponse.success("", response));
  }

  @PatchMapping("/{menuItemId}")
  public ResponseEntity<ApiResponse<MenuItemResponseDto>> patchMenuItem(
      @PathVariable UUID menuItemId, @RequestBody MenuItemPatchRequestDto request) {
    MenuItemResponseDto response = menuItemService.patchMenuItem(menuItemId, request);
    return ResponseEntity.ok(ApiResponse.success("", response));
  }

  @DeleteMapping("/{menuItemId}")
  public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable UUID menuItemId) {
    menuItemService.deleteMenuItem(menuItemId);
    return ResponseEntity.ok(ApiResponse.success(""));
  }
}
