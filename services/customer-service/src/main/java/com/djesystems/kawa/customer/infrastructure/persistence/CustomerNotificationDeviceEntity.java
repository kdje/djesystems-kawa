package com.djesystems.kawa.customer.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "customer_notification_device",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_customer_notification_device_token",
            columnNames = "fcm_token"
        )
    }
)
public class CustomerNotificationDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_kawa_id", nullable = false, length = 32)
    private String publicKawaId;

    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;

    @Column(name = "platform", nullable = false, length = 20)
    private String platform;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerNotificationDeviceEntity() {
    }

    public CustomerNotificationDeviceEntity(
            String publicKawaId,
            String fcmToken,
            String platform) {

        Instant now = Instant.now();

        this.publicKawaId = publicKawaId;
        this.fcmToken = fcmToken;
        this.platform = platform;
        this.active = true;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void bindTo(
            String publicKawaId,
            String platform) {

        this.publicKawaId = publicKawaId;
        this.platform = platform;
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getPublicKawaId() {
        return publicKawaId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getPlatform() {
        return platform;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }
}