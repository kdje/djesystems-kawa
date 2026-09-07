package com.djesystems.kawa.customer.domain;

import java.util.UUID;

/**
 * Fabrique des identifiants publics KAWA.
 *
 * Le publicKawaId :
 * - n'est pas l'identifiant technique de la base ;
 * - n'est pas le Firebase UID ;
 * - peut être exposé aux enseignes ;
 * - reste permanent tant que le compte existe.
 */
public final class PublicKawaIdFactory {

    private PublicKawaIdFactory() {
        // Utility class
    }

    /**
     * Génère un identifiant aléatoire de 32 caractères hexadécimaux.
     *
     * Exemple :
     * d73f8cb29aed4cc4941885b31e8d1327
     */
    public static String generate() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }
}
