package com.djesystems.kawa.wallet.application;

import com.djesystems.kawa.wallet.domain.MappingStatus;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCustomerMappingEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCustomerMappingRepository;
import com.djesystems.kawa.wallet.messaging.customer.CustomerRetailerConsentDecidedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CustomerRetailerConsentDecisionService {

    private static final Logger log =
        LoggerFactory.getLogger(
            CustomerRetailerConsentDecisionService.class
        );

    private final RetailerCustomerMappingRepository
        mappingRepository;


    public CustomerRetailerConsentDecisionService(
            RetailerCustomerMappingRepository mappingRepository) {

        this.mappingRepository = mappingRepository;
    }


    @Transactional
    public void handle(
            CustomerRetailerConsentDecidedEvent event) {

        RetailerCustomerMappingEntity mapping =
            mappingRepository
                .findByPublicKawaIdAndRetailerCode(
                    event.publicKawaId(),
                    event.retailerCode()
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Retailer/customer mapping not found: "
                        + event.publicKawaId()
                        + " / "
                        + event.retailerCode()
                    )
                );


        switch (event.decision()) {

            case "APPROVED" ->
                handleApproved(
                    mapping,
                    event
                );

            case "REJECTED" ->
                handleRejected(
                    mapping,
                    event
                );

            default ->
                throw new IllegalArgumentException(
                    "Unsupported consent decision: "
                    + event.decision()
                );
        }
    }


    private void handleApproved(
            RetailerCustomerMappingEntity mapping,
            CustomerRetailerConsentDecidedEvent event) {

        /*
         * Idempotence.
         */
        if (mapping.getStatus()
                == MappingStatus.CONSENT_APPROVED) {

            log.info(
                "Consent already approved: eventId={}, publicKawaId={}, retailerCode={}",
                event.eventId(),
                event.publicKawaId(),
                event.retailerCode()
            );

            return;
        }


        /*
         * Si le mapping est déjà ACTIVE,
         * il n'y a plus rien à faire.
         */
        if (mapping.getStatus()
                == MappingStatus.ACTIVE) {

            log.info(
                "Mapping already active: publicKawaId={}, retailerCode={}",
                event.publicKawaId(),
                event.retailerCode()
            );

            return;
        }


        mapping.approveConsent();

        mappingRepository.save(mapping);


        log.info(
            "Customer consent approved: eventId={}, consentRequestEventId={}, publicKawaId={}, retailerCode={}",
            event.eventId(),
            event.consentRequestEventId(),
            event.publicKawaId(),
            event.retailerCode()
        );


        /*
         * PROCHAINE ÉTAPE :
         *
         * création ici de l'événement destiné
         * au retailer.
         */
    }


    private void handleRejected(
            RetailerCustomerMappingEntity mapping,
            CustomerRetailerConsentDecidedEvent event) {

        /*
         * Idempotence.
         */
        if (mapping.getStatus()
                == MappingStatus.CONSENT_REJECTED) {

            log.info(
                "Consent already rejected: eventId={}, publicKawaId={}, retailerCode={}",
                event.eventId(),
                event.publicKawaId(),
                event.retailerCode()
            );

            return;
        }


        mapping.rejectConsent();

        mappingRepository.save(mapping);


        log.info(
            "Customer consent rejected: eventId={}, consentRequestEventId={}, publicKawaId={}, retailerCode={}",
            event.eventId(),
            event.consentRequestEventId(),
            event.publicKawaId(),
            event.retailerCode()
        );


        /*
         * IMPORTANT :
         *
         * REFUS = aucune publication vers Retailer.
         *
         * Le workflow s'arrête ici.
         */
    }
}