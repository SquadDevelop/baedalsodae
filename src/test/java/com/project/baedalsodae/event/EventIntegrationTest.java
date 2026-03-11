package com.project.baedalsodae.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventStatus;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.poller.EventPoller;
import com.project.baedalsodae.event.publisher.EventPublisher;
import com.project.baedalsodae.event.repository.EventRepository;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.payment.service.PaymentService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class EventIntegrationTest {

    @Autowired private EventPublisher eventPublisher;

    @Autowired private EventPoller eventPoller;

    @Autowired private EventRepository eventRepository;

    @MockitoBean private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("EventPublisher가 발생시킨 이벤트가 정상적으로 Poller에 의해 처리되고 Listener에서 실행되는지 통합 검증")
    void publishAndListenEvent() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal finalAmount = BigDecimal.valueOf(25000);

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(orderId);
        when(order.getUserId()).thenReturn(userId);
        when(order.getFinalAmount()).thenReturn(finalAmount);

        // when 1. 이벤트 발행 (Status가 PENDING으로 DB에 저장됨)
        eventPublisher.publishOrderEvent(order, EventType.ORDER_CREATED);

        List<Event> pendingEvents = eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING);
        assertThat(pendingEvents).hasSize(1);
        
        Event savedEvent = pendingEvents.get(0);
        assertThat(savedEvent.getEventType()).isEqualTo(EventType.ORDER_CREATED);

        // when 2. EventPoller가 PENDING 상태의 이벤트를 폴링 후 dispatch
        eventPoller.poll();

        // then 1. 이벤트 상태가 PUBLISHED로 변경되어야 함
        Event publishedEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(publishedEvent.getStatus()).isEqualTo(EventStatus.PUBLISHED);

        // then 2. PaymentEventListener가 PaymentService를 적절히 호출했는지 검증
        verify(paymentService, timeout(2000).times(1))
                .processPayment(eq(orderId), eq(userId), eq(finalAmount));
    }
}
