package com.djesystems.kawa.retailer.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerUserRepository extends JpaRepository<RetailerUserEntity, String> {

    List<RetailerUserEntity> findAllByIdentityProviderAndIdentityTenantIdAndIdentitySubject(
        String identityProvider,
        String identityTenantId,
        String identitySubject
    );
}
