package com.djesystems.kawa.retailer.infrastructure.persistence;

import java.time.LocalDateTime;
import com.djesystems.kawa.retailer.domain.RetailerStatus;
import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "retailer")
public class RetailerEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
        name = "id",
        nullable = false,
        length = 36,
        columnDefinition = "CHAR(36)"
    )
    private String id;

    @Column(name = "code", nullable = false, unique = true, length = 80)
    private String code;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RetailerStatus status;

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

    protected RetailerEntity() {
    }

    public RetailerEntity(
            String id,
            String code,
            String name,
            String countryCode,
            String logoUrl,
            RetailerStatus status) {

        this.id = id;
        this.code = code;
        this.name = name;
        this.countryCode = countryCode;
        this.logoUrl = logoUrl;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public RetailerStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}