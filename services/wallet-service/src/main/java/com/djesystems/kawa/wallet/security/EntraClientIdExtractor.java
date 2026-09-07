package com.djesystems.kawa.wallet.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class EntraClientIdExtractor {

    public String extract(Jwt jwt) {

        String clientId =
                jwt.getClaimAsString("azp");

        if (clientId == null || clientId.isBlank()) {
            clientId =
                jwt.getClaimAsString("appid");
        }

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException(
                "No Entra client identifier found in JWT"
            );
        }

        return clientId;
    }
}