package com.project.baedalsodae.menu.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.common.OrderUtil;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.repository.MenuCategoryRepository;
import com.project.baedalsodae.menu.service.MenuCategoryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuCategoryServiceImpl implements MenuCategoryService {

  private final MenuCategoryRepository menuCategoryRepository;

  @Override
  @Transactional
  public MenuCategoryResponseDto updateMenuCategory(
      UUID menuCategoryId, MenuCategoryPutRequestDto request) {
    MenuCategory menuCategory =
        menuCategoryRepository
            .findByIdAndDeletedIsFalse(menuCategoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
    menuCategory.changeMenuCategoryName(request.name());
    return MenuCategoryResponseDto.fromEntity(menuCategory);
  }

  @Override
  @Transactional
  public void deleteMenuCategory(UUID menuCategoryId) {
    MenuCategory menuCategory =
        menuCategoryRepository
            .findByIdAndDeletedIsFalse(menuCategoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
    menuCategory.softDelete(null); // / 토큰 기능 추가 시 수정 필요
  }
}
