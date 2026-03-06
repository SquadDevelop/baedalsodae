package com.project.baedalsodae.event.poller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.event.entity.AggregateType;
import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventStatus;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.repository.EventRepository;
import com.project.baedalsodae.order.dto.event.OrderCreatedEvent;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class EventPollerTest {

    @InjectMocks private EventPoller eventPoller;

    @Mock private EventRepository eventRepository;

    @Mock private ApplicationEventPublisher eventPublisher;

    @Spy private ObjectMapper objectMapper;

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
        then(eventPublisher).shouldHaveNoInteractions();
    }

    // ======================== poll - 정상 dispatch ========================

    @Test
    @DisplayName("poll() - ORDER_CREATED 이벤트 정상 dispatch 시 PUBLISHED 상태로 변경됨")
    void poll_successfulDispatch_marksPublished() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        int finalAmount = 18000;

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
        then(eventPublisher).should().publishEvent(any(OrderCreatedEvent.class));
    }

    // ======================== poll - dispatch 실패 ========================

    @Test
    @DisplayName("poll() - dispatch 중 예외 발생 시 retryCount가 1 증가하고 PENDING 유지")
    void poll_dispatchFails_incrementsRetryAndStaysPending() {
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

        // when
        eventPoller.poll();

        // then
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PENDING);
    }

    @Test
    @DisplayName("poll() - dispatch 3회 실패 시 FAILED 상태로 변경됨")
    void poll_dispatchFailsMaxRetryTimes_becomesFailed() {
        // given
        Event event =
                Event.create(
                        AggregateType.ORDER,
                        UUID.randomUUID(),
                        EventType.ORDER_CREATED,
                        "invalid-json");

        given(eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING))
                .willReturn(List.of(event));

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
    void poll_exhaustedEvent_isSkipped() {
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
        then(eventPublisher).should(never()).publishEvent(any());
    }

    // ======================== poll - 알 수 없는 이벤트 타입 ========================

    @Test
    @DisplayName("poll() - 처리되지 않은 EventType이면 dispatch 없이 무시됨 (PAYMENT_CREATED)")
    void poll_unknownEventType_isIgnored() throws Exception {
        // given - EventPoller는 현재 ORDER_CREATED만 처리하므로 PAYMENT_CREATED는 dispatch 안 됨
        Event event =
                Event.create(
                        AggregateType.PAYMENT, UUID.randomUUID(), EventType.PAYMENT_CREATED, "{}");

        given(eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING))
                .willReturn(List.of(event));

        // when
        eventPoller.poll();

        // then
        then(eventPublisher).should(never()).publishEvent(any());
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }
}
