package com.djesystems.kawa.retailer.api;

import com.djesystems.kawa.retailer.domain.RetailerStatus;

public record RetailerResponse(
        String id,
        String code,
        String name,
        String countryCode,
        String logoUrl,
        RetailerStatus status
) {
}