package com.djesystems.kawa.wallet.infrastructure.messaging;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventRepository;

@Service
public class OutboxClaimService {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 5;

    /*
     * Si un événement reste PROCESSING plus de 5 minutes,
     * on considère que l'instance qui le traitait est probablement
     * tombée et qu'il peut être repris.
     */
    private static final Duration PROCESSING_TIMEOUT =
        Duration.ofMinutes(5);

    private final OutboxEventRepository outboxEventRepository;


    public OutboxClaimService(
            OutboxEventRepository outboxEventRepository) {

        this.outboxEventRepository = outboxEventRepository;
    }


    @Transactional
    public List<OutboxEventEntity> claimBatch() {

        Instant staleBefore =
            Instant.now().minus(PROCESSING_TIMEOUT);

        List<OutboxEventEntity> events =
            outboxEventRepository.findBatchForProcessing(
                staleBefore,
                MAX_ATTEMPTS,
                BATCH_SIZE
            );

        for (OutboxEventEntity event : events) {

            event.markProcessing();
        }

        /*
         * Force l'UPDATE avant le COMMIT.
         */
        outboxEventRepository.flush();

        return events;
    }
}