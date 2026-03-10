package com.project.baedalsodae.event.poller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.event.dto.OrderDeliveredEvent;
import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeliveredDispatcher implements EventDispatcher {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public EventType getSupportedEventType() {
        return EventType.ORDER_DELIVERED;
    }

    @Override
    public void dispatch(Event event) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(event.getPayload());
        UUID orderId = UUID.fromString(node.get("orderId").asText());
        UUID storeId = UUID.fromString(node.get("storeId").asText());

        eventPublisher.publishEvent(new OrderDeliveredEvent(orderId, storeId));
    }
}
