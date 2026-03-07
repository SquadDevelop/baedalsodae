package com.project.baedalsodae.event.repository;

import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findTop10ByStatusOrderByCreatedAtAsc(EventStatus status);
}
