package com.djesystems.kawa.customer.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.djesystems.kawa.customer.application.ConsentRequestEventHandler;
import com.djesystems.kawa.customer.application.CustomerRetailerConsentApprovedEventHandler;
import com.djesystems.kawa.customer.application.CustomerRetailerLinkedEventHandler;
import com.djesystems.kawa.customer.infrastructure.messaging.dto.CustomerRetailerConsentApprovedEvent;
import com.djesystems.kawa.customer.infrastructure.messaging.dto.CustomerRetailerConsentRequestedEvent;
import com.djesystems.kawa.customer.infrastructure.messaging.dto.CustomerRetailerLinkedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WalletEventHandler {

    private static final Logger log = LoggerFactory.getLogger(WalletEventHandler.class);

    private static final String CONSENT_REQUESTED =
            "CUSTOMER_RETAILER_CONSENT_REQUESTED";
    private static final String CONSENT_APPROVED =
            "CUSTOMER_RETAILER_CONSENT_APPROVED";
    private static final String RETAILER_LINKED =
            "CUSTOMER_RETAILER_LINKED";

    private final ObjectMapper objectMapper;
    private final ConsentRequestEventHandler consentRequestEventHandler;
    private final CustomerRetailerConsentApprovedEventHandler consentApprovedEventHandler;
    private final CustomerRetailerLinkedEventHandler customerRetailerLinkedEventHandler;

    public WalletEventHandler(
            ObjectMapper objectMapper,
            ConsentRequestEventHandler consentRequestEventHandler,
            CustomerRetailerConsentApprovedEventHandler consentApprovedEventHandler,
            CustomerRetailerLinkedEventHandler customerRetailerLinkedEventHandler) {
        this.objectMapper = objectMapper;
        this.consentRequestEventHandler = consentRequestEventHandler;
        this.consentApprovedEventHandler = consentApprovedEventHandler;
        this.customerRetailerLinkedEventHandler = customerRetailerLinkedEventHandler;
    }

    public void process(ServiceBusReceivedMessageContext context) {
        try {
            String body = context.getMessage().getBody().toString();
            JsonNode root = objectMapper.readTree(body);
            String eventType = root.path("eventType").asText();

            log.info(
                    "Wallet event received: eventType={}, messageId={}",
                    eventType,
                    context.getMessage().getMessageId()
            );

            switch (eventType) {
                case CONSENT_REQUESTED -> consentRequestEventHandler.handle(
                        objectMapper.treeToValue(
                                root,
                                CustomerRetailerConsentRequestedEvent.class
                        )
                );

                case CONSENT_APPROVED -> consentApprovedEventHandler.handle(
                        objectMapper.treeToValue(
                                root,
                                CustomerRetailerConsentApprovedEvent.class
                        )
                );

                case RETAILER_LINKED -> customerRetailerLinkedEventHandler.handle(
                        objectMapper.treeToValue(
                                root,
                                CustomerRetailerLinkedEvent.class
                        )
                );

                default -> log.warn(
                        "Unsupported Wallet event: eventType={}, messageId={}",
                        eventType,
                        context.getMessage().getMessageId()
                );
            }
        } catch (Exception ex) {
            log.error("Unable to process Wallet event", ex);
            throw new IllegalStateException("Unable to process Wallet event", ex);
        }
    }

    public void processError(ServiceBusErrorContext context) {
        log.error(
                "Wallet Service Bus error: {}",
                context.getException().getMessage(),
                context.getException()
        );
    }
}
