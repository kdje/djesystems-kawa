package com.djesystems.kawa.customer.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.djesystems.kawa.customer.application.ConsentRequestEventHandler;
import com.djesystems.kawa.customer.infrastructure.messaging.dto.CustomerRetailerConsentRequestedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WalletEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(WalletEventHandler.class);

    private static final String CONSENT_REQUESTED =
            "CUSTOMER_RETAILER_CONSENT_REQUESTED";

    private final ObjectMapper objectMapper;
    private final ConsentRequestEventHandler consentRequestEventHandler;

    public WalletEventHandler(
            ObjectMapper objectMapper,
            ConsentRequestEventHandler consentRequestEventHandler) {

        this.objectMapper = objectMapper;
        this.consentRequestEventHandler = consentRequestEventHandler;
    }

    public void process(ServiceBusReceivedMessageContext context) {

        var message = context.getMessage();

        try {

            String payload = message.getBody().toString();

            CustomerRetailerConsentRequestedEvent event =
                    objectMapper.readValue(
                            payload,
                            CustomerRetailerConsentRequestedEvent.class
                    );

            log.info(
                    "Wallet event received: eventId={}, eventType={}, publicKawaId={}, retailerCode={}",
                    event.eventId(),
                    event.eventType(),
                    event.publicKawaId(),
                    event.retailerCode()
            );

            if (!CONSENT_REQUESTED.equals(event.eventType())) {
                log.debug(
                        "Wallet event ignored: eventType={}",
                        event.eventType()
                );
                return;
            }

            consentRequestEventHandler.handle(event);

        } catch (Exception e) {

            log.error(
                    "Unable to process Wallet Service Bus message. messageId={}",
                    message.getMessageId(),
                    e
            );

            throw new IllegalStateException(
                    "Unable to process Wallet event",
                    e
            );
        }
    }

    public void processError(ServiceBusErrorContext context) {

        log.error(
                "Azure Service Bus error while consuming Wallet events",
                context.getException()
        );
    }
}