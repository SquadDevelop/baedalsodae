package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class OrderActionStatusResponse {

    private UUID orderId;

    private OrderStatus status;

    public static OrderActionStatusResponse from(Order order) {
        return OrderActionStatusResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .build();
    }
}