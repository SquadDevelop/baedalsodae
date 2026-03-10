package com.project.baedalsodae.event.publisher;

import com.project.baedalsodae.event.dto.OrderCancelRequestedEvent;
import com.project.baedalsodae.event.dto.OrderCreatedEvent;
import com.project.baedalsodae.event.dto.OrderDeliveredEvent;
import com.project.baedalsodae.event.dto.PaymentCreatedEvent;
import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.service.EventService;
import com.project.baedalsodae.global.common.util.JsonUtils;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {
    private final ApplicationEventPublisher eventPublisher;
    private final EventService eventService;

    public void publishOrderEvent(final Order order, final EventType type) {
        if (type == EventType.ORDER_CREATED) {
            final OrderCreatedEvent event = OrderCreatedEvent.from(order);
            eventService.save(Event.fromOrder(order, type, JsonUtils.toJson(event)));
        }else if (type == EventType.ORDER_UPDATED) {
            final OrderCancelRequestedEvent event = OrderCancelRequestedEvent.from(order);
            eventService.save(Event.fromOrder(order, type, JsonUtils.toJson(event)));
        } else if (type == EventType.ORDER_DELIVERED) {
            final OrderDeliveredEvent event = OrderDeliveredEvent.from(order);
            eventService.save(Event.fromOrder(order, type, JsonUtils.toJson(event)));
        }
    }

    public void publishPaymentEvent(final Payment payment, final EventType type) {
        if (type == EventType.PAYMENT_CREATED) {
            final PaymentCreatedEvent event = PaymentCreatedEvent.from(payment);
            eventPublisher.publishEvent(event);
            eventService.save(Event.fromPayment(payment, EventType.PAYMENT_CREATED));
        }
        // 다른 이벤트 타입이 추가될 경우 여기에 분기 처리 (예: PAYMENT_FAILED, PAYMENT_REFUNDED 등
    }
}
