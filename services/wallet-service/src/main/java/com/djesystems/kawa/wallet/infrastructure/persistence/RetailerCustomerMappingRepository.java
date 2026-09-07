package com.djesystems.kawa.wallet.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerCustomerMappingRepository
        extends JpaRepository<RetailerCustomerMappingEntity, Long> {

    Optional<RetailerCustomerMappingEntity>
        findByPublicKawaIdAndRetailerCode(
            String publicKawaId,
            String retailerCode
        );
}