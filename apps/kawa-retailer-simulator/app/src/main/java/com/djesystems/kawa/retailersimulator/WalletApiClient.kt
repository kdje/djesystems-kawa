package com.djesystems.kawa.retailersimulator

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class ResolveResult(
    val status: String,
    val publicKawaId: String,
    val retailerCode: String,
    val retailerCustomerId: String?,
    val email: String?
)

class WalletApiClient {

    fun getAccessToken(config: RetailerConfig): String {
        require(config.tenantId.isNotBlank()) { "Tenant ID manquant" }
        require(config.clientId.isNotBlank()) { "Client ID manquant" }
        require(config.clientSecret.isNotBlank()) { "Client secret manquant" }
        require(config.scope.isNotBlank()) { "Scope Wallet manquant" }

        val tokenUrl =
            "https://login.microsoftonline.com/${config.tenantId}/oauth2/v2.0/token"

        val body = formEncode(
            mapOf(
                "grant_type" to "client_credentials",
                "client_id" to config.clientId,
                "client_secret" to config.clientSecret,
                "scope" to config.scope
            )
        )

        val response = request(
            url = tokenUrl,
            method = "POST",
            contentType = "application/x-www-form-urlencoded",
            body = body,
            bearerToken = null
        )

        val json = JSONObject(response)
        return json.optString("access_token").takeIf { it.isNotBlank() }
            ?: error("Entra ID n'a pas retourné access_token: $response")
    }

    fun resolve(
        config: RetailerConfig,
        accessToken: String,
        publicKawaId: String
    ): ResolveResult {
        val body = JSONObject()
            .put("publicKawaId", publicKawaId)
            .toString()

        val response = request(
            url = endpoint(config.walletBaseUrl, "/api/wallet/resolve"),
            method = "POST",
            contentType = "application/json",
            body = body,
            bearerToken = accessToken,
            acceptedCodes = setOf(200, 202, 409)
        )

        val json = JSONObject(response)
        val customer = json.optJSONObject("customer")

        return ResolveResult(
            status = json.optString("status"),
            publicKawaId = json.optString("publicKawaId", publicKawaId),
            retailerCode = json.optString("retailerCode", config.retailerCode),
            retailerCustomerId = json.optString("retailerCustomerId").takeIf { it.isNotBlank() },
            email = customer?.optString("email")?.takeIf { it.isNotBlank() }
        )
    }

    fun link(
        config: RetailerConfig,
        accessToken: String,
        publicKawaId: String,
        retailerCustomerId: String
    ) {
        val body = JSONObject()
            .put("publicKawaId", publicKawaId)
            .put("retailerCustomerId", retailerCustomerId)
            .toString()

        request(
            url = endpoint(config.walletBaseUrl, "/api/wallet/link"),
            method = "POST",
            contentType = "application/json",
            body = body,
            bearerToken = accessToken,
            acceptedCodes = setOf(204)
        )
    }

    private fun endpoint(baseUrl: String, path: String): String =
        baseUrl.trimEnd('/') + path

    private fun formEncode(values: Map<String, String>): String =
        values.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

    private fun request(
        url: String,
        method: String,
        contentType: String,
        body: String,
        bearerToken: String?,
        acceptedCodes: Set<Int> = setOf(200)
    ): String {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 15_000
        connection.readTimeout = 20_000
        connection.doOutput = true
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Content-Type", contentType)
        bearerToken?.let { connection.setRequestProperty("Authorization", "Bearer $it") }

        connection.outputStream.use { output ->
            output.write(body.toByteArray(StandardCharsets.UTF_8))
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        connection.disconnect()

        if (code !in acceptedCodes) {
            error("HTTP $code sur $url: $response")
        }

        return response
    }
}
