package com.djesystems.kawa.wallet.application;

import com.djesystems.kawa.wallet.domain.CustomerProjectionStatus;
import com.djesystems.kawa.wallet.infrastructure.persistence.CustomerProjectionEntity;
import com.djesystems.kawa.wallet.infrastructure.persistence.CustomerProjectionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerProjectionService {

    private static final Logger log =
            LoggerFactory.getLogger(CustomerProjectionService.class);

    private final CustomerProjectionRepository repository;

    public CustomerProjectionService(
            CustomerProjectionRepository repository) {

        this.repository = repository;
    }

    @Transactional
    public void synchronize(
            String publicKawaId,
            CustomerProjectionStatus status,
            String email) {

        repository.findById(publicKawaId)
                .ifPresentOrElse(

                    existing -> {

                        existing.setStatus(status);

                        /*
                         * Pas besoin de repository.save(existing).
                         *
                         * L'entity est managée par Hibernate dans
                         * la transaction : le changement sera
                         * automatiquement persisté.
                         */
                        log.info(
                            "Customer projection updated: publicKawaId={}, status={}",
                            publicKawaId,
                            status
                        );
                    },

                    () -> {

                        CustomerProjectionEntity entity =
                                new CustomerProjectionEntity(
                                    publicKawaId,
                                    status,
                                    email
                                );

                        repository.save(entity);

                        log.info(
                            "Customer projection created: publicKawaId={}, status={}",
                            publicKawaId,
                            status
                        );
                    }
                );
    }
}