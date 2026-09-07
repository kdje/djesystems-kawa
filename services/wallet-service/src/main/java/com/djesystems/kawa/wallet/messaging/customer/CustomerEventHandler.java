package com.djesystems.kawa.wallet.messaging.customer;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

import com.djesystems.kawa.wallet.application.CustomerProjectionService;
import com.djesystems.kawa.wallet.application.CustomerRetailerConsentDecisionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;


@Component
public class CustomerEventHandler {

    private static final Logger log =
        LoggerFactory.getLogger(
            CustomerEventHandler.class
        );

    private static final String CONSENT_DECIDED =
        "CUSTOMER_RETAILER_CONSENT_DECIDED";


    private final ObjectMapper objectMapper;

    private final CustomerProjectionService
        customerProjectionService;

    private final CustomerRetailerConsentDecisionService
        consentDecisionService;


    public CustomerEventHandler(
            ObjectMapper objectMapper,
            CustomerProjectionService customerProjectionService,
            CustomerRetailerConsentDecisionService
                consentDecisionService) {

        this.objectMapper = objectMapper;

        this.customerProjectionService =
            customerProjectionService;

        this.consentDecisionService =
            consentDecisionService;
    }


    public void processMessage(
            ServiceBusReceivedMessageContext context) {

        var message =
            context.getMessage();

        String body =
            message.getBody().toString();

        String eventType =
            message.getSubject();


        try {

            log.info(
                "Customer event received: messageId={}, eventType={}, body={}",
                message.getMessageId(),
                eventType,
                body
            );


            /*
             * ======================================================
             * ÉVÉNEMENT DE DÉCISION DE CONSENTEMENT
             * ======================================================
             */
            if (CONSENT_DECIDED.equals(eventType)) {

                CustomerRetailerConsentDecidedEvent event =
                    objectMapper.readValue(
                        body,
                        CustomerRetailerConsentDecidedEvent.class
                    );

                consentDecisionService.handle(event);


                log.info(
                    "Customer consent decision processed successfully: eventId={}, publicKawaId={}, retailerCode={}, decision={}",
                    event.eventId(),
                    event.publicKawaId(),
                    event.retailerCode(),
                    event.decision()
                );

                return;
            }


            /*
             * ======================================================
             * ÉVÉNEMENTS CUSTOMER CLASSIQUES
             * ======================================================
             *
             * On conserve exactement le comportement existant.
             */
            CustomerEvent event =
                objectMapper.readValue(
                    body,
                    CustomerEvent.class
                );


            customerProjectionService.synchronize(
                event.publicKawaId(),
                event.status(),
                event.email()
            );


            log.info(
                "Customer projection event processed successfully: eventId={}, eventType={}, publicKawaId={}, status={}",
                event.eventId(),
                event.eventType(),
                event.publicKawaId(),
                event.status()
            );


        } catch (Exception e) {

            log.error(
                "Unable to process Customer event. messageId={}, eventType={}",
                message.getMessageId(),
                eventType,
                e
            );


            throw new IllegalStateException(
                "Unable to process Customer event",
                e
            );
        }
    }


    public void processError(
            ServiceBusErrorContext context) {

        log.error(
            "Azure Service Bus consumer error. namespace={}, entity={}, source={}",
            context.getFullyQualifiedNamespace(),
            context.getEntityPath(),
            context.getErrorSource(),
            context.getException()
        );
    }
}