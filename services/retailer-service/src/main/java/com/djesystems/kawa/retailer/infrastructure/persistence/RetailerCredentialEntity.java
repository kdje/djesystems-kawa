package com.djesystems.kawa.retailer.infrastructure.persistence;

import java.time.LocalDateTime;
import com.djesystems.kawa.retailer.domain.RetailerEnvironment;
import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "retailer_credential")
public class RetailerCredentialEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
        name = "id",
        nullable = false,
        length = 36,
        columnDefinition = "CHAR(36)"
    )
    private String id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
        name = "retailer_id",
        nullable = false,
        length = 36,
        columnDefinition = "CHAR(36)"
    )
    private String retailerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 20)
    private RetailerEnvironment environment;

    @Column(
        name = "entra_client_id",
        nullable = false,
        unique = true,
        length = 36
    )
    private String entraClientId;

    @Column(name = "enabled", nullable = false)
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

    protected RetailerCredentialEntity() {
    }

    public RetailerCredentialEntity(
            String id,
            String retailerId,
            RetailerEnvironment environment,
            String entraClientId,
            boolean enabled) {

        this.id = id;
        this.retailerId = retailerId;
        this.environment = environment;
        this.entraClientId = entraClientId;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getRetailerId() {
        return retailerId;
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