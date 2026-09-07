package com.djesystems.kawa.wallet.dto;

public record WalletResolveResponse(

    String status,

    String publicKawaId,

    String retailerCode,

    String retailerCustomerId,

    WalletCustomerDetails customer

) {
}