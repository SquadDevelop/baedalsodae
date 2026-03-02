package com.project.baedalsodae.menu.dto.requestDto;

import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import java.util.UUID;


  public record MenuPutRequestDto(
      String name,
      String description,
      int price,
      int orderNo,
      boolean isPopular,
      UUID categoryId,
      MenuStatus menuStatus) {}

