package com.djesystems.kawa.customer.infrastructure.persistence;

import com.djesystems.kawa.customer.domain.NotificationStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "notification_outbox",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_event",
                        columnNames = "event_id"
                )
        }
)
public class NotificationOutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "public_kawa_id", nullable = false, length = 32)
    private String publicKawaId;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "body", nullable = false, length = 1000)
    private String body;

    @Column(name = "payload", columnDefinition = "json")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    protected NotificationOutboxEntity() {
    }

    public NotificationOutboxEntity(
            String eventId,
            String publicKawaId,
            String notificationType,
            String title,
            String body,
            String payload) {

        this.eventId = eventId;
        this.publicKawaId = publicKawaId;
        this.notificationType = notificationType;
        this.title = title;
        this.body = body;
        this.payload = payload;
        this.status = NotificationStatus.PENDING;
        this.createdAt = Instant.now();
        this.retryCount = 0;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getPublicKawaId() {
        return publicKawaId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getPayload() {
        return payload;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markFailed() {
        this.status = NotificationStatus.FAILED;
        this.retryCount++;
    }
}