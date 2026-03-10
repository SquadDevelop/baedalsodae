package com.project.baedalsodae.event.poller;

import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventStatus;
import com.project.baedalsodae.event.repository.EventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPoller {

    private static final int MAX_RETRY = 3;

    private final EventRepository eventRepository;
    private final List<EventDispatcher> dispatchers;

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        List<Event> pendingEvents =
                eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING);

        for (Event event : pendingEvents) {
            if (event.isExhausted(MAX_RETRY)) {
                log.warn(
                        "[OutboxPoller] 최대 재시도 초과, 이벤트 무시: id={}, type={}",
                        event.getId(),
                        event.getEventType());
                continue;
            }

            try {
                //                dispatch(event);

                dispatchers.stream()
                        .filter(d -> d.getSupportedEventType() == event.getEventType())
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "[OutboxPoller] 처리되지 않은 이벤트 타입: "
                                                        + event.getEventType()))
                        .dispatch(event);

                event.markPublished();
                eventRepository.save(event);

                log.info(
                        "[OutboxPoller] 이벤트 발행 성공: id={}, type={}",
                        event.getId(),
                        event.getEventType());
            } catch (Exception e) {
                event.markFailed(MAX_RETRY); // retryCount < MAX_RETRY → PENDING, 이상이면 FAILED
                log.error(
                        "[OutboxPoller] 이벤트 발행 실패 (retryCount={}): id={}, type={}, error={}",
                        event.getRetryCount(),
                        event.getId(),
                        event.getEventType(),
                        e.getMessage());
            }
        }
    }

    //    private void dispatch(Event event) throws JsonProcessingException {
    //        if (event.getEventType() == EventType.ORDER_CREATED) {
    //            JsonNode node = objectMapper.readTree(event.getPayload());
    //            UUID orderId = UUID.fromString(node.get("orderId").asText());
    //            UUID userId = UUID.fromString(node.get("userId").asText());
    //            BigDecimal finalAmount = node.get("finalAmount").decimalValue();
    //
    //            eventPublisher.publishEvent(new OrderCreatedEvent(orderId, userId, finalAmount));
    //        } else {
    //            log.warn("[OutboxPoller] 처리되지 않은 이벤트 타입: {}", event.getEventType());
    //        }
    //    }
}
