package com.djesystems.kawa.wallet.infrastructure.persistence;

import java.time.LocalDateTime;

import com.djesystems.kawa.wallet.domain.RetailerProjectionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "retailer_projection")
public class RetailerProjectionEntity {

    @Id
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
        unique = true,
        length = 80
    )
    private String retailerCode;

    @Column(
        name = "name",
        nullable = false,
        length = 180
    )
    private String name;

    @Column(
        name = "country_code",
        nullable = false,
        length = 2
    )
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 30
    )
    private RetailerProjectionStatus status;

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

    protected RetailerProjectionEntity() {
    }

    public RetailerProjectionEntity(
            String retailerId,
            String retailerCode,
            String name,
            String countryCode,
            RetailerProjectionStatus status) {

        this.retailerId = retailerId;
        this.retailerCode = retailerCode;
        this.name = name;
        this.countryCode = countryCode;
        this.status = status;
    }

    public void update(
            String retailerCode,
            String name,
            String countryCode,
            RetailerProjectionStatus status) {

        this.retailerCode = retailerCode;
        this.name = name;
        this.countryCode = countryCode;
        this.status = status;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public String getRetailerCode() {
        return retailerCode;
    }

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public RetailerProjectionStatus getStatus() {
        return status;
    }
}