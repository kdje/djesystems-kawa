package com.djesystems.kawa.retailer.api;

public record CreateRetailerRequest(
        String code,
        String name,
        String countryCode,
        String logoUrl
) {
}