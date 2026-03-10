package com.project.baedalsodae.event.poller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.event.dto.OrderCreatedEvent;
import com.project.baedalsodae.event.entity.AggregateType;
import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventStatus;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.repository.EventRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventPollerTest {

    private EventPoller eventPoller;

    @Mock private EventRepository eventRepository;

    @Spy private ObjectMapper objectMapper;

    @Mock private EventDispatcher orderCreatedDispatcher;
    @Mock private EventDispatcher paymentCreatedDispatcher;

    @BeforeEach
    void setUp() {
        lenient()
                .when(orderCreatedDispatcher.getSupportedEventType())
                .thenReturn(EventType.ORDER_CREATED);
        lenient()
                .when(paymentCreatedDispatcher.getSupportedEventType())
                .thenReturn(EventType.PAYMENT_CREATED);

        // 모든 테스트에서 공통으로 사용
        eventPoller =
                new EventPoller(
                        eventRepository, List.of(orderCreatedDispatcher, paymentCreatedDispatcher));
    }

    // ======================== poll - 이벤트 없음 ========================

    @Test
    @DisplayName("poll() - PENDING 이벤트가 없으면 아무 작업도 수행하지 않음")
    void poll_noPendingEvents_doesNothing() {
        // given
        given(eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING))
                .willReturn(List.of());

        // when
        eventPoller.poll();

        // then
        //        then(eventPublisher).shouldHaveNoInteractions();
        then(orderCreatedDispatcher).shouldHaveNoMoreInteractions();
        then(paymentCreatedDispatcher).shouldHaveNoMoreInteractions();
    }

    // ======================== poll - 정상 dispatch ========================

    @Test
    @DisplayName("poll() - ORDER_CREATED 이벤트 정상 dispatch 시 PUBLISHED 상태로 변경됨")
    void poll_successfulDispatch_marksPublished() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal finalAmount = BigDecimal.valueOf(18000);
        String payload =
                objectMapper.writeValueAsString(
                        new OrderCreatedEvent(orderId, userId, finalAmount));

        Event event = Event.create(AggregateType.ORDER, orderId, EventType.ORDER_CREATED, payload);

        given(eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING))
                .willReturn(List.of(event));

        // when
        eventPoller.poll();

        // then
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
        //        then(eventPublisher).should().publishEvent(any(OrderCreatedEvent.class));
        verify(orderCreatedDispatcher).dispatch(event);
    }

    // ======================== poll - dispatch 실패 ========================

    @Test
    @DisplayName("poll() - dispatch 중 예외 발생 시 retryCount가 1 증가하고 PENDING 유지")
    void poll_dispatchFails_incrementsRetryAndStaysPending() throws JsonProcessingException {
        // given
        // payload가 잘못된 JSON이면 dispatch 시 JsonProcessingException 발생
        Event event =
                Event.create(
                        AggregateType.ORDER,
                        UUID.randomUUID(),
                        EventType.ORDER_CREATED,
                        "invalid-json");

        given(eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING))
                .willReturn(List.of(event));

        doThrow(new JsonProcessingException("dispatch 실패") {})
                .when(orderCreatedDispatcher)
                .dispatch(event);

        // when
        eventPoller.poll();

        // then
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PENDING);
    }

    @Test
    @DisplayName("poll() - dispatch 3회 실패 시 FAILED 상태로 변경됨")
    void poll_dispatchFailsMaxRetryTimes_becomesFailed() throws JsonProcessingException {
        // given
        Event event =
                Event.create(
                        AggregateType.ORDER,
                        UUID.randomUUID(),
                        EventType.ORDER_CREATED,
                        "invalid-json");

        given(eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING))
                .willReturn(List.of(event));

        doThrow(new JsonProcessingException("dispatch 실패") {})
                .when(orderCreatedDispatcher)
                .dispatch(event);

        // when - 3회 폴링
        eventPoller.poll();
        eventPoller.poll();
        eventPoller.poll();

        // then
        assertThat(event.getRetryCount()).isEqualTo(3);
        assertThat(event.getStatus()).isEqualTo(EventStatus.FAILED);
    }

    // ======================== poll - 소진된 이벤트 skip ========================

    @Test
    @DisplayName("poll() - retryCount가 MAX_RETRY(3) 이상인 이벤트는 dispatch 시도 없이 skip됨")
    void poll_exhaustedEvent_isSkipped() throws JsonProcessingException {
        // given
        Event event =
                Event.create(
                        AggregateType.ORDER,
                        UUID.randomUUID(),
                        EventType.ORDER_CREATED,
                        "invalid-json");
        // 3번 실패시켜 retryCount=3 (FAILED 상태)
        event.markFailed(3);
        event.markFailed(3);
        event.markFailed(3);

        given(eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING))
                .willReturn(List.of(event));

        // when
        eventPoller.poll();

        // then - ApplicationEventPublisher는 호출되지 않아야 함
        then(orderCreatedDispatcher).should(never()).dispatch(any());
    }

    // ======================== poll - 알 수 없는 이벤트 타입 ========================

    @Test
    @DisplayName("poll() - Dispatcher가 없는 EventType이면 markFailed() 처리")
    void poll_unknownEventType_marksAsFailed() throws JsonProcessingException {
        Event event =
                Event.create(AggregateType.ORDER, UUID.randomUUID(), EventType.ORDER_UPDATED, "{}");

        given(eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING))
                .willReturn(List.of(event));

        eventPoller.poll();

        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PENDING);
        then(orderCreatedDispatcher).should(never()).dispatch(any());
        then(paymentCreatedDispatcher).should(never()).dispatch(any());
    }
}
