package com.djesystems.kawa.customer.application;

import com.djesystems.kawa.customer.domain.CustomerRetailerRelationStatus;
import com.djesystems.kawa.customer.infrastructure.messaging.dto.CustomerRetailerLinkActivatedEvent;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRetailerRelationEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRetailerRelationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetailerLinkActivatedEventHandler {

    private final CustomerRetailerRelationRepository relationRepository;

    public RetailerLinkActivatedEventHandler(
            CustomerRetailerRelationRepository relationRepository) {

        this.relationRepository = relationRepository;
    }

    @Transactional
    public void handle(
            CustomerRetailerLinkActivatedEvent event) {

        CustomerRetailerRelationEntity relation =
                relationRepository
                        .findByPublicKawaIdAndRetailerCode(
                                event.publicKawaId(),
                                event.retailerCode()
                        )
                        .orElseGet(() ->
                                new CustomerRetailerRelationEntity(
                                        event.publicKawaId(),
                                        event.retailerCode(),
                                        CustomerRetailerRelationStatus.ACTIVE,
                                        event.eventId()
                                )
                        );

        /*
         * Idempotent :
         * recevoir deux fois l'événement ne pose pas de problème.
         */
        relation.updateStatus(
                CustomerRetailerRelationStatus.ACTIVE,
                event.eventId()
        );

        relationRepository.save(relation);
    }
}