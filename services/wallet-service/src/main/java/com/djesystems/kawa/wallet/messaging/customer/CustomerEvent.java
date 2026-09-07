package com.djesystems.kawa.wallet.messaging.customer;

import java.time.Instant;

import com.djesystems.kawa.wallet.domain.CustomerProjectionStatus;

public record CustomerEvent(

        String eventId,

        String eventType,

        Instant occurredAt,

        String publicKawaId,

        CustomerProjectionStatus status,

        String email

) {
}