package com.djesystems.kawa.wallet.infrastructure.messaging;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventEntity;

@Component
public class OutboxPublisher {

    private static final Logger log =
        LoggerFactory.getLogger(
            OutboxPublisher.class
        );

    private final OutboxClaimService outboxClaimService;
    private final OutboxStateService outboxStateService;
    private final EventPublisher eventPublisher;


    public OutboxPublisher(
            OutboxClaimService outboxClaimService,
            OutboxStateService outboxStateService,
            EventPublisher eventPublisher) {

        this.outboxClaimService = outboxClaimService;
        this.outboxStateService = outboxStateService;
        this.eventPublisher = eventPublisher;
    }


    @Scheduled(
        fixedDelayString =
            "${kawa.wallet.outbox.fixed-delay-ms:2000}"
    )
    public void publishPendingEvents() {

        /*
         * Première transaction :
         *
         * PENDING -> PROCESSING
         *
         * Elle est terminée avant l'appel au bus.
         */
        List<OutboxEventEntity> events =
            outboxClaimService.claimBatch();

        if (events.isEmpty()) {
            return;
        }

        log.debug(
            "{} Outbox event(s) claimed",
            events.size()
        );

        /*
         * Aucun lock DB n'est conservé ici.
         */
        for (OutboxEventEntity event : events) {

            publish(event);
        }
    }


    private void publish(
            OutboxEventEntity event) {

        try {

            log.info(
                "Publishing Outbox event {} type {}",
                event.getEventId(),
                event.getEventType()
            );


            /*
             * Aujourd'hui :
             * LoggingEventPublisher.
             *
             * Plus tard :
             * AzureServiceBusEventPublisher.
             */
            eventPublisher.publish(
                event.getEventId(),
                event.getEventType(),
                event.getPayload()
            );


            /*
             * Nouvelle petite transaction SQL :
             *
             * PROCESSING -> PUBLISHED
             */
            outboxStateService.markPublished(
                event.getEventId()
            );


            log.info(
                "Outbox event {} published successfully",
                event.getEventId()
            );

        } catch (Exception e) {

            String error =
                e.getMessage() != null
                    ? e.getMessage()
                    : e.getClass().getName();

            log.error(
                "Unable to publish Outbox event {}",
                event.getEventId(),
                e
            );


            /*
             * Nouvelle transaction :
             *
             * PROCESSING -> PENDING
             *
             * ou :
             *
             * PROCESSING -> FAILED
             */
            outboxStateService.markFailure(
                event.getEventId(),
                error
            );
        }
    }
}