package com.djesystems.kawa.customer.application;

import com.djesystems.kawa.customer.domain.CustomerRetailerRelationStatus;
import com.djesystems.kawa.customer.infrastructure.messaging.dto.CustomerRetailerConsentApprovedEvent;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRetailerRelationEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRetailerRelationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerRetailerConsentApprovedEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(CustomerRetailerConsentApprovedEventHandler.class);

    private final CustomerRetailerRelationRepository relationRepository;

    public CustomerRetailerConsentApprovedEventHandler(
            CustomerRetailerRelationRepository relationRepository) {
        this.relationRepository = relationRepository;
    }

    @Transactional
    public void handle(CustomerRetailerConsentApprovedEvent event) {
        CustomerRetailerRelationEntity relation = relationRepository
                .findByPublicKawaIdAndRetailerCode(
                        event.publicKawaId(),
                        event.retailerCode()
                )
                .orElseGet(() -> new CustomerRetailerRelationEntity(
                        event.publicKawaId(),
                        event.retailerCode(),
                        CustomerRetailerRelationStatus.APPROVED,
                        event.eventId()
                ));

        relation.updateStatus(
                CustomerRetailerRelationStatus.APPROVED,
                event.eventId()
        );

        relationRepository.save(relation);

        log.info(
                "Automatic retailer consent projected: eventId={}, publicKawaId={}, retailerCode={}, consentMode={}",
                event.eventId(),
                event.publicKawaId(),
                event.retailerCode(),
                event.consentMode()
        );
    }
}
