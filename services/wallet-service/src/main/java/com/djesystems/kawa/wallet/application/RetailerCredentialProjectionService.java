package com.djesystems.kawa.wallet.application;

import com.djesystems.kawa.wallet.domain.RetailerEnvironment;

import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCredentialProjectionEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerCredentialProjectionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetailerCredentialProjectionService {

    private static final Logger log =
            LoggerFactory.getLogger(
                RetailerCredentialProjectionService.class
            );

    private final RetailerCredentialProjectionRepository repository;

    public RetailerCredentialProjectionService(
            RetailerCredentialProjectionRepository repository) {

        this.repository = repository;
    }

    @Transactional
    public void synchronize(
            String retailerId,
            String retailerCode,
            RetailerEnvironment environment,
            String entraClientId,
            boolean enabled) {

        repository
            .findByRetailerIdAndEnvironment(
                retailerId,
                environment
            )
            .ifPresentOrElse(

                existing -> {

                    existing.update(
                        retailerCode,
                        entraClientId,
                        enabled
                    );

                    log.info(
                        "Retailer credential projection updated: retailerCode={}, environment={}, entraClientId={}",
                        retailerCode,
                        environment,
                        entraClientId
                    );
                },

                () -> {

                    repository.save(
                        new RetailerCredentialProjectionEntity(
                            retailerId,
                            retailerCode,
                            environment,
                            entraClientId,
                            enabled
                        )
                    );

                    log.info(
                        "Retailer credential projection created: retailerCode={}, environment={}, entraClientId={}",
                        retailerCode,
                        environment,
                        entraClientId
                    );
                }
            );
    }
}