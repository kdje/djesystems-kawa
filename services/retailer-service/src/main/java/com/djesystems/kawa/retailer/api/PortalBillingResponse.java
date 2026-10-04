package com.djesystems.kawa.retailer.api;

public record PortalBillingResponse(
        String companyName,
        String billingEmail,
        String billingAddress,
        String externalProvider,
        String externalAccountId) {
}
