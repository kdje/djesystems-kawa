package com.djesystems.kawa.wallet.messaging.retailer;

import java.time.Instant;

import com.djesystems.kawa.wallet.domain.RetailerEnvironment;

public record RetailerCredentialCreatedEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String retailerId,
        String retailerCode,
        RetailerEnvironment environment,
        String entraClientId,
        boolean enabled
) {
}