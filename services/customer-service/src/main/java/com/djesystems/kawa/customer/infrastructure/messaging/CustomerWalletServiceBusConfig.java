package com.djesystems.kawa.customer.infrastructure.messaging;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerWalletServiceBusConfig {

    @Bean(
        name = "walletServiceBusProcessor",
        initMethod = "start",
        destroyMethod = "close"
    )
    public ServiceBusProcessorClient walletServiceBusProcessor(
            TokenCredential customerAzureCredential,
            WalletEventHandler walletEventHandler,

            @Value("${kawa.azure.service-bus.fully-qualified-namespace}")
            String namespace,

            @Value("${kawa.azure.service-bus.wallet.topic}")
            String topic,

            @Value("${kawa.azure.service-bus.wallet.subscription}")
            String subscription) {

        return new ServiceBusClientBuilder()
            .credential(namespace, customerAzureCredential)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .processor()
            .topicName(topic)
            .subscriptionName(subscription)
            .processMessage(walletEventHandler::process)
            .processError(walletEventHandler::processError)
            .buildProcessorClient();
    }
}