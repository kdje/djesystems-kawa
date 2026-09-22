package com.djesystems.kawa.wallet.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.djesystems.kawa.wallet.domain.AuthenticatedRetailer;
import com.djesystems.kawa.wallet.domain.ConsentMode;
import com.djesystems.kawa.wallet.domain.CustomerProjectionStatus;
import com.djesystems.kawa.wallet.domain.MappingStatus;
import com.djesystems.kawa.wallet.dto.WalletCustomerDetails;
import com.djesystems.kawa.wallet.dto.WalletResolveResponse;
import com.djesystems.kawa.wallet.infrastructure.persistence.CustomerProjectionEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.CustomerProjectionRepository;
import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.OutboxEventRepository;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCustomerMappingEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCustomerMappingRepository;

@Service
public class WalletResolutionService {

    private static final String CONSENT_REQUESTED =
            "CUSTOMER_RETAILER_CONSENT_REQUESTED";

    private static final String CONSENT_APPROVED =
            "CUSTOMER_RETAILER_CONSENT_APPROVED";

    private final RetailerCustomerMappingRepository mappingRepository;
    private final CustomerProjectionRepository customerProjectionRepository;
    private final OutboxEventRepository outboxEventRepository;
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

        var existing = mappingRepository.findByPublicKawaIdAndRetailerCode(
                publicKawaId, retailer.retailerCode());

        if (existing.isPresent()) {
            RetailerCustomerMappingEntity mapping = existing.get();

            if (mapping.getStatus() == MappingStatus.PENDING_CONSENT) {
                var projection = customerProjectionRepository.findById(publicKawaId);
                if (projection.isPresent()
                        && projection.get().isAutoRetailerAssociationEnabled()) {
                    return approveExistingPendingMapping(
                            mapping,
                            projection.get()
                    );
                }
            }

            return responseForExisting(mapping);
        }

        var projectionOptional = customerProjectionRepository.findById(publicKawaId);

        if (projectionOptional.isEmpty()) {
            return response(
                    "CUSTOMER_SYNC_PENDING", publicKawaId, retailer.retailerCode(), null, null);
        }

        CustomerProjectionEntity projection = projectionOptional.get();

        if (projection.getStatus() != CustomerProjectionStatus.ACTIVE) {
            return response(
                    "CUSTOMER_NOT_ACTIVE", publicKawaId, retailer.retailerCode(), null, null);
        }

        if (projection.isAutoRetailerAssociationEnabled()) {
            return createAutoApprovedMapping(projection, retailer);
        }

