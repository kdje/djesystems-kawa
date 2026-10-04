package com.djesystems.kawa.retailer.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.djesystems.kawa.retailer.domain.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "retailer_subscription")
public class RetailerSubscriptionEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "retailer_id", nullable = false, unique = true, length = 36, columnDefinition = "CHAR(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String retailerId;

    @Column(name = "plan", nullable = false, length = 80)
    private String plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SubscriptionStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "renewal_date")
    private LocalDate renewalDate;

    @Column(name = "billing_cycle", length = 30)
    private String billingCycle;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected RetailerSubscriptionEntity() {
    }

    public RetailerSubscriptionEntity(
            String retailerId,
            String plan,
            SubscriptionStatus status,
            LocalDate startDate,
            LocalDate renewalDate,
            String billingCycle) {
        this.id = UUID.randomUUID().toString();
        this.retailerId = retailerId;
        this.plan = plan;
        this.status = status;
        this.startDate = startDate;
        this.renewalDate = renewalDate;
        this.billingCycle = billingCycle;
    }

    public String getPlan() { return plan; }
    public SubscriptionStatus getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getRenewalDate() { return renewalDate; }
    public String getBillingCycle() { return billingCycle; }
}
