package com.djesystems.kawa.retailer.infrastructure.persistence;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_event")
public class OutboxEventEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
        name = "id",
        nullable = false,
        length = 36,
        columnDefinition = "CHAR(36)"
    )
    private String id;

    @Column(
        name = "aggregate_type",
        nullable = false,
        length = 100
    )
    private String aggregateType;

    @Column(
        name = "aggregate_id",
        nullable = false,
        length = 100
    )
    private String aggregateId;

    @Column(
        name = "event_type",
        nullable = false,
        length = 150
    )
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "payload",
        nullable = false,
        columnDefinition = "json"
    )
    private String payload;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    protected OutboxEventEntity() {
    }

    public OutboxEventEntity(
            String id,
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload) {

        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public void markPublished() {
        this.publishedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}