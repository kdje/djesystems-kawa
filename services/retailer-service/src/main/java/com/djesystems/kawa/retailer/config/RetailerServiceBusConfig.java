package com.djesystems.kawa.retailer.config;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetailerServiceBusConfig {

    @Bean
    TokenCredential retailerAzureCredential(
            @Value("${kawa.azure.tenant-id}")
            String tenantId,

            @Value("${kawa.azure.client-id}")
            String clientId,

            @Value("${kawa.azure.client-secret}")
            String clientSecret) {

        return new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    @Bean(destroyMethod = "close")
    ServiceBusSenderClient retailerEventSender(
            TokenCredential retailerAzureCredential,

            @Value("${kawa.azure.service-bus.fully-qualified-namespace}")
            String namespace,

            @Value("${kawa.azure.service-bus.topic}")
            String topic) {

        return new ServiceBusClientBuilder()
                .credential(
                        namespace,
                        retailerAzureCredential
                )

                // Comme Customer : réseau corporate via HTTPS/443
                .transportType(
                        AmqpTransportType.AMQP_WEB_SOCKETS
                )

                .sender()
                .topicName(topic)
                .buildClient();
    }
}