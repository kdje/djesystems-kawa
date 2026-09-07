package com.djesystems.kawa.retailer.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerRepository
        extends JpaRepository<RetailerEntity, String> {

    Optional<RetailerEntity> findByCode(String code);
}