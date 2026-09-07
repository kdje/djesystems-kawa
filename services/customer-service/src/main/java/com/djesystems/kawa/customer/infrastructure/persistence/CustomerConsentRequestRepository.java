package com.djesystems.kawa.customer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerConsentRequestRepository
        extends JpaRepository<CustomerConsentRequestEntity, Long> {

    boolean existsByEventId(String eventId);

    Optional<CustomerConsentRequestEntity> findByEventId(String eventId);

    Optional<CustomerConsentRequestEntity> findByEventIdAndPublicKawaId( String eventId, String publicKawaId );
}