package com.project.baedalsodae.event.poller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPoller {

    private final EventPollerService eventPollerService;

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        eventPollerService.processEvents();
    }
}
