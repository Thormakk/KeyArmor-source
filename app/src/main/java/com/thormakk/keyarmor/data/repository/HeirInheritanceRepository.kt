package com.thormakk.keyarmor.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.thormakk.keyarmor.data.database.KeyArmorDatabase
import com.thormakk.keyarmor.data.export.ImportParser
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.HeirIdentityManager
import com.thormakk.keyarmor.security.InheritanceEnvelope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import android.util.Base64
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.thormakk.keyarmor.appcheck.AppCheckHeader

@Singleton
class HeirInheritanceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db:                   KeyArmorDatabase,
    private val vaultRepository:      VaultRepository,
    private val twoFactorRepository:  TwoFactorRepository,
    private val creditCardRepository: CreditCardRepository,
    private val bankAccountRepository: BankAccountRepository,
    private val cryptoWalletRepository: CryptoWalletRepository,
    private val secureNoteRepository:  SecureNoteRepository,
    private val customEntryRepository: CustomEntryRepository,
    private val passkeyRepository:     PasskeyRepository,
    private val identityDocRepository: IdentityDocumentRepository,
    private val vehicleRepository:     VehicleRepository,
    private val vaultMetadataRepository: VaultMetadataRepository,
    private val crypto:      CryptoManager,
    private val inheritanceEnvelope: InheritanceEnvelope,
    private val heirIdentityManager: HeirIdentityManager,
    private val pinnedHubPrefs: PinnedHubPrefs,
    private val inheritancePrefs: com.thormakk.keyarmor.data.prefs.InheritancePrefs,
) {

    companion object {
        private const val DOWNLOAD_URL_ENDPOINT = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val CONFIRM_RECEIVED_ENDPOINT = "<URL DEL SERVIDOR DE GOOGLE>"
    }

    data class InheritedVaultInfo(
        val ownerEmail:  String,
        val driveFileId: String = "",
    )

    suspend fun downloadAndImport(
        heirEmail:   String,
        heirSub:     String,
        heirIdToken: String,
        info:        InheritedVaultInfo,
        privateKeyCodeOverride: String? = null,
    ) = withContext(Dispatchers.IO) {
        val downloadUrl = fetchDownloadUrl(ownerEmail = info.ownerEmail, heirIdToken = heirIdToken)
        val encPayload  = downloadFromStorage(downloadUrl)
        val blobV4      = String(encPayload, Charsets.UTF_8)

        val wrapperVersion = JSONObject(blobV4).optInt("version", 0)
        require(wrapperVersion == 5) { "Unsupported inheritance blob version: $wrapperVersion (se esperaba v5)" }

        val privateCode = privateKeyCodeOverride
            ?: heirIdentityManager.getPrivateCode()
            ?: throw HeirPrivateKeyRequiredException()

        val pinnedOwnerSigningKey = inheritancePrefs.getPinnedOwnerSigningKey(info.ownerEmail)
            ?: throw InheritanceOriginUnverifiedException()

        val plainJson = try {
            inheritanceEnvelope.openAsHeir(blobV4, privateCode, pinnedOwnerSigningKey)
        } catch (e: HeirPrivateKeyRequiredException) {
            throw e
        } catch (e: java.security.SignatureException) {
            throw InheritanceOriginUnverifiedException(e)
        } catch (e: Exception) {
            throw HeirPrivateKeyInvalidException(e)
        }

        val parsed = ImportParser.parseKeyArmorJson(plainJson)

        val inheritedVaultId = VaultMetadata.inheritedId(info.ownerEmail)

        val ownerEmail = info.ownerEmail
        val passwords  = parsed.passwords.map         { it.copy(id = UUID.randomUUID().toString(), vaultId = inheritedVaultId, inheritedFrom = ownerEmail) }
        val twoFactor  = parsed.twoFactor.map         { it.copy(id = UUID.randomUUID().toString(), vaultId = inheritedVaultId, inheritedFrom = ownerEmail) }
        val cards      = parsed.creditCards.map       { it.copy(id = UUID.randomUUID().toString(), vaultId = inheritedVaultId, inheritedFrom = ownerEmail) }
        val banks      = parsed.bankAccounts.map      { it.copy(id = UUID.randomUUID().toString(), vaultId = inheritedVaultId, inheritedFrom = ownerEmail) }
        val cryptos    = parsed.cryptoWallets.map     { it.copy(id = UUID.randomUUID().toString(), vaultId = inheritedVaultId, inheritedFrom = ownerEmail) }
        val notes      = parsed.secureNotes.map       { it.copy(id = UUID.randomUUID().toString(), vaultId = inheritedVaultId, inheritedFrom = ownerEmail) }
        val custom     = parsed.customEntries.map     { it.copy(id = UUID.randomUUID().toString(), vaultId = inheritedVaultId, inheritedFrom = ownerEmail) }
        val identities = parsed.identityDocuments.map { it.copy(id = UUID.randomUUID().toString(), vaultId = inheritedVaultId, inheritedFrom = ownerEmail) }
        val vehicles   = parsed.vehicles.map          { it.copy(id = UUID.randomUUID().toString(), vaultId = inheritedVaultId, inheritedFrom = ownerEmail) }
        val passkeys   = parsed.passkeys.map          { it.copy(vaultId = inheritedVaultId, inheritedFrom = ownerEmail) }

        db.withTransaction {
            deleteInheritedVaultEntries(inheritedVaultId)

            passwords.forEach  { vaultRepository.saveEntry(it) }
            twoFactor.forEach  { twoFactorRepository.saveAccount(it) }
            cards.forEach      { creditCardRepository.saveCard(it) }
            banks.forEach      { bankAccountRepository.saveAccount(it) }
            cryptos.forEach    { cryptoWalletRepository.saveWallet(it) }
            notes.forEach      { secureNoteRepository.saveNote(it) }
            custom.forEach     { customEntryRepository.saveEntry(it) }
            passkeys.forEach   { passkeyRepository.savePasskey(it) }
            identities.forEach { identityDocRepository.saveDocument(it) }
            vehicles.forEach   { vehicleRepository.saveVehicle(it) }

            val vaultMeta = VaultMetadata(
                id          = inheritedVaultId,
                name        = "Herencia de ${info.ownerEmail}",
                isInherited = true,
                ownerEmail  = info.ownerEmail,
                sortOrder   = 999,
            )
            vaultMetadataRepository.save(vaultMeta)
        }

        runCatching {
            confirmReceived(ownerEmail = info.ownerEmail, heirIdToken = heirIdToken)
        }
    }

    private fun fetchDownloadUrl(ownerEmail: String, heirIdToken: String): String {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val body = """{"ownerEmail":"${ownerEmail.esc()}","heirIdToken":"${heirIdToken.esc()}"}"""
        val conn = URL(DOWNLOAD_URL_ENDPOINT).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 15_000
        conn.readTimeout    = 15_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        if (code !in 200..299) {
            throw Exception("onGetInheritanceDownloadUrl respondió HTTP $code")
        }
        val response = conn.inputStream.bufferedReader().readText()
        return JSONObject(response).getString("downloadUrl")
    }

    private fun confirmReceived(ownerEmail: String, heirIdToken: String) {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val body = """{"ownerEmail":"${ownerEmail.esc()}","heirIdToken":"${heirIdToken.esc()}"}"""
        val conn = URL(CONFIRM_RECEIVED_ENDPOINT).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 15_000
        conn.readTimeout    = 15_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        if (code !in 200..299) {
            throw Exception("onConfirmInheritanceReceived respondió HTTP $code")
        }
    }

    private fun downloadFromStorage(downloadUrl: String): ByteArray {
        val conn = URL(downloadUrl).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 20_000
        conn.readTimeout    = 40_000
        val code = conn.responseCode
        if (code !in 200..299) {
            throw Exception("Storage GET respondió HTTP $code")
        }
        return conn.inputStream.readBytes()
    }

    private suspend fun deleteInheritedVaultEntries(vaultId: String) {
        pinnedHubPrefs.unpinAllByVault(vaultId)

        vaultRepository.getAllForBackup()
            .filter { it.vaultId == vaultId }
            .forEach { vaultRepository.deletePermanently(it.id) }

        twoFactorRepository.getAllForBackup()
            .filter { it.vaultId == vaultId }
            .forEach { twoFactorRepository.deletePermanently(it.id) }

        creditCardRepository.getAllForBackup()
            .filter { it.vaultId == vaultId }
            .forEach { creditCardRepository.deletePermanently(it.id) }

        bankAccountRepository.getAllForBackup()
            .filter { it.vaultId == vaultId }
            .forEach { bankAccountRepository.deletePermanently(it.id) }

        cryptoWalletRepository.getAllForBackup()
            .filter { it.vaultId == vaultId }
            .forEach { cryptoWalletRepository.deletePermanently(it.id) }

        secureNoteRepository.getAllForBackup()
            .filter { it.vaultId == vaultId }
            .forEach { secureNoteRepository.deletePermanently(it.id) }

        customEntryRepository.getAllForBackup()
            .filter { it.vaultId == vaultId }
            .forEach { customEntryRepository.deletePermanently(it.id) }

        identityDocRepository.getAllForBackup()
            .filter { it.vaultId == vaultId }
            .forEach { identityDocRepository.deletePermanently(it.id) }

        vehicleRepository.getAllForBackup()
            .filter { it.vaultId == vaultId }
            .forEach { vehicleRepository.deletePermanently(it.id) }

        passkeyRepository.observePasskeys().first()
            .filter { it.vaultId == vaultId }
            .forEach { passkeyRepository.deletePermanently(it.credentialId) }
    }
}

class HeirPrivateKeyRequiredException : Exception("Se requiere la clave privada del heredero para abrir la herencia.")

class HeirPrivateKeyInvalidException(cause: Throwable? = null) :
    Exception("El código de recuperación no abre la bóveda heredada.", cause)

class InheritanceOriginUnverifiedException(cause: Throwable? = null) :
    Exception("No se pudo verificar que la herencia provenga del titular esperado.", cause)
