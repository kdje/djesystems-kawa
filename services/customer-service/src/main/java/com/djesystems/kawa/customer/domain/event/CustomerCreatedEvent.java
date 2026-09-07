package com.djesystems.kawa.customer.domain.event;

import java.time.Instant;

public record CustomerCreatedEvent(

    String eventId,

    String eventType,

    String publicKawaId,

    String email,

    String status,

    Instant occurredAt

) {
}