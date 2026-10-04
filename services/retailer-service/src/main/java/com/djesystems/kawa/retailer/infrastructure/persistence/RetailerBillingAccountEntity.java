package com.djesystems.kawa.retailer.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "retailer_billing_account")
public class RetailerBillingAccountEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "retailer_id", nullable = false, unique = true, length = 36, columnDefinition = "CHAR(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String retailerId;

    @Column(name = "company_name", nullable = false, length = 180)
    private String companyName;

    @Column(name = "billing_email", nullable = false, length = 320)
    private String billingEmail;

    @Column(name = "billing_address", length = 1000)
    private String billingAddress;

    @Column(name = "external_provider", length = 100)
    private String externalProvider;

    @Column(name = "external_account_id", length = 255)
    private String externalAccountId;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected RetailerBillingAccountEntity() {
    }

    public RetailerBillingAccountEntity(
            String retailerId,
            String companyName,
            String billingEmail,
            String billingAddress,
            String externalProvider,
            String externalAccountId) {
        this.id = UUID.randomUUID().toString();
        this.retailerId = retailerId;
        this.companyName = companyName;
        this.billingEmail = billingEmail;
        this.billingAddress = billingAddress;
        this.externalProvider = externalProvider;
        this.externalAccountId = externalAccountId;
    }

    public String getCompanyName() { return companyName; }
    public String getBillingEmail() { return billingEmail; }
    public String getBillingAddress() { return billingAddress; }
    public String getExternalProvider() { return externalProvider; }
    public String getExternalAccountId() { return externalAccountId; }
}
