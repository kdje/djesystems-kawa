package com.djesystems.kawa.customer.application;

import com.djesystems.kawa.customer.domain.ConsentDecision;
import com.djesystems.kawa.customer.domain.ConsentRequestStatus;
import com.djesystems.kawa.customer.domain.Customer;
import com.djesystems.kawa.customer.domain.CustomerRetailerRelationStatus;
import com.djesystems.kawa.customer.domain.event.CustomerRetailerConsentDecidedEvent;

import com.djesystems.kawa.customer.infrastructure.persistence.CustomerConsentRequestEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerConsentRequestRepository;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRetailerRelationEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRetailerRelationRepository;
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

    private final CustomerRetailerRelationRepository relationRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    public ConsentDecisionService(
            CustomerService customerService,
            CustomerConsentRequestRepository consentRepository,
            CustomerRetailerRelationRepository relationRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.customerService = customerService;
        this.consentRepository = consentRepository;
        this.relationRepository = relationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void decide(
            String firebaseUid,
            String consentRequestEventId,
            ConsentDecision decision) {

        /*
         * 1. On retrouve le customer depuis son identité Firebase.
         */
        Customer customer =
                customerService.getCustomer(firebaseUid);

        String publicKawaId =
                customer.publicKawaId();

        /*
         * 2. On récupère uniquement une demande appartenant
         *    réellement au customer connecté.
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

        /*
         * 3. Etat de la demande de consentement.
         */
        ConsentRequestStatus targetConsentStatus =
                switch (decision) {

                    case APPROVED ->
                            ConsentRequestStatus.APPROVED;

                    case REJECTED ->
                            ConsentRequestStatus.REJECTED;
                };

        /*
         * 4. Etat de la projection utilisée par
         *    la page "Mes enseignes".
         */
        CustomerRetailerRelationStatus targetRelationStatus =
                switch (decision) {

                    case APPROVED ->
                            CustomerRetailerRelationStatus.APPROVED;

                    case REJECTED ->
                            CustomerRetailerRelationStatus.REJECTED;
                };

        /*
         * 5. Idempotence.
         *
         * Si la même décision a déjà été prise,
         * on ne republie pas un nouvel événement.
         *
         * En revanche, on resynchronise quand même
         * customer_retailer_relation au cas où
         * la projection serait restée en PENDING.
         */
        if (consent.getStatus() == targetConsentStatus) {

            updateRelation(
                    publicKawaId,
                    consent.getRetailerCode(),
                    targetRelationStatus,
                    consentRequestEventId
            );

            return;
        }

        /*
         * Une décision déjà prise ne peut pas être inversée ici.
         */
        if (consent.getStatus()
                != ConsentRequestStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Consent request already decided: "
                            + consent.getStatus()
            );
        }

        Instant now =
                Instant.now();

        /*
         * 6. Mise à jour de la demande.
         */
        consent.decide(
                targetConsentStatus,
                now
        );

        consentRepository.save(consent);

        /*
         * L'identifiant du nouvel événement de décision.
         */
        String eventId =
                UUID.randomUUID().toString();

        /*
         * 7. Mise à jour de la projection
         *    customer_retailer_relation.
         *
         * PENDING -> APPROVED
         * ou
         * PENDING -> REJECTED
         */
        updateRelation(
                publicKawaId,
                consent.getRetailerCode(),
                targetRelationStatus,
                eventId
        );

        /*
         * 8. Création de l'événement envoyé au Wallet.
         */
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
         * 9. Transactional Outbox.
         *
         * La décision,
         * la projection Customer
         * et l'événement Outbox
         * sont enregistrés dans la même transaction MySQL.
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

    /**
     * Met à jour la projection utilisée par l'écran
     * "Mes enseignes".
     *
     * Le orElseGet rend le traitement robuste :
     * si la projection n'existe pas encore pour une raison
     * quelconque, elle est recréée.
     */
    private void updateRelation(
            String publicKawaId,
            String retailerCode,
            CustomerRetailerRelationStatus status,
            String sourceEventId) {

        CustomerRetailerRelationEntity relation =
                relationRepository
                        .findByPublicKawaIdAndRetailerCode(
                                publicKawaId,
                                retailerCode
                        )
                        .orElseGet(() ->
                                new CustomerRetailerRelationEntity(
                                        publicKawaId,
                                        retailerCode,
                                        status,
                                        sourceEventId
                                )
                        );

        relation.updateStatus(
                status,
                sourceEventId
        );

        relationRepository.save(relation);
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