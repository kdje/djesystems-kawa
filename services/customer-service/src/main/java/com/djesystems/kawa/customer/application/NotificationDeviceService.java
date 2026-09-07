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

        repository.findByFcmToken(fcmToken)
                .ifPresentOrElse(

                    device ->
                        device.bindTo(
                            customer.publicKawaId(),
                            platform
                        ),

                    () ->
                        repository.save(
                            new CustomerNotificationDeviceEntity(
                                customer.publicKawaId(),
                                fcmToken,
                                platform
                            )
                        )
                );
    }
}