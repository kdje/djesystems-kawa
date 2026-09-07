package com.djesystems.kawa.wallet.dto;

import jakarta.validation.constraints.NotBlank;

public record WalletLinkRequest(

    @NotBlank
    String publicKawaId,

    @NotBlank
    String retailerCustomerId

) {
}