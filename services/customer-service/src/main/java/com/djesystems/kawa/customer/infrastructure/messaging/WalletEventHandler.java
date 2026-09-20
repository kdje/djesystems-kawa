package com.djesystems.kawa.customer.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

import com.djesystems.kawa.customer.application.ConsentRequestEventHandler;
import com.djesystems.kawa.customer.application.RetailerLinkActivatedEventHandler;

import com.djesystems.kawa.customer.infrastructure.messaging.dto.CustomerRetailerConsentRequestedEvent;
import com.djesystems.kawa.customer.infrastructure.messaging.dto.CustomerRetailerLinkActivatedEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class WalletEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    WalletEventHandler.class
            );

    private static final String CONSENT_REQUESTED =
            "CUSTOMER_RETAILER_CONSENT_REQUESTED";

    private static final String LINK_ACTIVATED =
            "CUSTOMER_RETAILER_LINK_ACTIVATED";

    private final ObjectMapper objectMapper;

    private final ConsentRequestEventHandler
            consentRequestEventHandler;

    private final RetailerLinkActivatedEventHandler
            retailerLinkActivatedEventHandler;

    public WalletEventHandler(
            ObjectMapper objectMapper,
            ConsentRequestEventHandler consentRequestEventHandler,
            RetailerLinkActivatedEventHandler retailerLinkActivatedEventHandler) {

        this.objectMapper = objectMapper;
        this.consentRequestEventHandler =
                consentRequestEventHandler;

        this.retailerLinkActivatedEventHandler =
                retailerLinkActivatedEventHandler;
    }

    public void process(
            ServiceBusReceivedMessageContext context) {

        try {

            String body =
                    context
                            .getMessage()
                            .getBody()
                            .toString();

            JsonNode root =
                    objectMapper.readTree(body);

            String eventType =
                    root
                            .path("eventType")
                            .asText();

            log.info(
                    "Wallet event received: eventType={}",
                    eventType
            );

            switch (eventType) {

                case CONSENT_REQUESTED -> {

                    CustomerRetailerConsentRequestedEvent event =
                            objectMapper.treeToValue(
                                    root,
                                    CustomerRetailerConsentRequestedEvent.class
                            );

                    consentRequestEventHandler
                            .handle(event);
                }

                case LINK_ACTIVATED -> {

                    CustomerRetailerLinkActivatedEvent event =
                            objectMapper.treeToValue(
                                    root,
                                    CustomerRetailerLinkActivatedEvent.class
                            );

                    retailerLinkActivatedEventHandler
                            .handle(event);
                }

                default ->
                        log.warn(
                                "Unsupported Wallet event: {}",
                                eventType
                        );
            }

        } catch (Exception ex) {

            log.error(
                    "Unable to process Wallet event",
                    ex
            );

            throw new IllegalStateException(
                    "Unable to process Wallet event",
                    ex
            );
        }
    }

    public void processError(
            ServiceBusErrorContext context) {

        log.error(
                "Wallet Service Bus error: {}",
                context.getException().getMessage(),
                context.getException()
        );
    }
}