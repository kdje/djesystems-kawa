package com.djesystems.kawa.wallet.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerProjectionRepository
        extends JpaRepository<CustomerProjectionEntity, String> {


}