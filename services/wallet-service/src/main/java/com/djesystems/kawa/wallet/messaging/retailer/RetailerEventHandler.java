package com.djesystems.kawa.wallet.messaging.retailer;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

import com.djesystems.kawa.wallet.application.RetailerCredentialProjectionService;
import com.djesystems.kawa.wallet.application.RetailerProjectionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class RetailerEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(RetailerEventHandler.class);

    private final ObjectMapper objectMapper;
    private final RetailerProjectionService retailerProjectionService;
    private final RetailerCredentialProjectionService
            credentialProjectionService;

    public RetailerEventHandler(
            ObjectMapper objectMapper,
            RetailerProjectionService retailerProjectionService,
            RetailerCredentialProjectionService
                    credentialProjectionService) {

        this.objectMapper = objectMapper;
        this.retailerProjectionService =
                retailerProjectionService;

        this.credentialProjectionService =
                credentialProjectionService;
    }

    public void processMessage(
            ServiceBusReceivedMessageContext context) {

        var message = context.getMessage();

        String eventType =
                message.getSubject();

        String body =
                message.getBody().toString();

        try {

            log.info(
                "Retailer event received: messageId={}, eventType={}, body={}",
                message.getMessageId(),
                eventType,
                body
            );

            switch (eventType) {

                case "RETAILER_CREATED" ->
                    processRetailerCreated(body);

                case "RETAILER_CREDENTIAL_CREATED" ->
                    processCredentialCreated(body);

                default ->
                    throw new IllegalArgumentException(
                        "Unsupported Retailer event type: "
                            + eventType
                    );
            }

            log.info(
                "Retailer event processed successfully: messageId={}, eventType={}",
                message.getMessageId(),
                eventType
            );

        } catch (Exception e) {

            log.error(
                "Unable to process Retailer event. messageId={}, eventType={}",
                message.getMessageId(),
                eventType,
                e
            );

            throw new IllegalStateException(
                "Unable to process Retailer event",
                e
            );
        }
    }

    private void processRetailerCreated(
            String body) throws Exception {

        RetailerCreatedEvent event =
                objectMapper.readValue(
                    body,
                    RetailerCreatedEvent.class
                );

        retailerProjectionService.synchronize(
            event.retailerId(),
            event.retailerCode(),
            event.name(),
            event.countryCode(),
            event.status()
        );
    }

    private void processCredentialCreated(
            String body) throws Exception {

        RetailerCredentialCreatedEvent event =
                objectMapper.readValue(
                    body,
                    RetailerCredentialCreatedEvent.class
                );

        credentialProjectionService.synchronize(
            event.retailerId(),
            event.retailerCode(),
            event.environment(),
            event.entraClientId(),
            event.enabled()
        );
    }

    public void processError(
            ServiceBusErrorContext context) {

        log.error(
            "Azure Service Bus Retailer consumer error. namespace={}, entity={}, source={}",
            context.getFullyQualifiedNamespace(),
            context.getEntityPath(),
            context.getErrorSource(),
            context.getException()
        );
    }
}