package com.project.baedalsodae.order.publisher;

import com.project.baedalsodae.order.dto.event.OrderCreatedEvent;
import com.project.baedalsodae.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishOrderCreated(Order order) {
        final OrderCreatedEvent event = OrderCreatedEvent.from(order);
        eventPublisher.publishEvent(event);
    }
}
