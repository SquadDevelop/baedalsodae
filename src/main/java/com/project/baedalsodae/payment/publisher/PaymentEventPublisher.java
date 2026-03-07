package com.project.baedalsodae.payment.publisher;

import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.service.EventService;
import com.project.baedalsodae.payment.dto.event.PaymentResultEvent;
import com.project.baedalsodae.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {
    private final ApplicationEventPublisher eventPublisher;
    private final EventService eventService;

    public void publishPaymentResult(Payment payment) {
        final PaymentResultEvent event = PaymentResultEvent.from(payment);
        eventPublisher.publishEvent(event);
        Event newEvent = Event.fromPayment(payment, EventType.PAYMENT_CREATED);
        eventService.save(newEvent);
    }
}
