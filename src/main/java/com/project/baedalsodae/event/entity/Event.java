package com.project.baedalsodae.event.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.payment.entity.Payment;
import io.lettuce.core.json.JsonType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class Event extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID traceId;
    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;
    private UUID aggregateId;
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Enumerated(EnumType.STRING)
    private EventStatus status;
    private int retryCount;
    private Instant publishedAt;

    private Event(
            AggregateType aggregateType, UUID aggregateId, EventType eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = EventStatus.PENDING;
        this.retryCount = 0;
    }

    public static Event fromPayment(Payment payment, EventType type, String payload) {
        return new Event(AggregateType.PAYMENT, payment.getId(), type, payload);
    }

    public static Event fromOrder(final Order order, final EventType type, String payload) {
        return new Event(AggregateType.ORDER, order.getId(), type, payload);
    }

    public void markPublished() {
        this.status = EventStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public void markFailed(int maxRetry) {
        this.retryCount++;
        // 재시도 횟수가 남아있으면 PENDING으로 되돌려 다음 폴링에서 재시도
        // 최대 재시도 초과 시 최종 FAILED 처리
        this.status = this.retryCount < maxRetry ? EventStatus.PENDING : EventStatus.FAILED;
    }

    public boolean isExhausted(int maxRetry) {
        return this.retryCount >= maxRetry;
    }

    public static Event fromOrder(Order order, EventType type) {
        return new Event(AggregateType.ORDER, order.getId(), type, null);
    }

    public static Event create(
            AggregateType aggregateType, UUID aggregateId, EventType eventType, String payload) {
        return new Event(aggregateType, aggregateId, eventType, payload);
    }
}
