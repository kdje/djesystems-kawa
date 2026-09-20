package com.djesystems.kawa.customer.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djesystems.kawa.customer.api.dto.CustomerRetailersResponse;
import com.djesystems.kawa.customer.api.dto.CustomerRetailersResponse.RetailerRelationResponse;
import com.djesystems.kawa.customer.domain.CustomerRetailerRelationStatus;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRetailerRelationEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerRetailerRelationRepository;

@Service
public class CustomerRetailerRelationService {

    private final CustomerRetailerRelationRepository repository;

    public CustomerRetailerRelationService(
            CustomerRetailerRelationRepository repository) {

        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public CustomerRetailersResponse getRetailers(
            String publicKawaId) {

        List<CustomerRetailerRelationEntity> relations =
                repository.findByPublicKawaIdOrderByUpdatedAtDesc(
                        publicKawaId
                );

        long activeCount =
                repository.countByPublicKawaIdAndStatus(
                        publicKawaId,
                        CustomerRetailerRelationStatus.ACTIVE
                );

        long pendingCount =
                repository.countByPublicKawaIdAndStatus(
                        publicKawaId,
                        CustomerRetailerRelationStatus.PENDING
                );

        List<RetailerRelationResponse> retailers =
                relations.stream()
                        .map(relation ->
                                new RetailerRelationResponse(
                                        relation.getRetailerCode(),
                                        relation.getStatus().name(),
                                        relation.getSourceEventId(),
                                        relation.getCreatedAt(),
                                        relation.getUpdatedAt()
                                )
                        )
                        .toList();

        return new CustomerRetailersResponse(
                activeCount,
                pendingCount,
                retailers
        );
    }
}