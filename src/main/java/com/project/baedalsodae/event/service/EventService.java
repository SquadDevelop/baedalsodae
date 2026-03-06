package com.project.baedalsodae.event.service;

import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public void save(Event event) {
        eventRepository.save(event);
    }
}
