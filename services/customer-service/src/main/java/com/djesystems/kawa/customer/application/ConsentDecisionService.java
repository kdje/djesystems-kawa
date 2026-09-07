package com.djesystems.kawa.customer.application;

import com.djesystems.kawa.customer.domain.ConsentDecision;
import com.djesystems.kawa.customer.domain.ConsentRequestStatus;
import com.djesystems.kawa.customer.domain.Customer;
import com.djesystems.kawa.customer.domain.event.CustomerRetailerConsentDecidedEvent;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerConsentRequestEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerConsentRequestRepository;
import com.djesystems.kawa.customer.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.OutboxEventRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class ConsentDecisionService {

    private static final String EVENT_TYPE =
            "CUSTOMER_RETAILER_CONSENT_DECIDED";

    private static final String AGGREGATE_TYPE =
            "CUSTOMER_CONSENT";

    private final CustomerService customerService;
    private final CustomerConsentRequestRepository consentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ConsentDecisionService(
            CustomerService customerService,
            CustomerConsentRequestRepository consentRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.customerService = customerService;
        this.consentRepository = consentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void decide(
            String firebaseUid,
            String consentRequestEventId,
            ConsentDecision decision) {

        /*
         * On déduit le publicKawaId depuis le user Firebase.
         * Le frontend ne choisit jamais lui-même le customer.
         */
        Customer customer =
                customerService.getCustomer(firebaseUid);

        String publicKawaId =
                customer.publicKawaId();

        /*
         * On cherche simultanément par eventId ET publicKawaId.
         *
         * Impossible ainsi de décider une demande appartenant
         * à un autre customer.
         */
        CustomerConsentRequestEntity consent =
                consentRepository
                        .findByEventIdAndPublicKawaId(
                                consentRequestEventId,
                                publicKawaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Consent request not found"
                                )
                        );

        ConsentRequestStatus targetStatus =
                switch (decision) {

                    case APPROVED ->
                            ConsentRequestStatus.APPROVED;

                    case REJECTED ->
                            ConsentRequestStatus.REJECTED;
                };

        /*
         * Idempotence :
         *
         * si le même choix a déjà été enregistré,
         * on répond OK sans créer un deuxième événement.
         */
        if (consent.getStatus() == targetStatus) {
            return;
        }

        /*
         * En revanche :
         *
         * APPROVED puis REJECTED
         * ou
         * REJECTED puis APPROVED
         *
         * n'est pas autorisé ici.
         */
        if (consent.getStatus()
                != ConsentRequestStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Consent request already decided: "
                            + consent.getStatus()
            );
        }

        Instant now = Instant.now();

        /*
         * 1. Modification de la demande.
         */
        consent.decide(
                targetStatus,
                now
        );

        consentRepository.save(consent);

        /*
         * 2. Création du nouvel événement.
         */
        String eventId =
                UUID.randomUUID().toString();

        CustomerRetailerConsentDecidedEvent event =
                new CustomerRetailerConsentDecidedEvent(
                        eventId,
                        EVENT_TYPE,
                        consentRequestEventId,
                        now,
                        publicKawaId,
                        consent.getRetailerCode(),
                        decision.name()
                );

        String payload =
                serialize(event);

        /*
         * 3. Transactional Outbox.
         *
         * La décision et l'événement sont enregistrés
         * dans la même transaction MySQL.
         */
        OutboxEventEntity outbox =
                new OutboxEventEntity(
                        eventId,
                        AGGREGATE_TYPE,
                        publicKawaId,
                        EVENT_TYPE,
                        payload
                );

        outboxEventRepository.save(outbox);
    }

    private String serialize(
            CustomerRetailerConsentDecidedEvent event) {

        try {

            return objectMapper
                    .writeValueAsString(event);

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(
                    "Unable to serialize "
                            + EVENT_TYPE,
                    ex
            );
        }
    }
}