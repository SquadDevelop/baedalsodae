package com.project.baedalsodae.menu.common;

import java.util.List;

public class OrderUtil {
  public static <T extends Orderable> void reorder(
      List<T> items, int sourceOrderNo, int targetOrderNo) {
    boolean isMovingDown = sourceOrderNo < targetOrderNo;

    if (isMovingDown) {
      items.stream()
          .filter(item -> item.getOrderNo() > sourceOrderNo && item.getOrderNo() <= targetOrderNo)
          .forEach(item -> item.changeOrderNo(item.getOrderNo() - 1));
    } else {
      items.stream()
          .filter(item -> item.getOrderNo() >= targetOrderNo && item.getOrderNo() < sourceOrderNo)
          .forEach(item -> item.changeOrderNo(item.getOrderNo() + 1));
    }
  }
}
