package com.djesystems.kawa.customer.infrastructure.persistence;

import com.djesystems.kawa.customer.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationOutboxRepository
        extends JpaRepository<NotificationOutboxEntity, Long> {

    List<NotificationOutboxEntity>
        findByStatusOrderByCreatedAtAsc(NotificationStatus status);

    boolean existsByEventId(String eventId);
}