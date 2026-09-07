package com.djesystems.kawa.wallet.application;

import com.djesystems.kawa.wallet.domain.RetailerProjectionStatus;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerProjectionEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.RetailerProjectionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetailerProjectionService {

    private static final Logger log =
            LoggerFactory.getLogger(RetailerProjectionService.class);

    private final RetailerProjectionRepository repository;

    public RetailerProjectionService(
            RetailerProjectionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void synchronize(
            String retailerId,
            String retailerCode,
            String name,
            String countryCode,
            RetailerProjectionStatus status) {

        repository.findById(retailerId)
            .ifPresentOrElse(

                existing -> {

                    existing.update(
                        retailerCode,
                        name,
                        countryCode,
                        status
                    );

                    log.info(
                        "Retailer projection updated: retailerId={}, retailerCode={}",
                        retailerId,
                        retailerCode
                    );
                },

                () -> {

                    repository.save(
                        new RetailerProjectionEntity(
                            retailerId,
                            retailerCode,
                            name,
                            countryCode,
                            status
                        )
                    );

                    log.info(
                        "Retailer projection created: retailerId={}, retailerCode={}",
                        retailerId,
                        retailerCode
                    );
                }
            );
    }
}