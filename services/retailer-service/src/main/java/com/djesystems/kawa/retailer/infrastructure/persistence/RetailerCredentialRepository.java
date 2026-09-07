package com.djesystems.kawa.retailer.infrastructure.persistence;

import java.util.Optional;
import com.djesystems.kawa.retailer.domain.RetailerEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerCredentialRepository
        extends JpaRepository<RetailerCredentialEntity, String> {

    Optional<RetailerCredentialEntity>
        findByEntraClientId(String entraClientId);

    Optional<RetailerCredentialEntity>
        findByRetailerIdAndEnvironment(
            String retailerId,
            RetailerEnvironment environment
        );
}