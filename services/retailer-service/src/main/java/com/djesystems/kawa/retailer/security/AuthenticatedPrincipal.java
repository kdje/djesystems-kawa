package com.djesystems.kawa.retailer.security;

import java.security.Principal;

/**
 * Provider-neutral identity established by a trusted authentication adapter.
 */
public record AuthenticatedPrincipal(
        String subject,
        String email,
        String identityProvider,
        String identityTenantId) implements Principal {

    @Override
    public String getName() {
        return subject;
    }
}
