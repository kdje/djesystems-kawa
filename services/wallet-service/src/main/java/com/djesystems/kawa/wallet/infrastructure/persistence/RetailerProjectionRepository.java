package com.djesystems.kawa.wallet.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerProjectionRepository
        extends JpaRepository<RetailerProjectionEntity, String> {

    Optional<RetailerProjectionEntity>
        findByRetailerCode(String retailerCode);
}