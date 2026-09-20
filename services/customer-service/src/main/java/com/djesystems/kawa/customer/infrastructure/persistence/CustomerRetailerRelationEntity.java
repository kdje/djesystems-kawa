package com.djesystems.kawa.customer.infrastructure.persistence;

import java.time.Instant;

import com.djesystems.kawa.customer.domain.CustomerRetailerRelationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "customer_retailer_relation",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_customer_retailer_relation",
            columnNames = {
                "public_kawa_id",
                "retailer_code"
            }
        )
    }
)
public class CustomerRetailerRelationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "public_kawa_id",
        nullable = false,
        length = 32
    )
    private String publicKawaId;

    @Column(
        name = "retailer_code",
        nullable = false,
        length = 64
    )
    private String retailerCode;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 32
    )
    private CustomerRetailerRelationStatus status;

    @Column(
        name = "source_event_id",
        length = 36
    )
    private String sourceEventId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerRetailerRelationEntity() {
    }

    public CustomerRetailerRelationEntity(
            String publicKawaId,
            String retailerCode,
            CustomerRetailerRelationStatus status,
            String sourceEventId) {

        Instant now = Instant.now();

        this.publicKawaId = publicKawaId;
        this.retailerCode = retailerCode;
        this.status = status;
        this.sourceEventId = sourceEventId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getPublicKawaId() {
        return publicKawaId;
    }

    public String getRetailerCode() {
        return retailerCode;
    }

    public CustomerRetailerRelationStatus getStatus() {
        return status;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateStatus(
            CustomerRetailerRelationStatus status,
            String sourceEventId) {

        this.status = status;
        this.sourceEventId = sourceEventId;
        this.updatedAt = Instant.now();
    }
}