package com.project.baedalsodae.menu.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.menu.dto.requestDto.MenuRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.MenuResponseDto;
import com.project.baedalsodae.menu.service.MenuItemService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

  private final MenuItemService menuItemService;

  @PutMapping("/{menuItemId}")
  public ResponseEntity<ApiResponse<MenuResponseDto.MenuItemResponse>> updateMenuItem(
      @PathVariable UUID menuItemId, @RequestBody MenuRequestDto.MenuRequest request) {
    MenuResponseDto.MenuItemResponse response = menuItemService.updateMenuItem(menuItemId, request);
    return ResponseEntity.ok(ApiResponse.success("", response));
  }
}
