package com.djesystems.kawa.wallet.infrastructure.persistence;

import java.util.Optional;

import com.djesystems.kawa.wallet.domain.RetailerEnvironment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerCredentialProjectionRepository
        extends JpaRepository<RetailerCredentialProjectionEntity, Long> {

    Optional<RetailerCredentialProjectionEntity>
        findByRetailerIdAndEnvironment(
            String retailerId,
            RetailerEnvironment environment
        );

    Optional<RetailerCredentialProjectionEntity>
        findByEntraClientId(String entraClientId);

    Optional<RetailerCredentialProjectionEntity>
        findByEntraClientIdAndEnabledTrue(String entraClientId);
}