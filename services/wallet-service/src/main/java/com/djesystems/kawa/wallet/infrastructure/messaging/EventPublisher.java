package com.djesystems.kawa.wallet.infrastructure.messaging;

public interface EventPublisher {

    void publish(
        String eventId,
        String eventType,
        String payload
    );
}