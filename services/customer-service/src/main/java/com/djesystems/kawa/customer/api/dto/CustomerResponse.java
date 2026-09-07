package com.djesystems.kawa.customer.api.dto;

import com.djesystems.kawa.customer.domain.Customer;

import java.time.Instant;

public record CustomerResponse(
        String publicKawaId,
        String email,
        String status,
        Instant createdAt
) {

    public static CustomerResponse from(Customer customer) {

        return new CustomerResponse(
                customer.publicKawaId(),
                customer.email(),
                customer.status().name(),
                customer.createdAt()
        );
    }
}
