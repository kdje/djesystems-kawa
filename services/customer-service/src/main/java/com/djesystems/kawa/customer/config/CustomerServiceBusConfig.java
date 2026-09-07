package com.djesystems.kawa.customer.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.core.amqp.AmqpTransportType;

@Configuration
public class CustomerServiceBusConfig {

    @Bean
    TokenCredential customerAzureCredential(
            @Value("${kawa.azure.tenant-id}") String tenantId,
            @Value("${kawa.azure.client-id}") String clientId,
            @Value("${kawa.azure.client-secret}") String clientSecret) {

        return new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    @Bean(destroyMethod = "close")
    ServiceBusSenderClient customerEventSender(
            TokenCredential customerAzureCredential,
            @Value("${kawa.azure.service-bus.fully-qualified-namespace}")
            String namespace,
            @Value("${kawa.azure.service-bus.topic}")
            String topic) {

        return new ServiceBusClientBuilder()
                .credential(namespace, customerAzureCredential)

                // Utilise AMQP over WebSockets sur le port 443
                .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)

                .sender()
                .topicName(topic)
                .buildClient();
    }
}