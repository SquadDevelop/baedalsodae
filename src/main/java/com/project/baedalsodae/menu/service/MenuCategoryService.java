package com.project.baedalsodae.menu.service;

import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import java.util.UUID;

public interface MenuCategoryService {

    MenuCategoryResponseDto updateMenuCategory(
            UUID menuCategoryId, MenuCategoryPutRequestDto request);

    void deleteMenuCategory(UUID menuCategoryId);

    MenuCategoryResponseDto updateMenuCategoryOrder(
            UUID menuCategoryId, MenuCategoryPatchRequestDto request);
}
