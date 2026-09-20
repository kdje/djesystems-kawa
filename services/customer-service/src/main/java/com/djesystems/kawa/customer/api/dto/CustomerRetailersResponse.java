package com.djesystems.kawa.customer.api.dto;

import java.time.Instant;
import java.util.List;

public record CustomerRetailersResponse(
        long activeCount,
        long pendingCount,
        List<RetailerRelationResponse> retailers) {

    public record RetailerRelationResponse(
            String retailerCode,
            String status,
            String sourceEventId,
            Instant createdAt,
            Instant updatedAt) {
    }
}