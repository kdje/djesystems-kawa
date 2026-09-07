package com.djesystems.kawa.customer.domain;

/**
 * État fonctionnel d'un client KAWA.
 */
public enum CustomerStatus {

    /**
     * Compte utilisable normalement.
     */
    ACTIVE,

    /**
     * Compte temporairement désactivé.
     */
    SUSPENDED,

    /**
     * Compte supprimé fonctionnellement.
     */
    DELETED
}
