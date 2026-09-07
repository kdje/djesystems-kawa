package com.djesystems.kawa.retailer.api;

import com.djesystems.kawa.retailer.application.RetailerApplicationService;
import com.djesystems.kawa.retailer.application.RetailerCredentialApplicationService;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/retailers")
public class RetailerAdminController {

    private final RetailerApplicationService retailerService;
    private final RetailerCredentialApplicationService credentialService;

    public RetailerAdminController(
            RetailerApplicationService retailerService,
            RetailerCredentialApplicationService credentialService) {

        this.retailerService = retailerService;
        this.credentialService = credentialService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RetailerResponse createRetailer(
            @RequestBody CreateRetailerRequest request) {

        return retailerService.createRetailer(request);
    }

    @PostMapping("/{retailerId}/credentials")
    @ResponseStatus(HttpStatus.CREATED)
    public RetailerCredentialResponse createCredential(
            @PathVariable String retailerId,
            @RequestBody CreateRetailerCredentialRequest request) {

        return credentialService.createCredential(
            retailerId,
            request
        );
    }
}