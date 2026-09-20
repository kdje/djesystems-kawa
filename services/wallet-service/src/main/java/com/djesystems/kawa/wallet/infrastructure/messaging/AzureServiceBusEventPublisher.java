package com.djesystems.kawa.wallet.infrastructure.messaging;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
@ConditionalOnProperty(
    name = "kawa.wallet.outbox.transport",
    havingValue = "servicebus"
)
public class AzureServiceBusEventPublisher
        implements EventPublisher {

    private static final Set<String> SUPPORTED_EVENT_TYPES =
        Set.of(
            "CUSTOMER_RETAILER_CONSENT_REQUESTED",
            "CUSTOMER_RETAILER_LINKED"
        );

    private final ServiceBusSenderClient sender;


    public AzureServiceBusEventPublisher(
            TokenCredential walletAzureCredential,

            @Value("${kawa.azure.service-bus.fully-qualified-namespace}")
            String namespace,

            @Value("${kawa.azure.service-bus.wallet.topic}")
            String walletTopic) {

        this.sender =
            new ServiceBusClientBuilder()
                .credential(
                    namespace,
                    walletAzureCredential
                )
                .transportType(
                    AmqpTransportType.AMQP_WEB_SOCKETS
                )
                .sender()
                .topicName(walletTopic)
                .buildClient();
    }


    @Override
    public void publish(
            String eventId,
            String eventType,
            String payload) {

        /*
         * Protection volontaire :
         *
         * seuls les événements explicitement
         * gérés par le wallet-service peuvent
         * être publiés sur le topic Wallet.
         */
        if (!SUPPORTED_EVENT_TYPES.contains(eventType)) {

            throw new IllegalArgumentException(
                "Unsupported outbox event type: "
                    + eventType
            );
        }


        ServiceBusMessage message =
            new ServiceBusMessage(payload);


        /*
         * MessageId permet notamment de conserver
         * l'identifiant métier de l'événement.
         */
        message.setMessageId(
            eventId
        );


        /*
         * Le subject contient le type d'événement.
         *
         * Exemple :
         *
         * CUSTOMER_RETAILER_LINKED
         */
        message.setSubject(
            eventType
        );


        /*
         * Propriété applicative utilisée
         * pour le routage / filtrage éventuel
         * dans Azure Service Bus.
         */
        message.getApplicationProperties()
            .put(
                "eventType",
                eventType
            );


        sender.sendMessage(
            message
        );
    }
}