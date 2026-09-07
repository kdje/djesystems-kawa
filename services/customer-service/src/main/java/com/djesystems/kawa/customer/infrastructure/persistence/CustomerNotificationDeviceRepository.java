package com.djesystems.kawa.customer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerNotificationDeviceRepository
        extends JpaRepository<CustomerNotificationDeviceEntity, Long> {

    Optional<CustomerNotificationDeviceEntity>
        findByFcmToken(String fcmToken);

    List<CustomerNotificationDeviceEntity>
        findByPublicKawaIdAndActiveTrue(String publicKawaId);
}