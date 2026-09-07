package com.djesystems.kawa.wallet.infrastructure.persistence;

public enum OutboxEventStatus {

    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}