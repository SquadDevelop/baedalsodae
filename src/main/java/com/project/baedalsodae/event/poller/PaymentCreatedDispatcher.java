package com.project.baedalsodae.event.poller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.event.dto.PaymentCreatedEvent;
import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.payment.entity.PaymentStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCreatedDispatcher implements EventDispatcher {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public EventType getSupportedEventType() {
        return EventType.PAYMENT_CREATED;
    }

    @Override
    public void dispatch(Event event) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(event.getPayload());
        UUID orderId = UUID.fromString(node.get("orderId").asText());
        UUID paymentId = UUID.fromString(node.get("paymentId").asText());
        PaymentStatus status = PaymentStatus.valueOf(node.get("status").asText());
        String pgTransactionId =
                node.has("pgTransactionId") && !node.get("pgTransactionId").isNull()
                        ? node.get("pgTransactionId").asText()
                        : null;

        eventPublisher.publishEvent(
                new PaymentCreatedEvent(orderId, paymentId, status, pgTransactionId));
    }
}
