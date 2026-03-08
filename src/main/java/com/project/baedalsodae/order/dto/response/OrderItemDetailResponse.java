package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.order.entity.OrderItem;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemDetailResponse {

    private UUID orderItemId;
    private UUID menuItemId;

    private String menuItemName;
    private BigDecimal menuItemPrice;
    private int quantity;

    private BigDecimal lineAmount;

    public static OrderItemDetailResponse from(OrderItem orderItem) {
        BigDecimal unitPrice = orderItem.getPriceSnapshot();
        int quantity = orderItem.getQuantity();

        return OrderItemDetailResponse.builder()
                .orderItemId(orderItem.getId())
                .menuItemId(orderItem.getMenuItemId())
                .menuItemName(orderItem.getNameSnapshot())
                .menuItemPrice(unitPrice)
                .quantity(quantity)
                .lineAmount(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
    }
}
