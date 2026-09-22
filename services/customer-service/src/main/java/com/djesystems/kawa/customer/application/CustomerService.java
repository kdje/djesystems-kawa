package com.djesystems.kawa.customer.application;

import com.djesystems.kawa.customer.domain.Customer;
import com.djesystems.kawa.customer.domain.CustomerStatus;
import com.djesystems.kawa.customer.domain.PublicKawaIdFactory;
import com.djesystems.kawa.customer.domain.event.CustomerCreatedEvent;
import com.djesystems.kawa.customer.domain.event.CustomerPreferencesUpdatedEvent;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRepository;
import com.djesystems.kawa.customer.infrastructure.persistence.OutboxEventEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CustomerService {

    private static final String CUSTOMER_AGGREGATE_TYPE = "CUSTOMER";
    private static final String CUSTOMER_CREATED_EVENT = "CUSTOMER_CREATED";
    private static final String CUSTOMER_PREFERENCES_UPDATED_EVENT = "CUSTOMER_PREFERENCES_UPDATED";

    private final CustomerRepository customerRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public CustomerService(
            CustomerRepository customerRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.customerRepository = customerRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Customer getOrCreateCustomer(String firebaseUid, String email) {
        return customerRepository
                .findByFirebaseUid(firebaseUid)
                .map(CustomerEntity::toDomain)
                .orElseGet(() -> createCustomer(firebaseUid, email));
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(String firebaseUid) {
        return customerRepository
                .findByFirebaseUid(firebaseUid)
                .map(CustomerEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found for Firebase UID: " + firebaseUid));
    }

    /**
     * Active/désactive le consentement permanent aux associations retailer.
     * La préférence et son événement Outbox sont persistés dans la même transaction.
     */
    @Transactional
    public Customer updateAutoRetailerAssociation(
            String firebaseUid,
            boolean enabled) {

        CustomerEntity entity = customerRepository
                .findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found for Firebase UID: " + firebaseUid));

        if (entity.isAutoRetailerAssociationEnabled() == enabled) {
            return entity.toDomain();
        }

        entity.setAutoRetailerAssociationEnabled(enabled);
        customerRepository.save(entity);

        String eventId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        CustomerPreferencesUpdatedEvent event =
                new CustomerPreferencesUpdatedEvent(
                        eventId,
                        CUSTOMER_PREFERENCES_UPDATED_EVENT,
                        entity.getPublicKawaId(),
                        entity.getEmail(),
                        entity.getStatus().name(),
                        enabled,
                        now
                );

        outboxEventRepository.save(
                new OutboxEventEntity(
                        eventId,
                        CUSTOMER_AGGREGATE_TYPE,
                        entity.getPublicKawaId(),
                        CUSTOMER_PREFERENCES_UPDATED_EVENT,
                        serializeEvent(event)
                )
        );

        return entity.toDomain();
    }

    private Customer createCustomer(String firebaseUid, String email) {
        Instant now = Instant.now();
        String publicKawaId = generateUniquePublicKawaId();

        CustomerEntity entity = new CustomerEntity(
                UUID.randomUUID(),
                firebaseUid,
                publicKawaId,
                email,
                CustomerStatus.ACTIVE,
                false,
                now,
                now
        );

        CustomerEntity savedEntity = customerRepository.save(entity);

        String eventId = UUID.randomUUID().toString();

        CustomerCreatedEvent event = new CustomerCreatedEvent(
                eventId,
                CUSTOMER_CREATED_EVENT,
                publicKawaId,
                email,
                CustomerStatus.ACTIVE.name(),
                false,
                now
        );

        outboxEventRepository.save(
                new OutboxEventEntity(
                        eventId,
                        CUSTOMER_AGGREGATE_TYPE,
                        publicKawaId,
                        CUSTOMER_CREATED_EVENT,
                        serializeEvent(event)
                )
        );

        return savedEntity.toDomain();
    }

    private String serializeEvent(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Unable to serialize customer event",
                    ex
            );
        }
    }

    private String generateUniquePublicKawaId() {
        String candidate;
        do {
            candidate = PublicKawaIdFactory.generate();
        } while (customerRepository.existsByPublicKawaId(candidate));
        return candidate;
    }
}
