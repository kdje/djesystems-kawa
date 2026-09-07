package com.djesystems.kawa.retailer.application;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import com.djesystems.kawa.retailer.api.CreateRetailerRequest;
import com.djesystems.kawa.retailer.api.RetailerResponse;
import com.djesystems.kawa.retailer.domain.RetailerStatus;
import com.djesystems.kawa.retailer.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.OutboxEventRepository;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RetailerApplicationService {

    private final RetailerRepository retailerRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public RetailerApplicationService(
            RetailerRepository retailerRepository,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper) {

        this.retailerRepository = retailerRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RetailerResponse createRetailer(
            CreateRetailerRequest request) {

        String code =
                request.code()
                    .trim()
                    .toUpperCase(Locale.ROOT);

        String countryCode =
                request.countryCode()
                    .trim()
                    .toUpperCase(Locale.ROOT);

        if (retailerRepository.findByCode(code).isPresent()) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Retailer already exists: " + code
            );
        }

        String retailerId =
                UUID.randomUUID().toString();

        RetailerEntity retailer =
                new RetailerEntity(
                    retailerId,
                    code,
                    request.name().trim(),
                    countryCode,
                    request.logoUrl(),
                    RetailerStatus.ACTIVE
                );

        retailerRepository.save(retailer);

        /*
         * L'eventId est également le messageId Service Bus
         * lorsqu'on publiera l'événement.
         */
        String eventId =
                UUID.randomUUID().toString();

        RetailerCreatedEvent event =
                new RetailerCreatedEvent(
                    eventId,
                    "RETAILER_CREATED",
                    Instant.now(),
                    retailerId,
                    code,
                    request.name().trim(),
                    countryCode,
                    RetailerStatus.ACTIVE
                );

        String payload;

        try {
            payload =
                    objectMapper.writeValueAsString(event);

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                "Unable to serialize RETAILER_CREATED event",
                e
            );
        }

        OutboxEventEntity outboxEvent =
                new OutboxEventEntity(
                    eventId,
                    "RETAILER",
                    retailerId,
                    "RETAILER_CREATED",
                    payload
                );

        outboxRepository.save(outboxEvent);

        return new RetailerResponse(
            retailer.getId(),
            retailer.getCode(),
            retailer.getName(),
            retailer.getCountryCode(),
            retailer.getLogoUrl(),
            retailer.getStatus()
        );
    }
}