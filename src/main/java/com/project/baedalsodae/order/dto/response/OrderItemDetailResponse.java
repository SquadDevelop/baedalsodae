package com.project.baedalsodae.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemDetailResponse {

    private UUID orderItemId;
    private UUID menuId;

    private String menuName;
    private int unitPrice;
    private int quantity;

    private int lineAmount;
}