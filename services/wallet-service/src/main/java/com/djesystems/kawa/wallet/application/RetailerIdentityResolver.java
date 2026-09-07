package com.djesystems.kawa.wallet.application;

import com.djesystems.kawa.wallet.domain.RetailerProjectionStatus;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCredentialProjectionEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCredentialProjectionRepository;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerProjectionEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerProjectionRepository;
import com.djesystems.kawa.wallet.domain.UnknownRetailerException;

import org.springframework.stereotype.Service;

@Service
public class RetailerIdentityResolver {

    private final RetailerCredentialProjectionRepository credentialRepository;
    private final RetailerProjectionRepository retailerRepository;

    public RetailerIdentityResolver(
            RetailerCredentialProjectionRepository credentialRepository,
            RetailerProjectionRepository retailerRepository) {

        this.credentialRepository = credentialRepository;
        this.retailerRepository = retailerRepository;
    }

    public RetailerProjectionEntity resolveByEntraClientId(
            String entraClientId) {

        /*
         * 1. Retrouver le credential Entra actif.
         */
        RetailerCredentialProjectionEntity credential =
                credentialRepository
                    .findByEntraClientIdAndEnabledTrue(entraClientId)
                    .orElseThrow(() ->
                        new UnknownRetailerException(
                            "Unknown or disabled Entra client: "
                                + entraClientId
                        )
                    );

        /*
         * 2. Retrouver l'enseigne correspondante.
         */
        RetailerProjectionEntity retailer =
        retailerRepository
            .findById(credential.getRetailerId())
            .orElseThrow(() ->
                new UnknownRetailerException(
                    "Retailer projection not found: "
                        + credential.getRetailerId()
                )
            );

        if (retailer.getStatus()
                != RetailerProjectionStatus.ACTIVE) {

            throw new UnknownRetailerException(
                "Retailer is not active: "
                    + retailer.getRetailerCode()
            );
        }


        /*
         * On vérifiera ici également son statut ACTIVE.
         */
        return retailer;
    }
}