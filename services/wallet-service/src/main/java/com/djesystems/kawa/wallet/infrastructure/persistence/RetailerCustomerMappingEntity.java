package com.djesystems.kawa.wallet.infrastructure.persistence;

import java.time.LocalDateTime;

import com.djesystems.kawa.wallet.domain.MappingStatus;

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
    name = "retailer_customer_mapping",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_wallet_public_kawa_retailer",
            columnNames = {
                "public_kawa_id",
                "retailer_code"
            }
        )
    }
)
public class RetailerCustomerMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "public_kawa_id",
        nullable = false,
        length = 100
    )
    private String publicKawaId;

    @Column(
        name = "retailer_code",
        nullable = false,
        length = 100
    )
    private String retailerCode;

    @Column(
        name = "retailer_customer_id",
        length = 255
    )
    private String retailerCustomerId;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 30
    )
    private MappingStatus status;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime updatedAt;

    protected RetailerCustomerMappingEntity() {
    }

    public RetailerCustomerMappingEntity(
            String publicKawaId,
            String retailerCode,
            String retailerCustomerId,
            MappingStatus status) {

        this.publicKawaId = publicKawaId;
        this.retailerCode = retailerCode;
        this.retailerCustomerId = retailerCustomerId;
        this.status = status;
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

    public String getRetailerCustomerId() {
        return retailerCustomerId;
    }

    public MappingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void activate(String retailerCustomerId) {

        if (retailerCustomerId == null
                || retailerCustomerId.isBlank()) {

            throw new IllegalArgumentException(
                "Retailer customer ID must not be blank"
            );
        }

        /*
        * Appel idempotent :
        * AUCHAN renvoie le même identifiant.
        */
        if (status == MappingStatus.ACTIVE) {

            if (retailerCustomerId.equals(
                    this.retailerCustomerId)) {

                return;
            }

            throw new IllegalStateException(
                "Mapping is already ACTIVE with another retailer customer ID"
            );
        }

        /*
        * Impossible d'activer sans consentement préalable.
        */
        if (status != MappingStatus.CONSENT_APPROVED) {

            throw new IllegalStateException(
                "Cannot activate mapping from status "
                    + status
            );
        }

        this.retailerCustomerId =
            retailerCustomerId;

        this.status =
            MappingStatus.ACTIVE;
    }

    public void approveConsent() {

        if (status == MappingStatus.CONSENT_APPROVED) {
            return;
        }

        if (status != MappingStatus.PENDING_CONSENT) {
            throw new IllegalStateException(
                "Cannot approve consent from status " + status
            );
        }

        this.status = MappingStatus.CONSENT_APPROVED;
    }


    public void rejectConsent() {

        if (status == MappingStatus.CONSENT_REJECTED) {
            return;
        }

        if (status != MappingStatus.PENDING_CONSENT) {
            throw new IllegalStateException(
                "Cannot reject consent from status " + status
            );
        }

        this.status = MappingStatus.CONSENT_REJECTED;
    }
}