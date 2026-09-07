package com.djesystems.kawa.wallet.application;

import com.djesystems.kawa.wallet.domain.AuthenticatedRetailer;
import com.djesystems.kawa.wallet.domain.MappingStatus;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCustomerMappingEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCustomerMappingRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
public class RetailerCustomerLinkService {

    private static final Logger log =
        LoggerFactory.getLogger(
            RetailerCustomerLinkService.class
        );

    private final RetailerCustomerMappingRepository
        mappingRepository;


    public RetailerCustomerLinkService(
            RetailerCustomerMappingRepository mappingRepository) {

        this.mappingRepository =
            mappingRepository;
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
         * Si AUCHAN répète exactement la même requête,
         * on considère l'opération comme déjà effectuée.
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


        log.info(
            "Retailer/customer mapping activated: publicKawaId={}, retailerCode={}, retailerCustomerId={}",
            publicKawaId,
            retailerCode,
            retailerCustomerId
        );
    }
}