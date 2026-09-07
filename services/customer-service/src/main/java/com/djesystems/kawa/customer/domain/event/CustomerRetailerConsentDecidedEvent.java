package com.djesystems.kawa.customer.domain.event;

import java.time.Instant;

public record CustomerRetailerConsentDecidedEvent(

        String eventId,

        String eventType,

        String consentRequestEventId,

        Instant occurredAt,

        String publicKawaId,

        String retailerCode,

        String decision

) {
}