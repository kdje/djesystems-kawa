package com.djesystems.kawa.customer.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.djesystems.kawa.customer.domain.CustomerRetailerRelationStatus;

public interface CustomerRetailerRelationRepository
        extends JpaRepository<CustomerRetailerRelationEntity, Long> {

    Optional<CustomerRetailerRelationEntity>
        findByPublicKawaIdAndRetailerCode(
            String publicKawaId,
            String retailerCode
        );

    List<CustomerRetailerRelationEntity>
        findByPublicKawaIdOrderByUpdatedAtDesc(
            String publicKawaId
        );

    long countByPublicKawaIdAndStatus(
        String publicKawaId,
        CustomerRetailerRelationStatus status
    );
}