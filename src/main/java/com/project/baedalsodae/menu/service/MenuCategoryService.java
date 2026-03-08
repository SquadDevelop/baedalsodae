package com.project.baedalsodae.menu.service;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPostRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import java.util.List;
import java.util.UUID;

public interface MenuCategoryService {

    MenuCategoryResponseDto createMenuCategory(
            UUID storeId, MenuCategoryPostRequestDto request, UserDetailsImpl userDetails);

    MenuCategoryResponseDto updateMenuCategory(
            UUID menuCategoryId, MenuCategoryPutRequestDto request, UserDetailsImpl userDetails);

    void deleteMenuCategory(UUID menuCategoryId, UserDetailsImpl userDetails);

    MenuCategoryResponseDto updateMenuCategoryOrder(
            UUID menuCategoryId, MenuCategoryPatchRequestDto request, UserDetailsImpl userDetails);

    List<MenuCategoryResponseDto> getMenuCategories(UUID storeId);

    boolean isDuplicateMenuCategoryName(UUID storeId, String name);
}
