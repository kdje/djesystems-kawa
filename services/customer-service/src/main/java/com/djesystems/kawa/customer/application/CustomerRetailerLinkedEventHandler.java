package com.djesystems.kawa.customer.application;

import com.djesystems.kawa.customer.domain.CustomerRetailerRelationStatus;
import com.djesystems.kawa.customer.infrastructure.messaging.dto.CustomerRetailerLinkedEvent;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRetailerRelationEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRetailerRelationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerRetailerLinkedEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    CustomerRetailerLinkedEventHandler.class
            );

    private final CustomerRetailerRelationRepository
            relationRepository;

    public CustomerRetailerLinkedEventHandler(
            CustomerRetailerRelationRepository relationRepository) {

        this.relationRepository =
                relationRepository;
    }

    @Transactional
    public void handle(
            CustomerRetailerLinkedEvent event) {

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
         * La vérité métier vient du Wallet :
         * le mapping est maintenant ACTIVE.
         */
        relation.updateStatus(
                CustomerRetailerRelationStatus.ACTIVE,
                event.eventId()
        );

        relationRepository.save(relation);

        log.info(
                "Customer/retailer relation activated: eventId={}, publicKawaId={}, retailerCode={}, retailerCustomerId={}",
                event.eventId(),
                event.publicKawaId(),
                event.retailerCode(),
                event.retailerCustomerId()
        );
    }
}