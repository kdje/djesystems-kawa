package com.djesystems.kawa.customer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Accès aux clients stockés dans MySQL.
 */
public interface CustomerRepository
        extends JpaRepository<CustomerEntity, UUID> {

    Optional<CustomerEntity> findByFirebaseUid(String firebaseUid);

    Optional<CustomerEntity> findByPublicKawaId(String publicKawaId);

    boolean existsByPublicKawaId(String publicKawaId);
}
