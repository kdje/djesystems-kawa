package com.djesystems.kawa.wallet.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_event")
public class OutboxEventEntity {

    @Id
    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", length = 2000)
    private String lastError;


    protected OutboxEventEntity() {
        // JPA
    }


    public OutboxEventEntity(
            String eventId,
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload) {

        this.eventId = eventId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;

        this.status = OutboxEventStatus.PENDING;
        this.createdAt = Instant.now();
        this.attemptCount = 0;
    }


    public void markProcessing() {

        this.status = OutboxEventStatus.PROCESSING;
        this.processingStartedAt = Instant.now();
        this.attemptCount++;
    }


    public void markPublished() {

        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.processingStartedAt = null;
        this.lastError = null;
    }


    public void markPending(String error) {

        this.status = OutboxEventStatus.PENDING;
        this.processingStartedAt = null;
        this.lastError = truncate(error);
    }


    public void markFailed(String error) {

        this.status = OutboxEventStatus.FAILED;
        this.processingStartedAt = null;
        this.lastError = truncate(error);
    }


    private String truncate(String value) {

        if (value == null) {
            return null;
        }

        return value.length() <= 2000
            ? value
            : value.substring(0, 2000);
    }


    public String getEventId() {
        return eventId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxEventStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessingStartedAt() {
        return processingStartedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public String getLastError() {
        return lastError;
    }
}