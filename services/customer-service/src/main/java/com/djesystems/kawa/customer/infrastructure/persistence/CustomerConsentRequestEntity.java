package com.djesystems.kawa.customer.infrastructure.persistence;

import com.djesystems.kawa.customer.domain.ConsentRequestStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "customer_consent_request",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_consent_event_id",
                        columnNames = "event_id"
                )
        }
)
public class CustomerConsentRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "public_kawa_id", nullable = false, length = 32)
    private String publicKawaId;

    @Column(name = "retailer_code", nullable = false, length = 50)
    private String retailerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsentRequestStatus status;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CustomerConsentRequestEntity() {
        // Required by JPA
    }

    public CustomerConsentRequestEntity(
            String eventId,
            String publicKawaId,
            String retailerCode,
            ConsentRequestStatus status,
            Instant requestedAt,
            Instant createdAt) {

        this.eventId = eventId;
        this.publicKawaId = publicKawaId;
        this.retailerCode = retailerCode;
        this.status = status;
        this.requestedAt = requestedAt;
        this.createdAt = createdAt;
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

    public String getRetailerCode() {
        return retailerCode;
    }

    public ConsentRequestStatus getStatus() {
        return status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void decide(
            ConsentRequestStatus decision,
            Instant respondedAt) {

        if (decision != ConsentRequestStatus.APPROVED
                && decision != ConsentRequestStatus.REJECTED) {

            throw new IllegalArgumentException(
                    "Consent decision must be APPROVED or REJECTED"
            );
        }

        if (status != ConsentRequestStatus.PENDING) {

            throw new IllegalStateException(
                    "Consent request is already decided: "
                            + status
            );
        }

        this.status = decision;
        this.respondedAt = respondedAt;
    }
}