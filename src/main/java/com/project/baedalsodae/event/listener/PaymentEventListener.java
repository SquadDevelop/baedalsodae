package com.project.baedalsodae.event.listener;

import com.project.baedalsodae.event.dto.OrderCancelRequestedEvent;
import com.project.baedalsodae.event.dto.OrderCreatedEvent;
import com.project.baedalsodae.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info(">>>>> handleOrderCreated start");
        paymentService.processPayment(event.orderId(), event.userId(), event.finalAmount());
    }

    @EventListener
    public void handleOrderCreated(OrderCancelRequestedEvent event) {
        log.info(">>>>> handleOrderCreated start");
        paymentService.processPaymentCancel(event.orderId(), event.userId(), event.finalAmount());
    }
}
