package com.project.baedalsodae.cart.dto.response;

import com.project.baedalsodae.cart.entity.CartItem;
import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID cartItemId,
        UUID menuItemId,
        String menuName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineAmount) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getMenuItem().getId(),
                item.getMenuItem().getName(),
                item.getMenuItem().getPrice(),
                item.getQuantity(),
                item.getLineAmount());
    }
}
