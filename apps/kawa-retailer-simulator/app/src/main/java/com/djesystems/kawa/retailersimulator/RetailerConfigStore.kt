package com.djesystems.kawa.retailersimulator

import android.content.Context

/**
 * Charge d'abord une éventuelle surcharge locale mémorisée dans l'application.
 * À défaut, utilise les valeurs compilées depuis local.properties.
 *
 * Cela permet de ne rien saisir sur le téléphone dans le cas normal, tout en gardant
 * la possibilité de modifier temporairement un champ depuis l'écran de test.
 */
class RetailerConfigStore(context: Context) {
    private val prefs = context.getSharedPreferences("retailer-config", Context.MODE_PRIVATE)

    fun load(retailerCode: String): RetailerConfig {
        val defaults = LocalRetailerConfigProvider.load(retailerCode)
        val prefix = "${retailerCode.uppercase()}."

        return RetailerConfig(
            retailerCode = retailerCode.uppercase(),
            tenantId = prefs.getString(prefix + "tenantId", null) ?: defaults.tenantId,
            clientId = prefs.getString(prefix + "clientId", null) ?: defaults.clientId,
            clientSecret = prefs.getString(prefix + "clientSecret", null) ?: defaults.clientSecret,
            scope = prefs.getString(prefix + "scope", null) ?: defaults.scope,
            walletBaseUrl = prefs.getString(prefix + "walletBaseUrl", null)
                ?: defaults.walletBaseUrl
        )
    }

    fun save(config: RetailerConfig) {
        val prefix = "${config.retailerCode.uppercase()}."
        prefs.edit()
            .putString(prefix + "tenantId", config.tenantId)
            .putString(prefix + "clientId", config.clientId)
            .putString(prefix + "clientSecret", config.clientSecret)
            .putString(prefix + "scope", config.scope)
            .putString(prefix + "walletBaseUrl", config.walletBaseUrl)
            .apply()
    }

    fun resetToLocalProperties(retailerCode: String) {
        val prefix = "${retailerCode.uppercase()}."
        prefs.edit()
            .remove(prefix + "tenantId")
            .remove(prefix + "clientId")
            .remove(prefix + "clientSecret")
            .remove(prefix + "scope")
            .remove(prefix + "walletBaseUrl")
            .apply()
    }
}
