package com.djesystems.kawa.wallet.application;

import com.djesystems.kawa.wallet.domain.RetailerProjectionStatus;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCredentialProjectionEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCredentialProjectionRepository;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerProjectionEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerProjectionRepository;
import com.djesystems.kawa.wallet.domain.AuthenticatedRetailer;
import com.djesystems.kawa.wallet.security.EntraClientIdExtractor;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class RetailerIdentityService {

    private final EntraClientIdExtractor clientIdExtractor;
    private final RetailerCredentialProjectionRepository credentialRepository;
    private final RetailerProjectionRepository retailerRepository;

    public RetailerIdentityService(
            EntraClientIdExtractor clientIdExtractor,
            RetailerCredentialProjectionRepository credentialRepository,
            RetailerProjectionRepository retailerRepository) {

        this.clientIdExtractor = clientIdExtractor;
        this.credentialRepository = credentialRepository;
        this.retailerRepository = retailerRepository;
    }

    public AuthenticatedRetailer resolve(Jwt jwt) {

        /*
         * 1. Extraire le client ID Entra depuis le JWT.
         */
        String entraClientId =
                clientIdExtractor.extract(jwt);

        /*
         * 2. Identifier le credential actif.
         */
        RetailerCredentialProjectionEntity credential =
                credentialRepository
                    .findByEntraClientIdAndEnabledTrue(
                        entraClientId
                    )
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "Unknown or disabled retailer client: "
                                    + entraClientId
                        )
                    );

        /*
         * 3. Retrouver le retailer métier.
         */
        RetailerProjectionEntity retailer =
                retailerRepository
                    .findById(
                        credential.getRetailerId()
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "Retailer projection not found: "
                                    + credential.getRetailerId()
                        )
                    );

        /*
         * 4. Le retailer doit être actif.
         */
        if (retailer.getStatus()
                != RetailerProjectionStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Retailer is not active: "
                            + retailer.getRetailerCode()
            );
        }

        /*
         * 5. Construire l'identité métier de l'appelant.
         */
        return new AuthenticatedRetailer(
                retailer.getRetailerId(),
                retailer.getRetailerCode()
        );
    }
}