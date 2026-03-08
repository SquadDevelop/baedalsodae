package com.project.baedalsodae.menu.service;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPostRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.item.MenuItemResponseDto;
import java.util.List;
import java.util.UUID;

public interface MenuItemService {

    MenuItemResponseDto createMenuItem(
            UUID menuCategoryId, MenuItemPostRequestDto request, UserDetailsImpl userDetails);

    MenuItemResponseDto updateMenuItem(
            UUID menuItemId, MenuItemPutRequestDto request, UserDetailsImpl userDetails);

    MenuItemResponseDto patchMenuItem(
            UUID menuItemId, MenuItemPatchRequestDto request, UserDetailsImpl userDetails);

    void deleteMenuItem(UUID menuItemId, UserDetailsImpl userDetails);

    boolean isDuplicateMenuItemName(UUID storeId, String name);

    MenuItemResponseDto updateMenuItemOrder(
            UUID menuItemId, Integer order, UserDetailsImpl userDetails);

    List<MenuItemResponseDto> getMenuItem(UUID menuCategoryId, UserDetailsImpl userDetails);
}
