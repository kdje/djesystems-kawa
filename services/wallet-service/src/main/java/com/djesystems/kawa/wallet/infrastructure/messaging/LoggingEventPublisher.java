package com.djesystems.kawa.wallet.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "kawa.wallet.outbox.transport",
    havingValue = "log",
    matchIfMissing = true
)
public class LoggingEventPublisher
        implements EventPublisher {

    private static final Logger log =
        LoggerFactory.getLogger(
            LoggingEventPublisher.class
        );

    @Override
    public void publish(
            String eventId,
            String eventType,
            String payload) {

        log.info(
            """
            Publishing Outbox event:
            eventId={}
            eventType={}
            payload={}
            """,
            eventId,
            eventType,
            payload
        );
    }
}