package com.project.baedalsodae.menu.service;

import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPostRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.item.MenuItemResponseDto;
import java.util.List;
import java.util.UUID;

public interface MenuItemService {

    MenuItemResponseDto createMenuItem(UUID menuCategoryId, MenuItemPostRequestDto request);

    MenuItemResponseDto updateMenuItem(UUID menuItemId, MenuItemPutRequestDto request);

    MenuItemResponseDto patchMenuItem(UUID menuItemId, MenuItemPatchRequestDto request);

    void deleteMenuItem(UUID menuItemId);

    boolean isDuplicateMenuItemName(UUID menuCategoryId, String name);

    MenuItemResponseDto updateMenuItemOrder(UUID menuItemId, Integer order);

    List<MenuItemResponseDto> getMenuItem(UUID menuCategoryId);
}
