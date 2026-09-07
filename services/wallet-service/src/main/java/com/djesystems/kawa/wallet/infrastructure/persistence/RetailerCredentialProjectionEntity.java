package com.djesystems.kawa.wallet.infrastructure.persistence;

import java.time.LocalDateTime;
import com.djesystems.kawa.wallet.domain.RetailerEnvironment;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "retailer_credential_projection")
public class RetailerCredentialProjectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
        name = "retailer_id",
        nullable = false,
        length = 36,
        columnDefinition = "CHAR(36)"
    )
    private String retailerId;

    @Column(
        name = "retailer_code",
        nullable = false,
        length = 80
    )
    private String retailerCode;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "environment",
        nullable = false,
        length = 20
    )
    private RetailerEnvironment environment;

    @Column(
        name = "entra_client_id",
        nullable = false,
        unique = true,
        length = 36
    )
    private String entraClientId;

    @Column(
        name = "enabled",
        nullable = false
    )
    private boolean enabled;

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

    protected RetailerCredentialProjectionEntity() {
    }

    public RetailerCredentialProjectionEntity(
            String retailerId,
            String retailerCode,
            RetailerEnvironment environment,
            String entraClientId,
            boolean enabled) {

        this.retailerId = retailerId;
        this.retailerCode = retailerCode;
        this.environment = environment;
        this.entraClientId = entraClientId;
        this.enabled = enabled;
    }

    public void update(
            String retailerCode,
            String entraClientId,
            boolean enabled) {

        this.retailerCode = retailerCode;
        this.entraClientId = entraClientId;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public String getRetailerCode() {
        return retailerCode;
    }

    public RetailerEnvironment getEnvironment() {
        return environment;
    }

    public String getEntraClientId() {
        return entraClientId;
    }

    public boolean isEnabled() {
        return enabled;
    }
}