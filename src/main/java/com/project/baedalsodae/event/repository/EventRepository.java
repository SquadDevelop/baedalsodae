package com.project.baedalsodae.event.repository;

import com.project.baedalsodae.event.entity.Event;
import com.project.baedalsodae.event.entity.EventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findTop10ByStatusOrderByCreatedAtAsc(EventStatus status);

    @Query(value = "SELECT * FROM baedalsodae.p_event WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT 10 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<Event> findTop10PendingWithLock();
}
