package com.project.baedalsodae.event.listener;

import com.project.baedalsodae.event.dto.OrderCancelRequestedEvent;
import com.project.baedalsodae.event.dto.OrderCreatedEvent;
import com.project.baedalsodae.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        paymentService.processPayment(event.orderId(), event.userId(), event.finalAmount());
    }

    @EventListener
    public void handleOrderCreated(OrderCancelRequestedEvent event) {
        paymentService.processPaymentCancel(event.orderId(), event.userId(), event.finalAmount());
    }
}
