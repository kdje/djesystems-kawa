package com.djesystems.kawa.retailersimulator

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.djesystems.kawa.retailersimulator.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.UUID
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var configStore: RetailerConfigStore
    private val apiClient = WalletApiClient()
    private val executor = Executors.newSingleThreadExecutor()

    private val retailers = listOf("AUCHAN", "CARREFOUR", "CORA", "LIDL")

    private val scanner = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { scanned ->
            binding.kawaIdInput.setText(extractKawaId(scanned))
            appendLog("QR scanné : ${extractKawaId(scanned)}")
            runWorkflow()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configStore = RetailerConfigStore(this)

        binding.retailerSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            retailers
        )

        binding.retailerSpinner.setSelection(0)
        loadConfig(retailers.first())
        appendLog("Configuration chargée depuis local.properties / surcharge locale Android.")

        binding.retailerSpinner.setOnItemSelectedListener(
            SimpleItemSelectedListener { position ->
                loadConfig(retailers[position])
            }
        )

        binding.saveConfigButton.setOnClickListener {
            configStore.save(readConfig())
            appendLog("Configuration ${selectedRetailer()} enregistrée localement.")
        }

        binding.scanButton.setOnClickListener {
            scanner.launch(
                ScanOptions()
                    .setPrompt("Scanner le QR KAWA du client")
                    .setBeepEnabled(true)
                    .setOrientationLocked(true)
            )
        }

        binding.simulateButton.setOnClickListener {
            runWorkflow()
        }
    }

    private fun runWorkflow() {
        val publicKawaId = binding.kawaIdInput.text.toString().trim()
        if (publicKawaId.isBlank()) {
            appendLog("ERREUR : scanne ou saisis d'abord un Kawa ID.")
            return
        }

        val config = readConfig()
        configStore.save(config)
        setBusy(true)
        binding.logView.text = ""
        appendLog("Retailer sélectionné : ${config.retailerCode}")
        appendLog("Customer : $publicKawaId")

        executor.execute {
            try {
                postLog("1/4 Authentification OAuth2 client_credentials…")
                val token = apiClient.getAccessToken(config)
                postLog("2/4 Token obtenu. Appel Wallet /resolve…")

                val result = apiClient.resolve(config, token, publicKawaId)
                postLog("3/4 Resolve => ${result.status}")

                when (result.status) {
                    "CONSENT_APPROVED" -> {
                        result.email?.let { postLog("Données client reçues : email=$it") }
                        val retailerCustomerId = simulatedRetailerCustomerId(
                            config.retailerCode,
                            publicKawaId
                        )
                        postLog("Création client simulée chez ${config.retailerCode}: $retailerCustomerId")
                        apiClient.link(
                            config,
                            token,
                            publicKawaId,
                            retailerCustomerId
                        )
                        postLog("4/4 LINK effectué. Association ACTIVE.")
                    }

                    "LINKED" ->
                        postLog("Déjà lié : retailerCustomerId=${result.retailerCustomerId}")

                    "CONSENT_REQUIRED", "CONSENT_PENDING" ->
                        postLog(
                            "Consentement explicite requis. Valide la notification côté client puis relance le scan."
                        )

                    "CONSENT_REJECTED" ->
                        postLog("Le client a refusé cette association.")

                    "CUSTOMER_SYNC_PENDING" ->
                        postLog("Projection Wallet pas encore synchronisée. Réessaie dans quelques secondes.")

                    "CUSTOMER_NOT_ACTIVE" ->
                        postLog("Customer KAWA non actif.")

                    else ->
                        postLog("Statut Wallet non géré : ${result.status}")
                }
            } catch (e: Exception) {
                postLog("ERREUR : ${e.message}")
            } finally {
                runOnUiThread { setBusy(false) }
            }
        }
    }

    private fun selectedRetailer(): String =
        retailers[binding.retailerSpinner.selectedItemPosition.coerceAtLeast(0)]

    private fun readConfig(): RetailerConfig = RetailerConfig(
        retailerCode = selectedRetailer(),
        tenantId = binding.tenantIdInput.text.toString().trim(),
        clientId = binding.clientIdInput.text.toString().trim(),
        clientSecret = binding.clientSecretInput.text.toString(),
        scope = binding.scopeInput.text.toString().trim(),
        walletBaseUrl = binding.walletUrlInput.text.toString().trim()
    )

    private fun loadConfig(retailerCode: String) {
        val config = configStore.load(retailerCode)
        binding.tenantIdInput.setText(config.tenantId)
        binding.clientIdInput.setText(config.clientId)
        binding.clientSecretInput.setText(config.clientSecret)
        binding.scopeInput.setText(config.scope)
        binding.walletUrlInput.setText(config.walletBaseUrl)
    }

    private fun setBusy(busy: Boolean) {
        binding.progress.visibility = if (busy) View.VISIBLE else View.GONE
        binding.simulateButton.isEnabled = !busy
        binding.scanButton.isEnabled = !busy
    }

    private fun appendLog(message: String) {
        val previous = binding.logView.text.toString()
        binding.logView.text = if (previous.isBlank()) message else "$previous\n$message"
    }

    private fun postLog(message: String) = runOnUiThread { appendLog(message) }

    private fun simulatedRetailerCustomerId(retailerCode: String, publicKawaId: String): String {
        val stable = UUID.nameUUIDFromBytes(
            "$retailerCode:$publicKawaId".toByteArray()
        ).toString().substring(0, 12)
        return "SIM-$retailerCode-$stable"
    }

    private fun extractKawaId(raw: String): String {
        val trimmed = raw.trim()
        val marker = "publicKawaId="
        return if (trimmed.contains(marker)) {
            trimmed.substringAfter(marker).substringBefore('&').trim()
        } else {
            trimmed
        }
    }
}
