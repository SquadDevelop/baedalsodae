package com.project.baedalsodae.event.poller;

import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPollerService {

    private static final int MAX_RETRY = 3;
	 private final List<EventDispatcher> dispatchers;

	final private EventRepository eventRepository;

    @Transactional
    public void processEvents() {

//        List<Event> pendingEvents =
//                eventRepository.findTop10ByStatusOrderByCreatedAtAsc(EventStatus.PENDING);

        List<Event> pendingEvents = eventRepository.findTop10PendingWithLock();

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
}
