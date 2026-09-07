package com.djesystems.kawa.customer.application;

import com.djesystems.kawa.customer.domain.ConsentRequestStatus;
import com.djesystems.kawa.customer.infrastructure.messaging.dto.CustomerRetailerConsentRequestedEvent;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerConsentRequestEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerConsentRequestRepository;
import com.djesystems.kawa.customer.infrastructure.persistence.NotificationOutboxEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.NotificationOutboxRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ConsentRequestEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ConsentRequestEventHandler.class);

    private final CustomerConsentRequestRepository consentRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;

    public ConsentRequestEventHandler(
            CustomerConsentRequestRepository consentRepository,
            NotificationOutboxRepository notificationOutboxRepository) {

        this.consentRepository = consentRepository;
        this.notificationOutboxRepository = notificationOutboxRepository;
    }

    @Transactional
    public void handle(CustomerRetailerConsentRequestedEvent event) {

        if (consentRepository.existsByEventId(event.eventId())) {

            log.info(
                    "Consent request already processed: eventId={}",
                    event.eventId()
            );

            return;
        }

        Instant now = Instant.now();

        CustomerConsentRequestEntity consent =
                new CustomerConsentRequestEntity(
                        event.eventId(),
                        event.publicKawaId(),
                        event.retailerCode(),
                        ConsentRequestStatus.PENDING,
                        event.occurredAt() != null
                                ? event.occurredAt()
                                : now,
                        now
                );

        consentRepository.save(consent);

        String payload = """
                {
                  "eventId": "%s",
                  "retailerCode": "%s",
                  "type": "RETAILER_CONSENT_REQUEST"
                }
                """.formatted(
                        event.eventId(),
                        event.retailerCode()
                );

        NotificationOutboxEntity notification =
                new NotificationOutboxEntity(
                        event.eventId(),
                        event.publicKawaId(),
                        "RETAILER_CONSENT_REQUEST",
                        "Demande de consentement",
                        event.retailerCode()
                                + " souhaite accéder aux informations nécessaires à votre programme de fidélité.",
                        payload
                );

        notificationOutboxRepository.save(notification);

        log.info(
                "Consent request and notification persisted: eventId={}, publicKawaId={}, retailerCode={}",
                event.eventId(),
                event.publicKawaId(),
                event.retailerCode()
        );
    }
}