        return createConsentRequiredMapping(projection, retailer);
    }

    private WalletResolveResponse responseForExisting(
            RetailerCustomerMappingEntity mapping) {

        return switch (mapping.getStatus()) {
            case ACTIVE -> response(
                    "LINKED",
                    mapping.getPublicKawaId(),
                    mapping.getRetailerCode(),
                    mapping.getRetailerCustomerId(),
                    null);

            case PENDING_CONSENT -> response(
                    "CONSENT_PENDING",
                    mapping.getPublicKawaId(),
                    mapping.getRetailerCode(),
                    null,
                    null);

            case CONSENT_APPROVED -> {
                CustomerProjectionEntity projection = customerProjectionRepository
                        .findById(mapping.getPublicKawaId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Customer projection not found: " + mapping.getPublicKawaId()));

                yield response(
                        "CONSENT_APPROVED",
                        mapping.getPublicKawaId(),
                        mapping.getRetailerCode(),
                        null,
                        new WalletCustomerDetails(projection.getEmail()));
            }

            case CONSENT_REJECTED -> response(
                    "CONSENT_REJECTED",
                    mapping.getPublicKawaId(),
                    mapping.getRetailerCode(),
                    null,
                    null);

            case REVOKED -> response(
                    "REVOKED",
                    mapping.getPublicKawaId(),
                    mapping.getRetailerCode(),
                    null,
                    null);
        };
    }

    private WalletResolveResponse approveExistingPendingMapping(
            RetailerCustomerMappingEntity mapping,
            CustomerProjectionEntity projection) {

        mapping.approveConsent(ConsentMode.AUTO_ON_QR_PRESENTATION);
        mappingRepository.save(mapping);

        String eventId = UUID.randomUUID().toString();
        CustomerRetailerConsentApprovedEvent event =
                new CustomerRetailerConsentApprovedEvent(
                        eventId,
                        CONSENT_APPROVED,
                        Instant.now(),
                        mapping.getPublicKawaId(),
                        mapping.getRetailerCode(),
                        ConsentMode.AUTO_ON_QR_PRESENTATION.name()
                );

        saveOutbox(
                eventId,
                mapping.getPublicKawaId(),
                CONSENT_APPROVED,
                event
        );

        return response(
                "CONSENT_APPROVED",
                mapping.getPublicKawaId(),
                mapping.getRetailerCode(),
                null,
                new WalletCustomerDetails(projection.getEmail())
        );
    }

    private WalletResolveResponse createAutoApprovedMapping(
            CustomerProjectionEntity projection,
            AuthenticatedRetailer retailer) {

        RetailerCustomerMappingEntity mapping = new RetailerCustomerMappingEntity(
                projection.getPublicKawaId(),
                retailer.retailerCode(),
                null,
                MappingStatus.PENDING_CONSENT
        );

        mapping.approveConsent(ConsentMode.AUTO_ON_QR_PRESENTATION);
        mappingRepository.save(mapping);

        String eventId = UUID.randomUUID().toString();
        CustomerRetailerConsentApprovedEvent event =
                new CustomerRetailerConsentApprovedEvent(
                        eventId,
                        CONSENT_APPROVED,
                        Instant.now(),
                        projection.getPublicKawaId(),
                        retailer.retailerCode(),
                        ConsentMode.AUTO_ON_QR_PRESENTATION.name()
                );

        saveOutbox(
                eventId,
                projection.getPublicKawaId(),
                CONSENT_APPROVED,
                event
        );

        return response(
                "CONSENT_APPROVED",
                projection.getPublicKawaId(),
                retailer.retailerCode(),
                null,
                new WalletCustomerDetails(projection.getEmail())
        );
    }

    private WalletResolveResponse createConsentRequiredMapping(
            CustomerProjectionEntity projection,
            AuthenticatedRetailer retailer) {

        RetailerCustomerMappingEntity mapping = new RetailerCustomerMappingEntity(
                projection.getPublicKawaId(),
                retailer.retailerCode(),
                null,
                MappingStatus.PENDING_CONSENT
        );
        mappingRepository.save(mapping);

        String eventId = UUID.randomUUID().toString();
        CustomerRetailerConsentRequestedEvent event =
                new CustomerRetailerConsentRequestedEvent(
                        eventId,
                        CONSENT_REQUESTED,
                        Instant.now(),
                        projection.getPublicKawaId(),
                        retailer.retailerCode()
                );

        saveOutbox(
                eventId,
                projection.getPublicKawaId(),
                CONSENT_REQUESTED,
                event
        );

        return response(
                "CONSENT_REQUIRED",
                projection.getPublicKawaId(),
                retailer.retailerCode(),
                null,
                null
        );
    }

    private void saveOutbox(
            String eventId,
            String publicKawaId,
            String eventType,
            Object event) {

        outboxEventRepository.save(new OutboxEventEntity(
                eventId,
                "RETAILER_CUSTOMER_MAPPING",
                publicKawaId,
                eventType,
                serializeEvent(event)
        ));
    }

    private WalletResolveResponse response(
            String status,
            String publicKawaId,
            String retailerCode,
            String retailerCustomerId,
            WalletCustomerDetails customer) {
        return new WalletResolveResponse(
                status,
                publicKawaId,
                retailerCode,
                retailerCustomerId,
                customer
        );
    }

    private String serializeEvent(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize outbox event", e);
        }
    }
}
