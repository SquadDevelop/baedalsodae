package com.project.baedalsodae.payment.publisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.project.baedalsodae.event.dto.PaymentCreatedEvent;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.publisher.EventPublisher;
import com.project.baedalsodae.event.service.EventService;
import com.project.baedalsodae.payment.entity.Payment;
import com.project.baedalsodae.payment.entity.PaymentStatus;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PaymentEventPublisherTest {

    @InjectMocks private EventPublisher paymentEventPublisher;

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private EventService eventService;

    @Mock private Payment payment;

    @Test
    @DisplayName(
            "성공 - publishPaymentResult 호출 시 ApplicationEventPublisher로 PaymentResultEvent가 발행됨")
    void publishPaymentResult_publishesPaymentResultEvent() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        given(payment.getId()).willReturn(paymentId);
        given(payment.getOrderId()).willReturn(orderId);
        given(payment.getStatus()).willReturn(PaymentStatus.SUCCESS);
        given(payment.getPgTransactionId()).willReturn("pg-txn-001");

        // when
        paymentEventPublisher.publishPaymentEvent(payment, EventType.PAYMENT_CREATED);

        // then
        then(eventPublisher).should().publishEvent(any(PaymentCreatedEvent.class));
    }

    @Test
    @DisplayName("성공 - publishPaymentResult 호출 시 Outbox용 Event가 EventService를 통해 저장됨")
    void publishPaymentResult_savesOutboxEvent() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        given(payment.getId()).willReturn(paymentId);
        given(payment.getOrderId()).willReturn(orderId);
        given(payment.getStatus()).willReturn(PaymentStatus.SUCCESS);
        given(payment.getPgTransactionId()).willReturn("pg-txn-001");

        // when
        paymentEventPublisher.publishPaymentEvent(payment, EventType.PAYMENT_CREATED);

        // then
        then(eventService).should().save(any());
    }

    @Test
    @DisplayName(
            "성공 - publishPaymentResult 호출 시 ApplicationEventPublisher와 EventService 모두 정확히 1번씩 호출됨")
    void publishPaymentResult_callsBothPublisherAndService_exactlyOnce() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        given(payment.getId()).willReturn(paymentId);
        given(payment.getOrderId()).willReturn(orderId);
        given(payment.getStatus()).willReturn(PaymentStatus.PENDING);
        given(payment.getPgTransactionId()).willReturn(null);

        // when
        paymentEventPublisher.publishPaymentEvent(payment, EventType.PAYMENT_CREATED);

        // then
        then(eventPublisher).should().publishEvent(any(Object.class));
        then(eventService).should().save(any());
    }
}
