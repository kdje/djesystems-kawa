package com.djesystems.kawa.customer.infrastructure.messaging.dto;

import java.time.Instant;

public record CustomerRetailerConsentRequestedEvent(
        String eventId,
        String eventType,
        String publicKawaId,
        String retailerCode,
        Instant occurredAt
) {
}