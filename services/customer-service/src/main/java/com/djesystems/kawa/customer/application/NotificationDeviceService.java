package com.djesystems.kawa.customer.application;

import com.djesystems.kawa.customer.domain.Customer;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerNotificationDeviceEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerNotificationDeviceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDeviceService {

    private final CustomerService customerService;
    private final CustomerNotificationDeviceRepository repository;

    public NotificationDeviceService(
            CustomerService customerService,
            CustomerNotificationDeviceRepository repository) {

        this.customerService = customerService;
        this.repository = repository;
    }

    @Transactional
    public void register(
            String firebaseUid,
            String fcmToken,
            String platform) {

        Customer customer =
                customerService.getOrCreateCustomer(
                        firebaseUid,
                        null
                );

        String publicKawaId = customer.publicKawaId();

        // Un seul device actif à la fois pour ce client.
        // Les anciens tokens restent en historique mais deviennent inactifs.
        repository.findByPublicKawaIdAndActiveTrue(publicKawaId)
                .forEach(CustomerNotificationDeviceEntity::deactivate);

        // Si le token existe déjà, on le rattache/réactive.
        // Sinon, on crée une nouvelle ligne active.
        repository.findByFcmToken(fcmToken)
                .ifPresentOrElse(

                    device ->
                        device.bindTo(
                            publicKawaId,
                            platform
                        ),

                    () ->
                        repository.save(
                            new CustomerNotificationDeviceEntity(
                                publicKawaId,
                                fcmToken,
                                platform
                            )
                        )
                );
    }
}