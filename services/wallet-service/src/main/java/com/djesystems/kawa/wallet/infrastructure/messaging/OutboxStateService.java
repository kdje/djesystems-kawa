package com.djesystems.kawa.wallet.infrastructure.messaging;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventRepository;

@Service
public class OutboxStateService {

    private static final int MAX_ATTEMPTS = 5;

    private final OutboxEventRepository outboxEventRepository;


    public OutboxStateService(
            OutboxEventRepository outboxEventRepository) {

        this.outboxEventRepository = outboxEventRepository;
    }


    @Transactional
    public void markPublished(String eventId) {

        OutboxEventEntity event =
            outboxEventRepository.findById(eventId)
                .orElseThrow(
                    () -> new IllegalStateException(
                        "Outbox event not found: " + eventId
                    )
                );

        event.markPublished();
    }


    @Transactional
    public void markFailure(
            String eventId,
            String error) {

        OutboxEventEntity event =
            outboxEventRepository.findById(eventId)
                .orElseThrow(
                    () -> new IllegalStateException(
                        "Outbox event not found: " + eventId
                    )
                );

        if (event.getAttemptCount() >= MAX_ATTEMPTS) {

            event.markFailed(error);

        } else {

            event.markPending(error);
        }
    }
}