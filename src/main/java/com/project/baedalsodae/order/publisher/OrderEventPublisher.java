package com.project.baedalsodae.order.publisher;

import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.service.EventService;
import com.project.baedalsodae.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    private final EventService eventService;

    public void publishOrderCreated(Order order) {
        Event newEvent = Event.fromOrder(order, EventType.ORDER_CREATED);
        eventService.save(newEvent);
    }

    public void publishOrderRequested(Order order) {
        Event newEvent = Event.fromOrder(order, EventType.ORDER_UPDATED);
        eventService.save(newEvent);
    }

    public void publishOrderAccepted(Order order) {
        Event newEvent = Event.fromOrder(order, EventType.ORDER_UPDATED);
        eventService.save(newEvent);
    }

    public void publishOrderRejected(Order order) {
        Event newEvent = Event.fromOrder(order, EventType.ORDER_UPDATED);
        eventService.save(newEvent);
    }
}
