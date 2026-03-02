package com.project.baedalsodae.menu.service;

import com.project.baedalsodae.menu.dto.requestDto.MenuPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.MenuPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.MenuItemResponseDto;
import java.util.UUID;

public interface MenuItemService {

  MenuItemResponseDto updateMenuItem(UUID menuItemId, MenuPutRequestDto request);

  MenuItemResponseDto patchMenuItem(UUID menuItemId, MenuPatchRequestDto request);

  void deleteMenuItem(UUID menuItemId);
}
