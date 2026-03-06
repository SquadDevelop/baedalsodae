package com.project.baedalsodae.event.handler;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.event.entity.AggregateType;
import com.project.baedalsodae.event.entity.EventStatus;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.repository.EventRepository;
import com.project.baedalsodae.order.dto.event.OrderCreatedEvent;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {

    @InjectMocks
    private OrderEventHandler orderEventHandler;

    @Mock
    private EventRepository eventRepository;

    @Spy
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("성공 - OrderCreatedEvent를 받으면 ORDER_CREATED 타입의 Event가 저장됨")
    void handleOrderCreated_savesOrderCreatedEvent() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        int finalAmount = 18000;
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, finalAmount);

        // when
        orderEventHandler.handleOrderCreated(event);

        // then
        then(eventRepository).should().save(
                argThat(savedEvent ->
                        savedEvent.getAggregateType() == AggregateType.ORDER
                                && savedEvent.getAggregateId().equals(orderId)
                                && savedEvent.getEventType() == EventType.ORDER_CREATED
                                && savedEvent.getStatus() == EventStatus.PENDING));
    }

    @Test
    @DisplayName("성공 - 저장되는 이벤트의 payload에 orderId, userId, finalAmount가 포함됨")
    void handleOrderCreated_payloadContainsEventFields() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        int finalAmount = 25000;
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, finalAmount);

        // when
        orderEventHandler.handleOrderCreated(event);

        // then
        then(eventRepository).should().save(
                argThat(savedEvent ->
                        savedEvent.getPayload() != null
                                && savedEvent.getPayload().contains(orderId.toString())
                                && savedEvent.getPayload().contains(userId.toString())
                                && savedEvent.getPayload().contains(String.valueOf(finalAmount))));
    }
}
