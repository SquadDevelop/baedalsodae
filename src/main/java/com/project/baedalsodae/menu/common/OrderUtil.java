package com.project.baedalsodae.menu.common;

import java.util.List;

public class OrderUtil {
    private static <T extends Orderable> void shiftBetween(
            List<T> items, int sourceOrderNo, int targetOrderNo) {
        boolean isMovingDown = sourceOrderNo < targetOrderNo;

        if (isMovingDown) {
            items.stream()
                    .filter(
                            item ->
                                    item.getOrderNo() > sourceOrderNo
                                            && item.getOrderNo() <= targetOrderNo)
                    .forEach(item -> item.changeOrderNo(item.getOrderNo() - 1));
        } else {
            items.stream()
                    .filter(
                            item ->
                                    item.getOrderNo() >= targetOrderNo
                                            && item.getOrderNo() < sourceOrderNo)
                    .forEach(item -> item.changeOrderNo(item.getOrderNo() + 1));
        }
    }

    public static <T extends Orderable> void reorder(List<T> items, T target, int from, int to) {
        shiftBetween(items, from, to);
        target.changeOrderNo(to);
    }

    public static <T extends Orderable> void deleteAndShift(List<T> items, T target) {
        int deletedOrderNo = target.getOrderNo();
        target.changeOrderNo(null);
        items.stream()
                .filter(item -> item.getOrderNo() != null && item.getOrderNo() > deletedOrderNo)
                .forEach(item -> item.changeOrderNo(item.getOrderNo() - 1));
    }
}
