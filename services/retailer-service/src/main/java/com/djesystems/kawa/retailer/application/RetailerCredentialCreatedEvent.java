package com.djesystems.kawa.retailer.application;

import java.time.Instant;
import com.djesystems.kawa.retailer.domain.RetailerEnvironment;

public record RetailerCredentialCreatedEvent(

        String eventId,

        String eventType,

        Instant occurredAt,

        String retailerId,

        String retailerCode,

        RetailerEnvironment environment,

        String entraClientId,

        boolean enabled

) {
}