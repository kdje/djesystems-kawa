package com.djesystems.kawa.wallet.messaging.customer;

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