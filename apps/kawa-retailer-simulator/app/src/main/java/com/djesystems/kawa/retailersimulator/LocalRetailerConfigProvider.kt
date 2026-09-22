package com.djesystems.kawa.retailersimulator

/**
 * Configuration injectée au build depuis local.properties.
 *
 * IMPORTANT : ces valeurs sont destinées uniquement au simulateur de développement.
 * Un client_secret embarqué dans un APK peut être extrait et ne doit jamais être utilisé
 * comme mécanisme de sécurité d'une application distribuée en production.
 */
object LocalRetailerConfigProvider {

    fun load(retailerCode: String): RetailerConfig {
        val (clientId, clientSecret) = when (retailerCode.uppercase()) {
            "AUCHAN" -> BuildConfig.AUCHAN_CLIENT_ID to BuildConfig.AUCHAN_CLIENT_SECRET
            "CARREFOUR" -> BuildConfig.CARREFOUR_CLIENT_ID to BuildConfig.CARREFOUR_CLIENT_SECRET
            "CORA" -> BuildConfig.CORA_CLIENT_ID to BuildConfig.CORA_CLIENT_SECRET
            "LIDL" -> BuildConfig.LIDL_CLIENT_ID to BuildConfig.LIDL_CLIENT_SECRET
            else -> "" to ""
        }

        return RetailerConfig(
            retailerCode = retailerCode.uppercase(),
            tenantId = BuildConfig.KAWA_TENANT_ID,
            clientId = clientId,
            clientSecret = clientSecret,
            scope = BuildConfig.KAWA_WALLET_SCOPE,
            walletBaseUrl = BuildConfig.KAWA_WALLET_BASE_URL.ifBlank {
                "https://api-dev.kawa-retail.com"
            }
        )
    }
}
