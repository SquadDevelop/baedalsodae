package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemDetailResponse {

    private UUID orderItemId;
    private UUID menuItemId;

    private String menuItemName;
    private int menuItemPrice;
    private int quantity;

    private int lineAmount;

    public static OrderItemDetailResponse from(OrderItem orderItem) {
        int unitPrice = orderItem.getPriceSnapshot();
        int quantity = orderItem.getQuantity();

        return OrderItemDetailResponse.builder()
                .orderItemId(orderItem.getId())
                .menuItemId(orderItem.getMenuItemId())
                .menuItemName(orderItem.getNameSnapshot())
                .menuItemPrice(unitPrice)
                .quantity(quantity)
                .lineAmount(unitPrice * quantity)
                .build();
    }
}