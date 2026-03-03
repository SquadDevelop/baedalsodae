package com.project.baedalsodae.menu.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.common.OrderUtil;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPostRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.item.MenuItemResponseDto;
import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.repository.MenuCategoryRepository;
import com.project.baedalsodae.menu.repository.MenuItemRepository;
import com.project.baedalsodae.menu.service.MenuItemService;
import com.project.baedalsodae.tag.service.TagMappingService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

  private final MenuItemRepository menuItemRepository;
  private final MenuCategoryRepository menuCategoryRepository;
  private final TagMappingService tagMappingService;

  @Transactional
  @Override
  public MenuItemResponseDto createMenuItem(UUID menuCategoryId, MenuItemPostRequestDto request) {
    if (existsByNameAndMenuCategoryIdAndDeletedIsFalse(menuCategoryId, request.name())) {
      throw new BusinessException(ErrorCode.DUPLICATE_MENU_ITEM_NAME);
    }
    MenuCategory category =
        menuCategoryRepository
            .findByIdAndDeletedIsFalse(menuCategoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
    int maxOrderNo = menuItemRepository.findMaxOrderNoByMenuCategoryId((menuCategoryId)).orElse(0);
    MenuItem item =
        MenuItem.createMenuItem(
            request.name(),
            request.description(),
            request.price(),
            request.isPopular(),
            maxOrderNo + 1,
            request.menuStatus(),
            category);
    tagMappingService.createTagMappings(menuItemRepository.save(item), request.tagNames());
    return MenuItemResponseDto.fromEntity(item);
  }

  @Transactional
  @Override
  public MenuItemResponseDto updateMenuItem(UUID menuItemId, MenuItemPutRequestDto request) {
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
        category,
        request.isPopular());
    tagMappingService.deleteAllTagMappingByMenuItemId(menuItemId);
    tagMappingService.createTagMappings(item, request.tagNames());
    return MenuItemResponseDto.fromEntity(item);
  }

  @Transactional
  @Override
  public MenuItemResponseDto patchMenuItem(UUID menuItemId, MenuItemPatchRequestDto request) {
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
    if (request.isPopular() != null) item.changeIsPopular(request.isPopular());
    if (request.menuStatus() != null) item.changeMenuStatus(request.menuStatus());
    if (request.tagNames() != null) {
      tagMappingService.deleteAllTagMappingByMenuItemId(menuItemId);
      tagMappingService.createTagMappings(item, request.tagNames());
    }
    return MenuItemResponseDto.fromEntity(item);
  }

  @Transactional
  @Override
  public void deleteMenuItem(UUID menuItemId) {
    MenuItem item =
        menuItemRepository
            .findByIdAndDeletedIsFalse(menuItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
    item.softDelete(null); // 토큰 기능 추가 시 수정 필요
  }


  @Transactional
  @Override
  public MenuItemResponseDto updateMenuItemOrder(UUID menuItemId, Integer order) {
    MenuItem item =
        menuItemRepository
            .findByIdAndDeletedIsFalse(menuItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
    Integer from = item.getOrderNo();
    if (from == null) {
      throw new BusinessException(ErrorCode.INVALID_MENU_ITEM_ORDER);
    }
    int to = order;

    if (from == to) {
      return MenuItemResponseDto.fromEntity(item);
    }

    UUID menuCategoryId = item.getMenuCategory().getId();

    List<MenuItem> menuItems =
        menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalseForUpdate(menuCategoryId);

    OrderUtil.reorder(menuItems, item, from, to);
    return MenuItemResponseDto.fromEntity(item);
  }

  private boolean existsByNameAndMenuCategoryIdAndDeletedIsFalse(UUID menuCategoryId, String name) {
    return menuItemRepository.existsByMenuCategoryIdAndNameAndIsDeletedIsFalse(
        menuCategoryId, name);
  }
}
