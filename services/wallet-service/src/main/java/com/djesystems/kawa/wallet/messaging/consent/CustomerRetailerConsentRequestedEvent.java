package com.djesystems.kawa.wallet.messaging.consent;

import java.time.Instant;

public record CustomerRetailerConsentRequestedEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String publicKawaId,
        String retailerCode
) {
}