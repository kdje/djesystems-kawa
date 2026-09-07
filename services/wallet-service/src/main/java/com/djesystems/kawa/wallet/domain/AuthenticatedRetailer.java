package com.djesystems.kawa.wallet.domain;

public record AuthenticatedRetailer(
        String clientId,
        String retailerCode) {
}