package com.djesystems.kawa.retailer.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import com.djesystems.kawa.retailer.domain.RetailerUserRole;
import com.djesystems.kawa.retailer.domain.RetailerUserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "retailer_user")
public class RetailerUserEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "retailer_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String retailerId;

    @Column(name = "identity_provider", nullable = false, length = 30)
    private String identityProvider;

    @Column(name = "identity_tenant_id", nullable = false, length = 180)
    private String identityTenantId;

    @Column(name = "identity_subject", nullable = false, length = 255)
    private String identitySubject;

    @Column(name = "email", length = 320)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private RetailerUserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RetailerUserStatus status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected RetailerUserEntity() {
    }

    public RetailerUserEntity(
            String retailerId,
            String identityProvider,
            String identityTenantId,
            String identitySubject,
            String email,
            RetailerUserRole role,
            RetailerUserStatus status) {
        this.id = UUID.randomUUID().toString();
        this.retailerId = retailerId;
        this.identityProvider = identityProvider;
        this.identityTenantId = identityTenantId;
        this.identitySubject = identitySubject;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public String getId() { return id; }
    public String getRetailerId() { return retailerId; }
    public String getIdentityProvider() { return identityProvider; }
    public String getIdentityTenantId() { return identityTenantId; }
    public String getIdentitySubject() { return identitySubject; }
    public String getEmail() { return email; }
    public RetailerUserRole getRole() { return role; }
    public RetailerUserStatus getStatus() { return status; }
}
