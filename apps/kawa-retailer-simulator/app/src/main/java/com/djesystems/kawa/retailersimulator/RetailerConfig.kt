package com.djesystems.kawa.retailersimulator

data class RetailerConfig(
    val retailerCode: String,
    val tenantId: String,
    val clientId: String,
    val clientSecret: String,
    val scope: String,
    val walletBaseUrl: String
)
