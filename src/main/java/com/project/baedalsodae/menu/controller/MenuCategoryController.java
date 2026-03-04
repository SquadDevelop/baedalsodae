package com.project.baedalsodae.menu.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.service.MenuCategoryService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/menu-categories")
@RequiredArgsConstructor
public class MenuCategoryController {

  private final MenuCategoryService menuCategoryService;

  @PutMapping("/{menuCategoryId}")
  public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> updateMenuCategory(
      @PathVariable UUID menuCategoryId, @RequestBody MenuCategoryPutRequestDto request) {
    MenuCategoryResponseDto response =
        menuCategoryService.updateMenuCategory(menuCategoryId, request);
    return ResponseEntity.ok(ApiResponse.success("", response));
  }

  @PatchMapping("/{menuCategoryId}/orders")
  public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> updateMenuCategoryOrder(
      @PathVariable UUID menuCategoryId, @Valid @RequestBody MenuCategoryPatchRequestDto request) {
    MenuCategoryResponseDto response =
        menuCategoryService.updateMenuCategoryOrder(menuCategoryId, request);
    return ResponseEntity.ok(ApiResponse.success("", response));
  }

  @DeleteMapping("/{menuCategoryId}")
  public ResponseEntity<ApiResponse<Void>> deleteMenuCategory(@PathVariable UUID menuCategoryId) {
    menuCategoryService.deleteMenuCategory(menuCategoryId);
    return ResponseEntity.ok(ApiResponse.success(""));
  }
}
