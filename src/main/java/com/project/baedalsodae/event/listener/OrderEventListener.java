package com.project.baedalsodae.event.listener;

import com.project.baedalsodae.event.dto.PaymentCanceledEvent;
import com.project.baedalsodae.event.dto.PaymentCreatedEvent;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final OrderService orderService;

    @EventListener
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        if (event.status() != PaymentStatus.SUCCESS) {
            return;
        }

        orderService.requestOrder(event.userId(), event.orderId());
    }

    @EventListener
    public void handlePaymentCanceled(PaymentCanceledEvent event) {
        if (event.status() != PaymentStatus.CANCELED) {
            return;
        }
        orderService.completeCancelOrder(event.userId(), event.orderId());
    }
}
