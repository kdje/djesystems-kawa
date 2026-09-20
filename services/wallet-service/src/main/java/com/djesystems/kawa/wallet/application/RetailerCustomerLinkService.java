package com.djesystems.kawa.wallet.application;

import com.djesystems.kawa.wallet.domain.AuthenticatedRetailer;
import com.djesystems.kawa.wallet.domain.MappingStatus;
import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventRepository;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCustomerMappingEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCustomerMappingRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;


@Service
public class RetailerCustomerLinkService {

    private static final Logger log =
        LoggerFactory.getLogger(
            RetailerCustomerLinkService.class
        );

    private static final String EVENT_TYPE =
        "CUSTOMER_RETAILER_LINKED";

    private static final String AGGREGATE_TYPE =
        "RETAILER_CUSTOMER_MAPPING";


    private final RetailerCustomerMappingRepository
        mappingRepository;

    private final OutboxEventRepository
        outboxEventRepository;

    private final ObjectMapper
        objectMapper;


    public RetailerCustomerLinkService(
            RetailerCustomerMappingRepository mappingRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.mappingRepository =
            mappingRepository;

        this.outboxEventRepository =
            outboxEventRepository;

        this.objectMapper =
            objectMapper;
    }


    @Transactional
    public void link(
            String publicKawaId,
            String retailerCustomerId,
            AuthenticatedRetailer retailer) {

        /*
         * IMPORTANT :
         *
         * retailerCode vient du JWT Microsoft Entra.
         *
         * Jamais du body envoyé par l'appelant.
         */
        String retailerCode =
            retailer.retailerCode();


        RetailerCustomerMappingEntity mapping =
            mappingRepository
                .findByPublicKawaIdAndRetailerCode(
                    publicKawaId,
                    retailerCode
                )
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Retailer/customer mapping not found"
                    )
                );


        /*
         * Appel idempotent.
         *
         * Si l'association est déjà ACTIVE
         * avec exactement le même retailerCustomerId,
         * il n'y a rien à refaire.
         */
        if (mapping.getStatus()
                == MappingStatus.ACTIVE) {

            if (retailerCustomerId.equals(
                    mapping.getRetailerCustomerId())) {

                log.info(
                    "Retailer/customer mapping already active: publicKawaId={}, retailerCode={}, retailerCustomerId={}",
                    publicKawaId,
                    retailerCode,
                    retailerCustomerId
                );

                return;
            }

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Mapping is already active with another retailer customer ID"
            );
        }


        /*
         * Le retailer ne peut confirmer la liaison
         * qu'après acceptation du consentement.
         */
        if (mapping.getStatus()
                != MappingStatus.CONSENT_APPROVED) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Customer consent is not approved. Current status: "
                    + mapping.getStatus()
            );
        }


        /*
         * CONSENT_APPROVED → ACTIVE
         */
        mapping.activate(
            retailerCustomerId
        );

        mappingRepository.save(
            mapping
        );


        /*
         * Création de l'événement métier.
         *
         * IMPORTANT :
         * l'événement Outbox est persisté dans
         * la même transaction que l'activation.
         *
         * Donc :
         *
         * - soit mapping ACTIVE + événement Outbox
         *   sont tous les deux commités,
         *
         * - soit rien n'est commité.
         */
        String eventId =
            UUID.randomUUID().toString();

        CustomerRetailerLinkedEvent event =
            new CustomerRetailerLinkedEvent(
                eventId,
                EVENT_TYPE,
                Instant.now(),
                publicKawaId,
                retailerCode,
                retailerCustomerId,
                MappingStatus.ACTIVE.name()
            );


        String payload =
            serializeEvent(
                event
            );


        OutboxEventEntity outboxEvent =
            new OutboxEventEntity(
                eventId,
                AGGREGATE_TYPE,
                publicKawaId,
                EVENT_TYPE,
                payload
            );


        outboxEventRepository.save(
            outboxEvent
        );


        log.info(
            "Retailer/customer mapping activated and event created: " +
            "eventId={}, publicKawaId={}, retailerCode={}, retailerCustomerId={}",
            eventId,
            publicKawaId,
            retailerCode,
            retailerCustomerId
        );
    }


    private String serializeEvent(
            CustomerRetailerLinkedEvent event) {

        try {

            return objectMapper
                .writeValueAsString(
                    event
                );

        } catch (JsonProcessingException e) {

            /*
             * On provoque volontairement l'échec
             * de la transaction.
             *
             * On ne veut surtout pas avoir :
             *
             * mapping = ACTIVE
             * mais aucun événement Outbox.
             */
            throw new IllegalStateException(
                "Unable to serialize "
                    + EVENT_TYPE
                    + " event",
                e
            );
        }
    }


    private record CustomerRetailerLinkedEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String publicKawaId,
        String retailerCode,
        String retailerCustomerId,
        String status
    ) {
    }
}