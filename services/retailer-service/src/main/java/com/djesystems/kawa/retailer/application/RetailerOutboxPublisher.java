package com.djesystems.kawa.retailer.application;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import com.djesystems.kawa.retailer.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.OutboxEventRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetailerOutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(RetailerOutboxPublisher.class);

    private final OutboxEventRepository repository;
    private final ServiceBusSenderClient sender;

    public RetailerOutboxPublisher(
            OutboxEventRepository repository,
            ServiceBusSenderClient sender) {

        this.repository = repository;
        this.sender = sender;
    }

    @Scheduled(
        fixedDelayString =
            "${kawa.outbox.publication-delay-ms:2000}"
    )
    public void publishPendingEvents() {

        var events =
                repository
                    .findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEventEntity event : events) {

            try {

                ServiceBusMessage message =
                        new ServiceBusMessage(
                            event.getPayload()
                        );

                message.setMessageId(
                    event.getId()
                );

                message.setSubject(
                    event.getEventType()
                );

                message.setContentType(
                    "application/json"
                );

                message.setCorrelationId(
                    event.getAggregateId()
                );

                message.getApplicationProperties()
                    .put(
                        "eventType",
                        event.getEventType()
                    );

                message.getApplicationProperties()
                    .put(
                        "aggregateType",
                        event.getAggregateType()
                    );

                sender.sendMessage(message);

                event.markPublished();

                repository.save(event);

                log.info(
                    "Retailer event published: eventId={}, eventType={}, aggregateId={}",
                    event.getId(),
                    event.getEventType(),
                    event.getAggregateId()
                );

            } catch (Exception e) {

                /*
                 * published_at reste NULL.
                 * L'événement sera donc retenté au prochain cycle.
                 */
                log.error(
                    "Unable to publish Retailer event: eventId={}, eventType={}",
                    event.getId(),
                    event.getEventType(),
                    e
                );

                /*
                 * Ici on continue avec les autres événements.
                 * Pour notre MVP c'est acceptable.
                 */
            }
        }
    }
}