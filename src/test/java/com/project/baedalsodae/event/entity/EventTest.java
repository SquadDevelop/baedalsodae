package com.project.baedalsodae.event.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventTest {

    private static final int MAX_RETRY = 3;

    // ======================== create / fromPayment ========================

    @Test
    @DisplayName("Event.create() - 초기 상태는 PENDING, retryCount=0")
    void create_initialStatus() {
        // given & when
        Event event = Event.create(AggregateType.ORDER, UUID.randomUUID(), EventType.ORDER_CREATED, "{}");

        // then
        assertThat(event.getStatus()).isEqualTo(EventStatus.PENDING);
        assertThat(event.getRetryCount()).isZero();
        assertThat(event.getPublishedAt()).isNull();
    }

    @Test
    @DisplayName("Event.create() - aggregateType, aggregateId, eventType, payload가 올바르게 설정됨")
    void create_fieldsAreSet() {
        // given
        UUID aggregateId = UUID.randomUUID();
        String payload = "{\"orderId\":\"test\"}";

        // when
        Event event = Event.create(AggregateType.ORDER, aggregateId, EventType.ORDER_CREATED, payload);

        // then
        assertThat(event.getAggregateType()).isEqualTo(AggregateType.ORDER);
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getEventType()).isEqualTo(EventType.ORDER_CREATED);
        assertThat(event.getPayload()).isEqualTo(payload);
    }

    // ======================== markPublished ========================

    @Test
    @DisplayName("markPublished() - status가 PUBLISHED로 변경되고 publishedAt이 설정됨")
    void markPublished_setsPublishedStatusAndTime() {
        // given
        Event event = Event.create(AggregateType.ORDER, UUID.randomUUID(), EventType.ORDER_CREATED, "{}");

        // when
        event.markPublished();

        // then
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
    }

    // ======================== markFailed ========================

    @Test
    @DisplayName("markFailed() - retryCount가 MAX_RETRY 미만이면 PENDING 유지")
    void markFailed_belowMaxRetry_staysPending() {
        // given
        Event event = Event.create(AggregateType.ORDER, UUID.randomUUID(), EventType.ORDER_CREATED, "{}");
        // retryCount = 0 → 1번 실패 → retryCount=1 < MAX_RETRY=3

        // when
        event.markFailed(MAX_RETRY);

        // then
        assertThat(event.getStatus()).isEqualTo(EventStatus.PENDING);
        assertThat(event.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("markFailed() - retryCount가 MAX_RETRY에 도달하면 FAILED로 변경")
    void markFailed_reachesMaxRetry_becomesFailed() {
        // given
        Event event = Event.create(AggregateType.ORDER, UUID.randomUUID(), EventType.ORDER_CREATED, "{}");
        // 3번 실패시켜 retryCount = MAX_RETRY가 되도록
        event.markFailed(MAX_RETRY); // retryCount=1
        event.markFailed(MAX_RETRY); // retryCount=2
        event.markFailed(MAX_RETRY); // retryCount=3 → FAILED

        // then
        assertThat(event.getStatus()).isEqualTo(EventStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(MAX_RETRY);
    }

    @Test
    @DisplayName("markFailed() - MAX_RETRY 직전까지는 PENDING 유지")
    void markFailed_justBelowMaxRetry_staysPending() {
        // given
        Event event = Event.create(AggregateType.ORDER, UUID.randomUUID(), EventType.ORDER_CREATED, "{}");

        // when - MAX_RETRY-1 번 실패
        for (int i = 0; i < MAX_RETRY - 1; i++) {
            event.markFailed(MAX_RETRY);
        }

        // then
        assertThat(event.getStatus()).isEqualTo(EventStatus.PENDING);
        assertThat(event.getRetryCount()).isEqualTo(MAX_RETRY - 1);
    }

    // ======================== isExhausted ========================

    @Test
    @DisplayName("isExhausted() - retryCount < maxRetry 이면 false")
    void isExhausted_belowMaxRetry_returnsFalse() {
        // given
        Event event = Event.create(AggregateType.ORDER, UUID.randomUUID(), EventType.ORDER_CREATED, "{}");
        event.markFailed(MAX_RETRY); // retryCount=1

        // then
        assertThat(event.isExhausted(MAX_RETRY)).isFalse();
    }

    @Test
    @DisplayName("isExhausted() - retryCount >= maxRetry 이면 true")
    void isExhausted_atOrAboveMaxRetry_returnsTrue() {
        // given
        Event event = Event.create(AggregateType.ORDER, UUID.randomUUID(), EventType.ORDER_CREATED, "{}");
        event.markFailed(MAX_RETRY); // retryCount=1
        event.markFailed(MAX_RETRY); // retryCount=2
        event.markFailed(MAX_RETRY); // retryCount=3

        // then
        assertThat(event.isExhausted(MAX_RETRY)).isTrue();
    }
}
