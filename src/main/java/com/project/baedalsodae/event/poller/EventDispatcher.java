package com.project.baedalsodae.event.poller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventType;

public interface EventDispatcher {

    EventType getSupportedEventType();

    void dispatch(Event event) throws JsonProcessingException;
}
