package com.project.baedalsodae.menu.dto.requestDto;

import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import java.util.UUID;

public class MenuRequestDto {

  public record MenuRequest(
      String name,
      String description,
      int price,
      int orderNo,
      boolean isPopular,
      UUID categoryId,
      MenuStatus menuStatus) {}

  public record PatchMenuRequest(
      String name,
      String description,
      Integer price,
      Integer orderNo,
      Boolean isPopular,
      UUID categoryId,
      MenuStatus menuStatus) {}
}
