package com.djesystems.kawa.customer.api;

import com.djesystems.kawa.customer.api.dto.ConsentDecisionRequest;
import com.djesystems.kawa.customer.api.dto.CustomerResponse;
import com.djesystems.kawa.customer.api.dto.CustomerRetailersResponse;
import com.djesystems.kawa.customer.api.dto.KawaIdResponse;
import com.djesystems.kawa.customer.api.dto.RegisterNotificationDeviceRequest;
import com.djesystems.kawa.customer.api.dto.UpdateCustomerPreferencesRequest;
import com.djesystems.kawa.customer.application.ConsentDecisionService;
import com.djesystems.kawa.customer.application.CustomerRetailerRelationService;
import com.djesystems.kawa.customer.application.CustomerService;
import com.djesystems.kawa.customer.application.NotificationDeviceService;
import com.djesystems.kawa.customer.domain.Customer;
import com.djesystems.kawa.customer.security.FirebaseUserPrincipal;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final NotificationDeviceService notificationDeviceService;
    private final ConsentDecisionService consentDecisionService;
    private final CustomerRetailerRelationService customerRetailerRelationService;

    public CustomerController(
            CustomerService customerService,
            NotificationDeviceService notificationDeviceService,
            ConsentDecisionService consentDecisionService,
            CustomerRetailerRelationService customerRetailerRelationService) {
        this.customerService = customerService;
        this.notificationDeviceService = notificationDeviceService;
        this.consentDecisionService = consentDecisionService;
        this.customerRetailerRelationService = customerRetailerRelationService;
    }

    @GetMapping("/me")
    public CustomerResponse me(Authentication authentication) {
        FirebaseUserPrincipal principal =
                (FirebaseUserPrincipal) authentication.getPrincipal();

        Customer customer = customerService.getOrCreateCustomer(
                principal.uid(),
                principal.email()
        );

        return CustomerResponse.from(customer);
    }

    @PatchMapping("/me/preferences")
    public CustomerResponse updatePreferences(
            Authentication authentication,
            @Valid @RequestBody UpdateCustomerPreferencesRequest request) {

        FirebaseUserPrincipal principal =
                (FirebaseUserPrincipal) authentication.getPrincipal();

        Customer customer = customerService.updateAutoRetailerAssociation(
                principal.uid(),
                request.autoRetailerAssociationEnabled()
        );

        return CustomerResponse.from(customer);
    }

    @GetMapping("/me/kawa-id")
    public KawaIdResponse kawaId(Authentication authentication) {
        FirebaseUserPrincipal principal =
                (FirebaseUserPrincipal) authentication.getPrincipal();

        Customer customer = customerService.getOrCreateCustomer(
                principal.uid(),
                principal.email()
        );

        return new KawaIdResponse(customer.publicKawaId());
    }

    @PostMapping("/me/notification-devices")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerNotificationDevice(
            Authentication authentication,
            @Valid @RequestBody RegisterNotificationDeviceRequest request) {
        notificationDeviceService.register(
                authentication.getName(),
                request.token(),
                request.platform()
        );
    }

    @PostMapping("/me/consent-requests/{eventId}/decision")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decideConsent(
            Authentication authentication,
            @PathVariable String eventId,
            @Valid @RequestBody ConsentDecisionRequest request) {
        consentDecisionService.decide(
                authentication.getName(),
                eventId,
                request.decision()
        );
    }

    @GetMapping("/me/retailers")
    public CustomerRetailersResponse retailers(Authentication authentication) {
        CustomerResponse customer = me(authentication);
        return customerRetailerRelationService.getRetailers(customer.publicKawaId());
    }
}
