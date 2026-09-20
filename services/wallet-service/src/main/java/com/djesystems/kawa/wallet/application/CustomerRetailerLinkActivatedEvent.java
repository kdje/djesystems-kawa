package com.djesystems.kawa.wallet.application;

import java.time.Instant;

public record CustomerRetailerLinkActivatedEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String publicKawaId,
        String retailerCode,
        String retailerCustomerId
) {
}