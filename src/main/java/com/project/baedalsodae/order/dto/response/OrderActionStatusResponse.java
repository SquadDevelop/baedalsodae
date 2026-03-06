package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.payment.entity.Payment;
import com.project.baedalsodae.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class OrderActionStatusResponse {

    private UUID orderId;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    public static OrderActionStatusResponse from(Order order, Payment payment) {
        return OrderActionStatusResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus())
                .paymentStatus(payment.getStatus())
                .build();
    }
}