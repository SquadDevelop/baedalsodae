package com.project.baedalsodae.menu.service.impl;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.common.OrderUtil;
import com.project.baedalsodae.menu.common.StoreOwnershipValidator;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPostRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.item.MenuItemResponseDto;
import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.repository.MenuCategoryRepository;
import com.project.baedalsodae.menu.repository.MenuItemRepository;
import com.project.baedalsodae.menu.service.MenuItemService;
import com.project.baedalsodae.recommendation.service.MenuEmbeddingService;
import com.project.baedalsodae.tag.service.TagMappingService;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final TagMappingService tagMappingService;
    private final MenuEmbeddingService menuEmbeddingService;

    @Transactional
    @Override
    public MenuItemResponseDto createMenuItem(
            UUID menuCategoryId, MenuItemPostRequestDto request, UserDetailsImpl userDetails) {
        MenuCategory category = getMenuCategoryByMenuCategoryIdWithLock(menuCategoryId);
        StoreOwnershipValidator.verifyStoreOwnership(
                category.getStore(), userDetails, ErrorCode.MENU_ITEM_FORBIDDEN);
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
        tagMappingService.createTagMappings(menuItemRepository.save(item), request.tagNames());

        menuEmbeddingService.syncMenuItem(item);
        return MenuItemResponseDto.fromEntity(item);
    }

    @Transactional
    @Override
    public MenuItemResponseDto updateMenuItem(
            UUID menuItemId, MenuItemPutRequestDto request, UserDetailsImpl userDetails) {
        MenuItem item =
                menuItemRepository
                        .findByIdAndDeletedIsFalse(menuItemId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
        StoreOwnershipValidator.verifyStoreOwnership(
                item.getMenuCategory().getStore(), userDetails, ErrorCode.MENU_ITEM_FORBIDDEN);
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
        menuEmbeddingService.syncMenuItem(item);
        return MenuItemResponseDto.fromEntity(item);
    }

    @Transactional
    @Override
    public MenuItemResponseDto patchMenuItem(
            UUID menuItemId, MenuItemPatchRequestDto request, UserDetailsImpl userDetails) {
        MenuItem item =
                menuItemRepository
                        .findByIdAndDeletedIsFalse(menuItemId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
        StoreOwnershipValidator.verifyStoreOwnership(
                item.getMenuCategory().getStore(), userDetails, ErrorCode.MENU_ITEM_FORBIDDEN);
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
        menuEmbeddingService.syncMenuItem(item);
        return MenuItemResponseDto.fromEntity(item);
    }

    @Transactional
    @Override
    public void deleteMenuItem(UUID menuItemId, UserDetailsImpl userDetails) {
        MenuItem item =
                menuItemRepository
                        .findByIdAndDeletedIsFalse(menuItemId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
        StoreOwnershipValidator.verifyStoreOwnership(
                item.getMenuCategory().getStore(), userDetails, ErrorCode.MENU_ITEM_FORBIDDEN);
        UUID menuCategoryId = item.getMenuCategory().getId();

        List<MenuItem> menuItems =
                menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalseWithLock(
                        menuCategoryId);
        OrderUtil.deleteAndShift(menuItems, item);
        item.softDelete(userDetails.getUserId());
    }

    @Override
    public boolean isDuplicateMenuItemName(UUID storeId, String name) {
        return existsByStoreIdAndNameAndDeletedIsFalse(storeId, name);
    }

    @Transactional
    @Override
    public MenuItemResponseDto updateMenuItemOrder(
            UUID menuItemId, Integer order, UserDetailsImpl userDetails) {
        MenuItem item =
                menuItemRepository
                        .findByIdAndDeletedIsFalse(menuItemId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
        StoreOwnershipValidator.verifyStoreOwnership(
                item.getMenuCategory().getStore(), userDetails, ErrorCode.MENU_ITEM_FORBIDDEN);
        int from = item.getValidOrderNo();
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
    public List<MenuItemResponseDto> getMenuItem(UUID menuCategoryId, UserDetailsImpl userDetails) {
        List<MenuItem> allItems =
                menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalse(menuCategoryId);

        List<MenuItem> items = new ArrayList<>();
        List<UUID> itemIds = new ArrayList<>();
        for (MenuItem item : allItems) {
            if (isVisibleTo(item, userDetails)) {
                items.add(item);
                itemIds.add(item.getId());
            }
        }
        if (items.isEmpty()) return List.of();
        Map<UUID, List<String>> tagMap = tagMappingService.getTagNamesByMenuItemIds(itemIds);
        List<MenuItemResponseDto> responseDto = new ArrayList<>();
        for (MenuItem item : items) {
            List<String> tagNames = tagMap.getOrDefault(item.getId(), List.of());
            responseDto.add(MenuItemResponseDto.fromEntity(item, tagNames));
        }
        return responseDto;
    }

    private boolean isVisibleTo(MenuItem item, UserDetailsImpl userDetails) {
        if (item.getMenuStatus().isPubliclyVisible()) return true;
        return userDetails != null && userDetails.getUserRole() != UserRole.CUSTOMER;
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
