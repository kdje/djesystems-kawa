package com.djesystems.kawa.retailer.api;

import com.djesystems.kawa.retailer.domain.RetailerEnvironment;

public record RetailerCredentialResponse(
        String id,
        String retailerId,
        RetailerEnvironment environment,
        String entraClientId,
        boolean enabled
) {
}