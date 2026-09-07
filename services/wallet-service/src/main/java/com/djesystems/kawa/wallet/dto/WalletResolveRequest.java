package com.djesystems.kawa.wallet.dto;

import jakarta.validation.constraints.NotBlank;

public record WalletResolveRequest(

    @NotBlank
    String publicKawaId

) {
}