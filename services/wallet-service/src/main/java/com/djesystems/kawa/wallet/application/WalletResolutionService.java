package com.djesystems.kawa.wallet.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.djesystems.kawa.wallet.domain.AuthenticatedRetailer;
import com.djesystems.kawa.wallet.domain.CustomerProjectionStatus;
import com.djesystems.kawa.wallet.domain.MappingStatus;
import com.djesystems.kawa.wallet.dto.WalletCustomerDetails;
import com.djesystems.kawa.wallet.dto.WalletResolveResponse;
import com.djesystems.kawa.wallet.infrastructure.persistence.CustomerProjectionRepository;
import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventRepository;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCustomerMappingEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCustomerMappingRepository;


@Service
public class WalletResolutionService {

    private final RetailerCustomerMappingRepository mappingRepository;
    private final CustomerProjectionRepository customerProjectionRepository;

    /*
     * Repository de la table OUTBOX.
     *
     * IMPORTANT :
     * Le mapping PENDING_CONSENT et l'événement Outbox seront enregistrés
     * dans la même transaction.
     */
    private final OutboxEventRepository outboxEventRepository;

    /*
     * Utilisé pour sérialiser le contenu de l'événement en JSON.
     */
    private final ObjectMapper objectMapper;


    public WalletResolutionService(
            RetailerCustomerMappingRepository mappingRepository,
            CustomerProjectionRepository customerProjectionRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.mappingRepository = mappingRepository;
        this.customerProjectionRepository = customerProjectionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }


    @Transactional
    public WalletResolveResponse resolve(
            String publicKawaId,
            AuthenticatedRetailer retailer) {

        /*
         * ============================================================
         * 1. Recherche d'une relation existante
         * ============================================================
         *
         * On vérifie si le customer KAWA est déjà connu
         * pour ce retailer.
         */
        var existing =
            mappingRepository.findByPublicKawaIdAndRetailerCode(
                publicKawaId,
                retailer.retailerCode()
            );


        if (existing.isPresent()) {

            RetailerCustomerMappingEntity mapping = existing.get();

            /*
             * --------------------------------------------------------
             * Customer déjà lié au retailer
             * --------------------------------------------------------
             */
            if (mapping.getStatus() == MappingStatus.ACTIVE) {

                return new WalletResolveResponse(
                    "LINKED",
                    mapping.getPublicKawaId(),
                    mapping.getRetailerCode(),
                    mapping.getRetailerCustomerId(),
                    null
                );
            }


            /*
             * --------------------------------------------------------
             * Consentement déjà demandé
             * --------------------------------------------------------
             *
             * IMPORTANT :
             *
             * On NE recrée PAS d'événement Outbox.
             *
             * Donc :
             *
             * Premier scan :
             *     PENDING_CONSENT créé
             *     + événement Outbox créé
             *
             * Deuxième scan :
             *     PENDING_CONSENT déjà présent
             *     => aucun nouvel événement
             */
            if (mapping.getStatus() == MappingStatus.PENDING_CONSENT) {

                return new WalletResolveResponse(
                    "CONSENT_PENDING",
                    mapping.getPublicKawaId(),
                    mapping.getRetailerCode(),
                    null,
                    null
                );
            }

            if (mapping.getStatus()
                    == MappingStatus.CONSENT_APPROVED) {

                var projection =
                    customerProjectionRepository
                        .findById(mapping.getPublicKawaId())
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "Customer projection not found: "
                                    + mapping.getPublicKawaId()
                            )
                        );

                return new WalletResolveResponse(
                    "CONSENT_APPROVED",
                    mapping.getPublicKawaId(),
                    mapping.getRetailerCode(),
                    null,
                    new WalletCustomerDetails(
                        projection.getEmail()
                    )
                );
            }


