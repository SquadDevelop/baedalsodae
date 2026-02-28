package com.project.baedalsodae.menu.service;

import com.project.baedalsodae.menu.dto.requestDto.MenuRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.MenuResponseDto;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public interface MenuItemService {

  @Transactional
  MenuResponseDto.MenuItemResponse updateMenuItem(
      UUID menuItemId, MenuRequestDto.MenuRequest request);

  @Transactional
  MenuResponseDto.MenuItemResponse patchMenuItem(
      UUID menuItemId, MenuRequestDto.PatchMenuRequest request);
}
