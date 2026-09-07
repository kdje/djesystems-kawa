package com.djesystems.kawa.wallet.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djesystems.kawa.wallet.application.RetailerIdentityService;
import com.djesystems.kawa.wallet.application.WalletResolutionService;
import com.djesystems.kawa.wallet.domain.AuthenticatedRetailer;
import com.djesystems.kawa.wallet.dto.WalletAuthenticationResponse;
import com.djesystems.kawa.wallet.dto.WalletResolveRequest;
import com.djesystems.kawa.wallet.dto.WalletResolveResponse;

import com.djesystems.kawa.wallet.application.RetailerCustomerLinkService;
import com.djesystems.kawa.wallet.dto.WalletLinkRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final RetailerIdentityService retailerIdentityService;
    private final WalletResolutionService walletResolutionService;
    private final RetailerCustomerLinkService retailerCustomerLinkService;

    public WalletController(
        RetailerIdentityService retailerIdentityService,
        WalletResolutionService walletResolutionService,
        RetailerCustomerLinkService retailerCustomerLinkService) {

        this.retailerIdentityService = retailerIdentityService;
        this.walletResolutionService = walletResolutionService;
        this.retailerCustomerLinkService = retailerCustomerLinkService;
    }

    @GetMapping("/whoami")
    public WalletAuthenticationResponse whoAmI(
            @AuthenticationPrincipal Jwt jwt) {

        AuthenticatedRetailer retailer =
                retailerIdentityService.resolve(jwt);

        return new WalletAuthenticationResponse(
                retailer.clientId(),
                retailer.retailerCode()
        );
    }

    @PostMapping("/resolve")
    public ResponseEntity<WalletResolveResponse> resolve(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WalletResolveRequest request) {

        AuthenticatedRetailer retailer =
                retailerIdentityService.resolve(jwt);

        WalletResolveResponse response =
                walletResolutionService.resolve(
                        request.publicKawaId(),
                        retailer
                );

        if ("LINKED".equals(response.status())) {
            return ResponseEntity.ok(response);
        }

        if ("CUSTOMER_SYNC_PENDING".equals(response.status())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    @PostMapping("/link")
    public ResponseEntity<Void> link(
                @AuthenticationPrincipal Jwt jwt,
                @Valid @RequestBody WalletLinkRequest request) {

        /*
        * Identification de l'enseigne
        * depuis le JWT Entra.
        */
        AuthenticatedRetailer retailer =
                retailerIdentityService.resolve(jwt);


        retailerCustomerLinkService.link(
                request.publicKawaId(),
                request.retailerCustomerId(),
                retailer
        );


        return ResponseEntity
                .noContent()
                .build();
        }
    }