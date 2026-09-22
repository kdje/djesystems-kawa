package com.djesystems.kawa.customer.infrastructure.messaging.dto;

import java.time.Instant;

public record CustomerRetailerConsentApprovedEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String publicKawaId,
        String retailerCode,
        String consentMode
) {
}
