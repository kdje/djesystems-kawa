package com.djesystems.kawa.customer.api.dto;

import com.djesystems.kawa.customer.domain.ConsentDecision;
import jakarta.validation.constraints.NotNull;

public record ConsentDecisionRequest(

        @NotNull
        ConsentDecision decision

) {
}