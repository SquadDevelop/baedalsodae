package com.project.baedalsodae.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.event.entity.AggregateType;
import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.repository.EventRepository;
import com.project.baedalsodae.event.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventHandler {
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public void handleOrderCreated(OrderCreatedEvent orderCreatedEvent) {
        Event orderEvent =
                Event.create(
                        AggregateType.ORDER,
                        orderCreatedEvent.orderId(),
                        EventType.ORDER_CREATED,
                        objectMapper.valueToTree(orderCreatedEvent).toString());
        eventRepository.save(orderEvent);
    }
}
