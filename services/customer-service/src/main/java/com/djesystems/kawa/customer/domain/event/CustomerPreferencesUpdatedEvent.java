package com.djesystems.kawa.customer.domain.event;

import java.time.Instant;

public record CustomerPreferencesUpdatedEvent(
    String eventId,
    String eventType,
    String publicKawaId,
    String email,
    String status,
    boolean autoRetailerAssociationEnabled,
    Instant occurredAt
) {
}
