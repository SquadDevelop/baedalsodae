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
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
        MenuCategory category = getMenuCategoryByMenuCategoryId(menuCategoryId);
        UUID storeId = category.getStore().getId();
        if (existsByStoreIdAndNameAndDeletedIsFalse(storeId, request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_MENU_ITEM_NAME);
        }
        int maxOrderNo =
                menuItemRepository.findMaxOrderNoByMenuCategoryId((menuCategoryId)).orElse(0);
        MenuItem item =
                MenuItem.createMenuItem(
                        request.name(),
                        request.description(),
                        request.price(),
                        request.isPopular(),
                        maxOrderNo + 1,
                        request.menuStatus(),
                        category);
        try {
            tagMappingService.createTagMappings(menuItemRepository.save(item), request.tagNames());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.MENU_ITEM_ORDER_CONFLICT);
        }
        return MenuItemResponseDto.fromEntity(item);
    }

    @Transactional
    @Override
    public MenuItemResponseDto updateMenuItem(UUID menuItemId, MenuItemPutRequestDto request) {
        MenuItem item =
                menuItemRepository
                        .findByIdAndDeletedIsFalse(menuItemId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
        MenuCategory category = getMenuCategoryByMenuCategoryId(request.categoryId());

        UUID storeId = category.getStore().getId();
        if (!Objects.equals(item.getName(), request.name())
                && existsByStoreIdAndNameAndDeletedIsFalse(storeId, request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_MENU_ITEM_NAME);
        }
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
        UUID currentCategoryId = item.getMenuCategory().getId();
        MenuCategory category = getMenuCategoryByMenuCategoryId(currentCategoryId);
        UUID storeId = category.getStore().getId();
        if (request.name() != null) {
            if (!Objects.equals(item.getName(), request.name())
                    && existsByStoreIdAndNameAndDeletedIsFalse(storeId, request.name())) {
                throw new BusinessException(ErrorCode.DUPLICATE_MENU_ITEM_NAME);
            }
            item.changeName(request.name());
        }
        if (request.categoryId() != null && !currentCategoryId.equals(request.categoryId())) {
            MenuCategory newCategory = getMenuCategoryByMenuCategoryId(request.categoryId());
            item.changeMenuCategory(newCategory);
        }
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
        UUID menuCategoryId = item.getMenuCategory().getId();

        List<MenuItem> menuItems =
                menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalseWithLock(
                        menuCategoryId);
        OrderUtil.deleteAndShift(menuItems, item);
        item.softDelete(null); // 토큰 기능 추가 시 수정 필요
    }

    @Override
    public boolean isDuplicateMenuItemName(UUID storeId, String name) {
        return existsByStoreIdAndNameAndDeletedIsFalse(storeId, name);
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
                menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalseWithLock(
                        menuCategoryId);

        OrderUtil.reorder(menuItems, item, from, to);
        return MenuItemResponseDto.fromEntity(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDto> getMenuItem(UUID menuCategoryId) {
        List<MenuItem> menuItems =
                menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalse(menuCategoryId);

        return menuItems.stream().map(MenuItemResponseDto::fromEntity).toList();
    }

    private boolean existsByStoreIdAndNameAndDeletedIsFalse(UUID storeId, String name) {
        return menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(storeId, name);
    }

    private MenuCategory getMenuCategoryByMenuCategoryId(UUID menuCategoryId) {
        return menuCategoryRepository
                .findByIdAndDeletedIsFalse(menuCategoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
    }
}
