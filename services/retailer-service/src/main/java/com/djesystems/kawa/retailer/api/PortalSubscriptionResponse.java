package com.djesystems.kawa.retailer.api;

import java.time.LocalDate;

public record PortalSubscriptionResponse(
        String plan,
        String status,
        LocalDate startDate,
        LocalDate renewalDate,
        String billingCycle) {
}
