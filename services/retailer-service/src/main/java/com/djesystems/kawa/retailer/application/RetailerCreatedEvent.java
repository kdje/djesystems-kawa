package com.djesystems.kawa.retailer.application;

import java.time.Instant;

import com.djesystems.kawa.retailer.domain.RetailerStatus;

public record RetailerCreatedEvent(

        String eventId,

        String eventType,

        Instant occurredAt,

        String retailerId,

        String retailerCode,

        String name,

        String countryCode,

        RetailerStatus status

) {
}