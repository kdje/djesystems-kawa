package com.djesystems.kawa.wallet.config;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.djesystems.kawa.wallet.messaging.customer.CustomerEventHandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalletCustomerServiceBusConfig {

    @Bean
    TokenCredential walletAzureCredential(
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

    @Bean(
            initMethod = "start",
            destroyMethod = "close"
    )
    ServiceBusProcessorClient customerEventProcessor(
            TokenCredential walletAzureCredential,
            CustomerEventHandler handler,

            @Value("${kawa.azure.service-bus.fully-qualified-namespace}")
            String namespace,

            @Value("${kawa.azure.service-bus.customer.topic}")
            String topic,

            @Value("${kawa.azure.service-bus.customer.subscription}")
            String subscription) {

        return new ServiceBusClientBuilder()
                .credential(
                        namespace,
                        walletAzureCredential
                )

                .transportType(
                        AmqpTransportType.AMQP_WEB_SOCKETS
                )

                .processor()

                .topicName(topic)

                .subscriptionName(subscription)

                .processMessage(
                        handler::processMessage
                )

                .processError(
                        handler::processError
                )

                .buildProcessorClient();
    }
}