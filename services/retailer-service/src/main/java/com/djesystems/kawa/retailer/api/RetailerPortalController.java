package com.djesystems.kawa.retailer.api;

import com.djesystems.kawa.retailer.application.RetailerPortalApplicationService;
import com.djesystems.kawa.retailer.security.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/retailer-portal")
public class RetailerPortalController {

    private final RetailerPortalApplicationService portalService;

    public RetailerPortalController(RetailerPortalApplicationService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/me")
    public PortalRetailerResponse getCurrentRetailer(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return portalService.getCurrentRetailer(principal);
    }

    @GetMapping("/me/subscription")
    public PortalSubscriptionResponse getSubscription(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return portalService.getSubscription(principal);
    }

    @GetMapping("/me/billing")
    public PortalBillingResponse getBilling(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return portalService.getBilling(principal);
    }
}
