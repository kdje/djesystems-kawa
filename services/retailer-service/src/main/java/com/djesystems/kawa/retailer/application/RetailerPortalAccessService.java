package com.djesystems.kawa.retailer.application;

import java.util.List;

import com.djesystems.kawa.retailer.domain.RetailerStatus;
import com.djesystems.kawa.retailer.domain.RetailerUserStatus;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerRepository;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerUserEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerUserRepository;
import com.djesystems.kawa.retailer.security.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RetailerPortalAccessService {

    private final RetailerUserRepository retailerUserRepository;
    private final RetailerRepository retailerRepository;

    public RetailerPortalAccessService(
            RetailerUserRepository retailerUserRepository,
            RetailerRepository retailerRepository) {
        this.retailerUserRepository = retailerUserRepository;
        this.retailerRepository = retailerRepository;
    }

    public RetailerPortalContext resolve(AuthenticatedPrincipal principal) {
        if (principal == null
                || principal.subject() == null
                || principal.identityProvider() == null
                || principal.identityTenantId() == null) {
            throw new RetailerPortalException(
                HttpStatus.FORBIDDEN,
                "No active retailer membership is associated with this identity"
            );
        }

        List<RetailerUserEntity> memberships =
            retailerUserRepository.findAllByIdentityProviderAndIdentityTenantIdAndIdentitySubject(
                principal.identityProvider(),
                principal.identityTenantId(),
                principal.subject()
            );
        if (memberships.size() != 1) {
            throw new RetailerPortalException(
                HttpStatus.FORBIDDEN,
                memberships.isEmpty()
                    ? "No active retailer membership is associated with this identity"
                    : "This identity has multiple retailer memberships; contact support"
            );
        }

        RetailerUserEntity user = memberships.getFirst();
        if (user.getStatus() != RetailerUserStatus.ACTIVE) {
            throw new RetailerPortalException(
                HttpStatus.FORBIDDEN,
                "No active retailer membership is associated with this identity"
            );
        }

        RetailerEntity retailer = retailerRepository.findById(user.getRetailerId())
            .filter(candidate -> candidate.getStatus() == RetailerStatus.ACTIVE)
            .orElseThrow(() -> new RetailerPortalException(
                HttpStatus.FORBIDDEN,
                "No active retailer membership is associated with this identity"
            ));
        return new RetailerPortalContext(user, retailer);
    }

    public record RetailerPortalContext(
            RetailerUserEntity user,
            RetailerEntity retailer) {
    }
}
