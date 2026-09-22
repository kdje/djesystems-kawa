package com.djesystems.kawa.customer.api.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCustomerPreferencesRequest(
        @NotNull Boolean autoRetailerAssociationEnabled
) {
}
