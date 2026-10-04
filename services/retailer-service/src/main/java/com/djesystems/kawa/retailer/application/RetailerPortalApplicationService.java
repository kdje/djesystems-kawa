package com.djesystems.kawa.retailer.application;

import com.djesystems.kawa.retailer.api.PortalBillingResponse;
import com.djesystems.kawa.retailer.api.PortalRetailerResponse;
import com.djesystems.kawa.retailer.api.PortalSubscriptionResponse;
import com.djesystems.kawa.retailer.domain.RetailerUserRole;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerBillingAccountRepository;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerSubscriptionRepository;
import com.djesystems.kawa.retailer.security.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RetailerPortalApplicationService {

    private final RetailerPortalAccessService accessService;
    private final RetailerSubscriptionRepository subscriptionRepository;
    private final RetailerBillingAccountRepository billingAccountRepository;

    public RetailerPortalApplicationService(
            RetailerPortalAccessService accessService,
            RetailerSubscriptionRepository subscriptionRepository,
            RetailerBillingAccountRepository billingAccountRepository) {
        this.accessService = accessService;
        this.subscriptionRepository = subscriptionRepository;
        this.billingAccountRepository = billingAccountRepository;
    }

    public PortalRetailerResponse getCurrentRetailer(AuthenticatedPrincipal principal) {
        var context = accessService.resolve(principal);
        return new PortalRetailerResponse(
            context.retailer().getCode(),
            context.retailer().getName(),
            context.retailer().getCountryCode(),
            context.user().getRole().name()
        );
    }

    public PortalSubscriptionResponse getSubscription(AuthenticatedPrincipal principal) {
        var context = accessService.resolve(principal);
        var subscription = subscriptionRepository.findByRetailerId(context.retailer().getId())
            .orElseThrow(() -> new RetailerPortalException(
                HttpStatus.NOT_FOUND,
                "Subscription is not configured"
            ));
        return new PortalSubscriptionResponse(
            subscription.getPlan(),
            subscription.getStatus().name(),
            subscription.getStartDate(),
            subscription.getRenewalDate(),
            subscription.getBillingCycle()
        );
    }

    public PortalBillingResponse getBilling(AuthenticatedPrincipal principal) {
        var context = accessService.resolve(principal);
        if (context.user().getRole() != RetailerUserRole.RETAILER_ADMIN
                && context.user().getRole() != RetailerUserRole.BILLING_ADMIN) {
            throw new RetailerPortalException(
                HttpStatus.FORBIDDEN,
                "Your retailer role does not allow access to billing information"
            );
        }
        var account = billingAccountRepository.findByRetailerId(context.retailer().getId())
            .orElseThrow(() -> new RetailerPortalException(
                HttpStatus.NOT_FOUND,
                "Billing account is not configured"
            ));
        return new PortalBillingResponse(
            account.getCompanyName(),
            account.getBillingEmail(),
            account.getBillingAddress(),
            account.getExternalProvider(),
            account.getExternalAccountId()
        );
    }
}