            if (mapping.getStatus()
                    == MappingStatus.CONSENT_REJECTED) {

                return new WalletResolveResponse(
                    "CONSENT_REJECTED",
                    mapping.getPublicKawaId(),
                    mapping.getRetailerCode(),
                    null,
                    null
                );
            }

        }


        /*
         * ============================================================
         * 2. Vérification du customer dans la projection Wallet
         * ============================================================
         */

        var customerProjection =
            customerProjectionRepository.findById(publicKawaId);


        /*
         * Le Wallet ne connaît pas encore ce customer.
         *
         * Par exemple, l'événement CUSTOMER_CREATED provenant
         * du Customer Service n'a peut-être pas encore été consommé.
         */
        if (customerProjection.isEmpty()) {

            return new WalletResolveResponse(
                "CUSTOMER_SYNC_PENDING",
                publicKawaId,
                retailer.retailerCode(),
                null,
                null
            );
        }


        /*
         * Le customer existe mais n'est pas actif.
         */
        if (customerProjection.get().getStatus()
                != CustomerProjectionStatus.ACTIVE) {

            return new WalletResolveResponse(
                "CUSTOMER_NOT_ACTIVE",
                publicKawaId,
                retailer.retailerCode(),
                null,
                null
            );
        }


        /*
         * ============================================================
         * 3. Création du mapping PENDING_CONSENT
         * ============================================================
         *
         * Le customer existe dans KAWA mais aucune relation
         * avec ce retailer n'existe encore.
         */

        RetailerCustomerMappingEntity mapping =
            new RetailerCustomerMappingEntity(
                publicKawaId,
                retailer.retailerCode(),
                null,
                MappingStatus.PENDING_CONSENT
            );


        /*
         * IMPORTANT :
         *
         * Cette écriture et l'écriture dans la table Outbox
         * sont réalisées dans la même transaction.
         */
        mappingRepository.save(mapping);


        /*
         * ============================================================
         * 4. Création de l'événement métier
         * ============================================================
         *
         * Cet événement signifie :
         *
         * "Le retailer X souhaite établir une relation avec
         * le customer Y. Le consentement du customer est nécessaire."
         */

        String eventId = UUID.randomUUID().toString();

        CustomerRetailerConsentRequestedEvent event =
            new CustomerRetailerConsentRequestedEvent(
                eventId,
                "CUSTOMER_RETAILER_CONSENT_REQUESTED",
                Instant.now(),
                publicKawaId,
                retailer.retailerCode()
            );


        /*
         * ============================================================
         * 5. Sérialisation de l'événement
         * ============================================================
         */

        String payload = serializeEvent(event);


        /*
         * ============================================================
         * 6. Transactional Outbox
         * ============================================================
         *
         * L'événement n'est PAS envoyé directement vers le bus ici.
         *
         * Il est enregistré dans la table OUTBOX.
         *
         * Un autre composant sera chargé de publier les événements
         * Outbox vers Azure Service Bus / Kafka / RabbitMQ, etc.
         */
        OutboxEventEntity outboxEvent =
            new OutboxEventEntity(
                eventId,
                "RETAILER_CUSTOMER_MAPPING",
                publicKawaId,
                "CUSTOMER_RETAILER_CONSENT_REQUESTED",
                payload
            );

        outboxEventRepository.save(outboxEvent);


        /*
         * ============================================================
         * 7. Réponse au retailer
         * ============================================================
         *
         * Le retailer sait maintenant que le customer existe
         * mais que son consentement est nécessaire.
         */
        return new WalletResolveResponse(
            "CONSENT_REQUIRED",
            publicKawaId,
            retailer.retailerCode(),
            null,
            null
        );
    }


    /**
     * Sérialise un événement métier en JSON.
     *
     * En cas d'erreur de sérialisation, une RuntimeException est levée.
     * Comme resolve() est @Transactional, toute la transaction sera
     * rollbackée :
     *
     * - pas de mapping PENDING_CONSENT
     * - pas d'événement Outbox
     *
     * Cela garantit la cohérence.
     */
    private String serializeEvent(Object event) {

        try {

            return objectMapper.writeValueAsString(event);

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                "Unable to serialize outbox event",
                e
            );
        }
    }
}