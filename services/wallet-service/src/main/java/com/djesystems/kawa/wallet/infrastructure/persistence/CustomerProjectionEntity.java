package com.djesystems.kawa.wallet.infrastructure.persistence;

import java.time.LocalDateTime;

import com.djesystems.kawa.wallet.domain.CustomerProjectionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_projection")
public class CustomerProjectionEntity {

    @Id
    @Column(
        name = "public_kawa_id",
        nullable = false,
        length = 100
    )
    private String publicKawaId;
    private String email;


    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 30
    )
    private CustomerProjectionStatus status;

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

    protected CustomerProjectionEntity() {
    }

    public CustomerProjectionEntity(
            String publicKawaId,
            CustomerProjectionStatus status,
            String email ) {

        this.publicKawaId = publicKawaId;
        this.status = status;
        this.email = email;
    }

    public String getPublicKawaId() {
        return publicKawaId;
    }

    public CustomerProjectionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(CustomerProjectionStatus status) {
        this.status = status;
    }

    public String getEmail() {
    return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}