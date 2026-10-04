package com.djesystems.kawa.retailer.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerBillingAccountRepository
        extends JpaRepository<RetailerBillingAccountEntity, String> {
    Optional<RetailerBillingAccountEntity> findByRetailerId(String retailerId);
}
