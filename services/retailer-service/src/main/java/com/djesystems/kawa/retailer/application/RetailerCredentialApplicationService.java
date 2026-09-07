package com.djesystems.kawa.retailer.application;

import java.time.Instant;
import java.util.UUID;

import com.djesystems.kawa.retailer.api.CreateRetailerCredentialRequest;
import com.djesystems.kawa.retailer.api.RetailerCredentialResponse;

import com.djesystems.kawa.retailer.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.OutboxEventRepository;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerCredentialEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerCredentialRepository;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RetailerCredentialApplicationService {

    private final RetailerRepository retailerRepository;
    private final RetailerCredentialRepository credentialRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public RetailerCredentialApplicationService(
            RetailerRepository retailerRepository,
            RetailerCredentialRepository credentialRepository,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper) {

        this.retailerRepository = retailerRepository;
        this.credentialRepository = credentialRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RetailerCredentialResponse createCredential(
            String retailerId,
            CreateRetailerCredentialRequest request) {

        /*
         * 1. Le retailer doit exister.
         */
        RetailerEntity retailer =
                retailerRepository.findById(retailerId)
                    .orElseThrow(() ->
                        new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Retailer not found: " + retailerId
                        )
                    );

        /*
         * 2. Validation minimale de la requête.
         */
        if (request.environment() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "environment is required"
            );
        }

        if (request.entraClientId() == null
                || request.entraClientId().isBlank()) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "entraClientId is required"
            );
        }

        String entraClientId =
                request.entraClientId().trim();

        /*
         * 3. Un retailer ne doit avoir qu'un credential
         *    par environnement.
         */
        credentialRepository
            .findByRetailerIdAndEnvironment(
                retailerId,
                request.environment()
            )
            .ifPresent(existing -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Credential already exists for retailer "
                        + retailer.getCode()
                        + " and environment "
                        + request.environment()
                );
            });

        /*
         * 4. Un clientId Entra ne doit pas appartenir
         *    à deux retailers.
         */
        credentialRepository
            .findByEntraClientId(entraClientId)
            .ifPresent(existing -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Entra client ID already registered"
                );
            });

        /*
         * 5. Création du credential.
         */
        String credentialId =
                UUID.randomUUID().toString();

        RetailerCredentialEntity credential =
                new RetailerCredentialEntity(
                    credentialId,
                    retailerId,
                    request.environment(),
                    entraClientId,
                    true
                );

        credentialRepository.save(credential);

        /*
         * 6. Création de l'événement Outbox.
         */
        String eventId =
                UUID.randomUUID().toString();

        RetailerCredentialCreatedEvent event =
                new RetailerCredentialCreatedEvent(
                    eventId,
                    "RETAILER_CREDENTIAL_CREATED",
                    Instant.now(),
                    retailer.getId(),
                    retailer.getCode(),
                    request.environment(),
                    entraClientId,
                    true
                );

        String payload;

        try {

            payload =
                    objectMapper.writeValueAsString(event);

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                "Unable to serialize RETAILER_CREDENTIAL_CREATED event",
                e
            );
        }

        /*
         * L'aggregate reste RETAILER :
         * le credential appartient au retailer.
         */
        OutboxEventEntity outboxEvent =
                new OutboxEventEntity(
                    eventId,
                    "RETAILER",
                    retailerId,
                    "RETAILER_CREDENTIAL_CREATED",
                    payload
                );

        outboxRepository.save(outboxEvent);

        /*
         * Transaction unique :
         *
         * retailer_credential
         * +
         * outbox_event
         *
         * sont commités ensemble.
         */

        return new RetailerCredentialResponse(
            credential.getId(),
            credential.getRetailerId(),
            credential.getEnvironment(),
            credential.getEntraClientId(),
            credential.isEnabled()
        );
    }
}