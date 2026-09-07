package com.djesystems.kawa.retailer.api;

import com.djesystems.kawa.retailer.domain.RetailerEnvironment;

public record CreateRetailerCredentialRequest(
        RetailerEnvironment environment,
        String entraClientId
) {
}