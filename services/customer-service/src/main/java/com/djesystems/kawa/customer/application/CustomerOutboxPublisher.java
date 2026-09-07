package com.djesystems.kawa.customer.application;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import com.djesystems.kawa.customer.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.OutboxEventRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CustomerOutboxPublisher {

    private final OutboxEventRepository repository;
    private final ServiceBusSenderClient sender;

    public CustomerOutboxPublisher(
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

            ServiceBusMessage message =
                new ServiceBusMessage(event.getPayload());

            message.setMessageId(event.getId());

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

            /*
             * Si Azure Service Bus est indisponible,
             * cette ligne lève une exception et
             * published_at reste NULL.
             */
            sender.sendMessage(message);

            /*
             * Seulement après succès de l'envoi.
             */
            event.markPublished();

            repository.save(event);
        }
    }
}