package com.djesystems.kawa.wallet.messaging.retailer;

import java.time.Instant;

import com.djesystems.kawa.wallet.domain.RetailerProjectionStatus;

public record RetailerCreatedEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String retailerId,
        String retailerCode,
        String name,
        String countryCode,
        RetailerProjectionStatus status
) {
}