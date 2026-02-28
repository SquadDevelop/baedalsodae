package com.project.baedalsodae.menu.service;

import com.project.baedalsodae.menu.dto.requestDto.MenuRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.MenuResponseDto;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public interface MenuItemService {

  MenuResponseDto.MenuItemResponse updateMenuItem(
      UUID menuItemId, MenuRequestDto.MenuRequest request);

  MenuResponseDto.MenuItemResponse patchMenuItem(
      UUID menuItemId, MenuRequestDto.PatchMenuRequest request);

  void deleteMenuItem(UUID menuItemId);
}
