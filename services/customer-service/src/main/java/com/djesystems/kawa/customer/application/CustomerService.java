package com.djesystems.kawa.customer.application;

import com.djesystems.kawa.customer.domain.Customer;
import com.djesystems.kawa.customer.domain.CustomerStatus;
import com.djesystems.kawa.customer.domain.PublicKawaIdFactory;
import com.djesystems.kawa.customer.domain.event.CustomerCreatedEvent;
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

    /**
     * Retourne le client correspondant au Firebase UID.
     *
     * Si le client Firebase existe mais n'a encore jamais été créé
     * dans KAWA :
     *
     * 1. création du customer KAWA
     * 2. création de l'événement CUSTOMER_CREATED dans l'outbox
     *
     * Les deux opérations appartiennent à la même transaction.
     */
    @Transactional
    public Customer getOrCreateCustomer(
            String firebaseUid,
            String email
    ) {
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
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found for Firebase UID: "
                                        + firebaseUid
                        )
                );
    }

    /**
     * Création d'un nouveau Customer KAWA.
     *
     * IMPORTANT :
     * cette méthode est appelée depuis getOrCreateCustomer(),
     * qui possède @Transactional.
     *
     * Le customer et l'événement Outbox sont donc validés
     * ou annulés ensemble.
     */
    private Customer createCustomer(
            String firebaseUid,
            String email
    ) {

        Instant now = Instant.now();

        String publicKawaId = generateUniquePublicKawaId();

        CustomerEntity entity = new CustomerEntity(
                UUID.randomUUID(),
                firebaseUid,
                publicKawaId,
                email,
                CustomerStatus.ACTIVE,
                now,
                now
        );

        /*
         * 1. Enregistrement du Customer.
         */
        CustomerEntity savedEntity =
                customerRepository.save(entity);

        /*
         * 2. Création de l'événement métier.
         */
        String eventId = UUID.randomUUID().toString();

        CustomerCreatedEvent event =
                new CustomerCreatedEvent(
                        eventId,
                        CUSTOMER_CREATED_EVENT,
                        publicKawaId,
                        email,
                        CustomerStatus.ACTIVE.name(),
                        now
                );

        /*
         * 3. Sérialisation JSON du payload qui sera,
         * plus tard, envoyé sur Azure Service Bus.
         */
        String payload = serializeEvent(event);

        /*
         * 4. Enregistrement dans la table Outbox.
         *
         * published_at reste NULL.
         *
         * Cela signifie :
         * "cet événement doit encore être publié sur le bus".
         */
        OutboxEventEntity outboxEvent =
                new OutboxEventEntity(
                        eventId,
                        CUSTOMER_AGGREGATE_TYPE,
                        publicKawaId,
                        CUSTOMER_CREATED_EVENT,
                        payload
                );

        outboxEventRepository.save(outboxEvent);

        /*
         * Si une exception survient avant la fin de la méthode,
         * Spring rollbackera :
         *
         * - INSERT customers
         * - INSERT outbox_event
         */
        return savedEntity.toDomain();
    }

    /**
     * Sérialise l'événement métier en JSON.
     *
     * Une erreur de sérialisation provoque une RuntimeException,
     * donc la transaction Customer + Outbox est rollbackée.
     */
    private String serializeEvent(CustomerCreatedEvent event) {

        try {
            return objectMapper.writeValueAsString(event);

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(
                    "Unable to serialize CUSTOMER_CREATED event",
                    ex
            );
        }
    }

    /**
     * La probabilité d'une collision UUID est extrêmement faible,
     * mais nous vérifions quand même l'unicité en base.
     */
    private String generateUniquePublicKawaId() {

        String candidate;

        do {
            candidate = PublicKawaIdFactory.generate();
        }
        while (customerRepository.existsByPublicKawaId(candidate));

        return candidate;
    }
}