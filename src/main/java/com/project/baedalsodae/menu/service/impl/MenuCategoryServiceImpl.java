package com.project.baedalsodae.menu.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.common.OrderUtil;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPostRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.repository.MenuCategoryRepository;
import com.project.baedalsodae.menu.service.MenuCategoryService;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuCategoryServiceImpl implements MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public MenuCategoryResponseDto createMenuCategory(
            UUID storeId, MenuCategoryPostRequestDto request) {

        Store store =
                storeRepository
                        .findByIdAndIsDeletedIsFalse(storeId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        if (existsByStoreIdAndNameAndDeletedIsFalse(storeId, request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_MENU_CATEGORY_NAME);
        }
        int maxOrderNo =
                menuCategoryRepository
                        .findMaxOrderNoByStoreIdAndDeletedIsFalse((storeId))
                        .orElse(0);
        MenuCategory menuCategory = MenuCategory.create(store, request.name(), maxOrderNo + 1);
        try {
            menuCategoryRepository.save(menuCategory);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.MENU_CATEGORY_ORDER_CONFLICT);
        }
        return MenuCategoryResponseDto.fromEntity(menuCategory);
    }

    @Override
    @Transactional
    public MenuCategoryResponseDto updateMenuCategory(
            UUID menuCategoryId, MenuCategoryPutRequestDto request) {
        MenuCategory menuCategory =
                menuCategoryRepository
                        .findByIdAndDeletedIsFalse(menuCategoryId)
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
        if (!menuCategory.getName().equals(request.name())
                && existsByStoreIdAndNameAndDeletedIsFalse(
                        menuCategory.getStore().getId(), request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_MENU_CATEGORY_NAME);
        }
        menuCategory.changeMenuCategoryName(request.name());
        return MenuCategoryResponseDto.fromEntity(menuCategory);
    }

    @Override
    @Transactional
    public void deleteMenuCategory(UUID menuCategoryId) {
        MenuCategory menuCategory =
                menuCategoryRepository
                        .findByIdAndDeletedIsFalse(menuCategoryId)
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
        if (menuCategory.hasItem()) throw new BusinessException(ErrorCode.MENU_CATEGORY_HAS_ITEMS);

        UUID storeId = menuCategory.getStore().getId();
        List<MenuCategory> menuCategories =
                menuCategoryRepository.findAllByStoreIdAndDeletedIsFalseWithLock(storeId);

        OrderUtil.deleteAndShift(menuCategories, menuCategory);
        menuCategory.softDelete(null); // / 토큰 기능 추가 시 수정 필요
    }

    @Override
    @Transactional
    public MenuCategoryResponseDto updateMenuCategoryOrder(
            UUID menuCategoryId, MenuCategoryPatchRequestDto request) {
        MenuCategory menuCategory =
                menuCategoryRepository
                        .findByIdAndDeletedIsFalse(menuCategoryId)
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
        Integer from = menuCategory.getOrderNo();
        if (from == null) {
            throw new BusinessException(ErrorCode.INVALID_MENU_CATEGORY_ORDER);
        }
        int to = request.orderNo();

        if (from == to) {
            return MenuCategoryResponseDto.fromEntity(menuCategory);
        }

        UUID storeId = menuCategory.getStore().getId();

        List<MenuCategory> menuCategories =
                menuCategoryRepository.findAllByStoreIdAndDeletedIsFalseWithLock(storeId);

        OrderUtil.reorder(menuCategories, menuCategory, from, to);
        return MenuCategoryResponseDto.fromEntity(menuCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuCategoryResponseDto> getMenuCategories(UUID storeId) {
        List<MenuCategory> menuCategories =
                menuCategoryRepository.findAllByStoreIdAndDeletedIsFalse(storeId);
        return MenuCategoryResponseDto.fromEntityList(menuCategories);
    }

    @Override
    public boolean isDuplicateMenuCategoryName(UUID storeId, String name) {
        return existsByStoreIdAndNameAndDeletedIsFalse(storeId, name);
    }

    private boolean existsByStoreIdAndNameAndDeletedIsFalse(UUID storeId, String name) {
        return menuCategoryRepository.existsByStoreIdAndNameAndDeletedIsFalse(storeId, name);
    }
}
