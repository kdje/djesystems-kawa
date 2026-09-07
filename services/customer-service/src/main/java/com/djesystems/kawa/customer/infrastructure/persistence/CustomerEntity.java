package com.djesystems.kawa.customer.infrastructure.persistence;

import com.djesystems.kawa.customer.domain.Customer;
import com.djesystems.kawa.customer.domain.CustomerStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Représentation JPA du client dans la base kawa_customer.
 */
@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_customer_firebase_uid",
                        columnNames = "firebase_uid"
                ),
                @UniqueConstraint(
                        name = "uk_customer_public_kawa_id",
                        columnNames = "public_kawa_id"
                )
        }
)
public class CustomerEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "firebase_uid", nullable = false, length = 128)
    private String firebaseUid;

    @Column(name = "public_kawa_id", nullable = false, length = 32)
    private String publicKawaId;

    @Column(length = 320)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerEntity() {
        // Required by JPA
    }

    public CustomerEntity(
            UUID id,
            String firebaseUid,
            String publicKawaId,
            String email,
            CustomerStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.firebaseUid = firebaseUid;
        this.publicKawaId = publicKawaId;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Customer toDomain() {
        return new Customer(
                id,
                firebaseUid,
                publicKawaId,
                email,
                status,
                createdAt,
                updatedAt
        );
    }

    public UUID getId() {
        return id;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getPublicKawaId() {
        return publicKawaId;
    }

    public String getEmail() {
        return email;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
