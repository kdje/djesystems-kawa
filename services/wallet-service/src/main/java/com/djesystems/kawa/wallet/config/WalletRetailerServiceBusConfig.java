package com.djesystems.kawa.wallet.config;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.TokenCredential;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;

import com.djesystems.kawa.wallet.messaging.retailer.RetailerEventHandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalletRetailerServiceBusConfig {

    @Bean(
        name = "retailerEventProcessor",
        initMethod = "start",
        destroyMethod = "close"
    )
    ServiceBusProcessorClient retailerEventProcessor(
            TokenCredential walletAzureCredential,
            RetailerEventHandler handler,

            @Value(
                "${kawa.azure.service-bus.fully-qualified-namespace}"
            )
            String namespace,

            @Value(
                "${kawa.azure.service-bus.retailer.topic}"
            )
            String topic,

            @Value(
                "${kawa.azure.service-bus.retailer.subscription}"
            )
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