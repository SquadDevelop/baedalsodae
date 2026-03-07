package com.project.baedalsodae.event.poller;

import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventStatus;
import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.repository.EventRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class EventPoller {

    private static final int MAX_RETRY = 3;

    private final EventRepository eventRepository;
    private final Map<EventType, EventDispatcher> dispatcherMap;

    public EventPoller(EventRepository eventRepository, List<EventDispatcher> dispatchers) {
        this.eventRepository = eventRepository;
        this.dispatcherMap =
                dispatchers.stream()
                        .collect(
                                Collectors.toMap(
                                        EventDispatcher::getSupportedEventType,
                                        Function.identity()));
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
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
                dispatch(event);
                event.markPublished();
                log.info(
                        "[OutboxPoller] 이벤트 발행 성공: id={}, type={}",
                        event.getId(),
                        event.getEventType());
            } catch (Exception e) {
                event.markFailed(MAX_RETRY);
                log.error(
                        "[OutboxPoller] 이벤트 발행 실패 (retryCount={}): id={}, type={}, error={}",
                        event.getRetryCount(),
                        event.getId(),
                        event.getEventType(),
                        e.getMessage());
            }
        }
    }

    private void dispatch(Event event) throws Exception {
        EventDispatcher dispatcher = dispatcherMap.get(event.getEventType());
        if (dispatcher == null) {
            log.warn("[OutboxPoller] 처리되지 않은 이벤트 타입: {}", event.getEventType());
            return;
        }
        dispatcher.dispatch(event);
    }
}
