package com.djesystems.kawa.customer.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Modèle métier représentant un client KAWA.
 *
 * Cette classe ne dépend volontairement pas de JPA.
 */
public record Customer(
        UUID id,
        String firebaseUid,
        String publicKawaId,
        String email,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
