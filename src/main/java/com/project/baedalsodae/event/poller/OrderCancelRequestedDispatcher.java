package com.project.baedalsodae.event.poller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.event.dto.OrderCancelRequestedEvent;
import com.project.baedalsodae.event.dto.OrderDeliveredEvent;
import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderCancelRequestedDispatcher implements EventDispatcher {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public EventType getSupportedEventType() {
        return EventType.ORDER_CANCEL_REQUESTED;
    }

    @Override
    public void dispatch(Event event) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(event.getPayload());
        UUID orderId = UUID.fromString(node.get("orderId").asText());
        UUID userId = UUID.fromString(node.get("userId").asText());
        BigDecimal finalAmount = node.get("finalAmount").decimalValue();

        eventPublisher.publishEvent(new OrderCancelRequestedEvent(orderId, userId, finalAmount));
    }
}
