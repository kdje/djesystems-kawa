package com.djesystems.kawa.wallet.infrastructure.messaging;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "kawa.wallet.outbox.transport",
    havingValue = "servicebus"
)
public class AzureServiceBusEventPublisher
        implements EventPublisher {

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

        if (!"CUSTOMER_RETAILER_CONSENT_REQUESTED".equals(eventType)) {
            throw new IllegalArgumentException(
                "Unsupported outbox event type: " + eventType
            );
        }

        ServiceBusMessage message =
            new ServiceBusMessage(payload);

        message.setMessageId(eventId);
        message.setSubject(eventType);

        message.getApplicationProperties()
            .put("eventType", eventType);

        sender.sendMessage(message);
    }
}