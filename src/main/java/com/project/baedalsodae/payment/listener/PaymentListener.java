package com.project.baedalsodae.payment.listener;

import com.project.baedalsodae.event.dto.OrderCreatedEvent;
import com.project.baedalsodae.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentService paymentService;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        paymentService.processPayment(event.orderId(), event.userId(), event.finalAmount());
    }
}
