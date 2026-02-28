package com.project.baedalsodae.menu.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.dto.requestDto.MenuRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.MenuResponseDto;
import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.repository.MenuCategoryRepository;
import com.project.baedalsodae.menu.repository.MenuItemRepository;
import com.project.baedalsodae.menu.service.MenuItemService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

  private final MenuItemRepository menuItemRepository;
  private final MenuCategoryRepository menuCategoryRepository;

  @Override
  public MenuResponseDto.MenuItemResponse updateMenuItem(
      UUID menuItemId, MenuRequestDto.MenuRequest request) {
    MenuItem item =
        menuItemRepository
            .findByIdAndDeletedIsFalse(menuItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
    MenuCategory category =
        menuCategoryRepository
            .findByIdAndDeletedIsFalse(request.categoryId())
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
    item.changeMenuInfo(
        request.name(),
        request.description(),
        request.price(),
        request.menuStatus(),
        request.orderNo(),
        category,
        request.isPopular());
    item.changeMenuStatus(request.menuStatus());
    return MenuResponseDto.MenuItemResponse.fromEntity(item);
  }

  @Override
  public MenuResponseDto.MenuItemResponse patchMenuItem(
      UUID menuItemId, MenuRequestDto.PatchMenuRequest request) {
    MenuItem item =
        menuItemRepository
            .findByIdAndDeletedIsFalse(menuItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
    if (request.categoryId() != null) {
      MenuCategory category =
          menuCategoryRepository
              .findByIdAndDeletedIsFalse(request.categoryId())
              .orElseThrow(() -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
      item.changeMenuCategory(category);
    }
    if (request.name() != null) item.changeName(request.name());
    if (request.description() != null) item.changeDescription(request.description());
    if (request.price() != null) item.changePrice(request.price());
    if (request.orderNo() != null) item.changeOrderNo(request.orderNo());
    if (request.isPopular() != null) item.changeIsPopular(request.isPopular());
    if (request.menuStatus() != null) item.changeMenuStatus(request.menuStatus());
    return MenuResponseDto.MenuItemResponse.fromEntity(item);
  }
}
