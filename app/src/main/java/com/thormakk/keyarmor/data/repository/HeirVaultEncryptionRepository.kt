package com.thormakk.keyarmor.data.repository

import android.content.Context
import com.thormakk.keyarmor.presentation.util.getFirebaseIdentityTokenOrNull
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.export.ExportSerializer
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.InheritanceEnvelope
import com.thormakk.keyarmor.security.SigningKeyLostException
import com.thormakk.keyarmor.security.HeirKeyNotConfirmedException
import com.thormakk.keyarmor.security.InheritanceKeyManager
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import android.util.Base64
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import com.thormakk.keyarmor.appcheck.AppCheckHeader

@Singleton
class HeirVaultEncryptionRepository @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val vaultRepo:           VaultRepository,
    private val twoFactorRepo:       TwoFactorRepository,
    private val creditCardRepo:      CreditCardRepository,
    private val bankAccountRepo:     BankAccountRepository,
    private val cryptoWalletRepo:    CryptoWalletRepository,
    private val secureNoteRepo:      SecureNoteRepository,
    private val customEntryRepo:     CustomEntryRepository,
    private val passkeyRepo:         PasskeyRepository,
    private val identityDocRepo:     IdentityDocumentRepository,
    private val vehicleRepo:         VehicleRepository,
    private val crypto:              CryptoManager,
    private val inheritanceKeyManager: InheritanceKeyManager,
    private val inheritanceEnvelope: InheritanceEnvelope,
    private val ownerSigningIdentityManager: com.thormakk.keyarmor.security.OwnerSigningIdentityManager,
    private val drivePrefs:          DrivePrefs,
    private val inheritancePrefs:    InheritancePrefs,
    private val vaultMetadataRepo:   VaultMetadataRepository
) {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    companion object {
        private const val TAG = "HeirVaultEncryptionRepository"
        private const val UPLOAD_URL_ENDPOINT = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val STORE_FILE_ID_ENDPOINT = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val V4_SEALED_MARKER = "storage-v4"
    }

    suspend fun encryptAndUpload(heirEmail: String, heirPublicKeyB64: String) =
        withContext(Dispatchers.IO) {
            if (inheritancePrefs.getConfirmedHeirKey(heirEmail) != heirPublicKeyB64) {
                throw HeirKeyNotConfirmedException(heirEmail)
            }
            val email = drivePrefs.getConnectedEmail()
                ?: error(context.getString(R.string.drive_error_email_not_configured))

            val deviceToken = inheritancePrefs.getDeviceToken()
                ?: error(context.getString(R.string.inheritance_error_device_token_unavailable))

            val plainJson = serializeVaults()

            val blob = inheritanceEnvelope.sealForHeir(plainJson, heirPublicKeyB64) {
                ownerSigningIdentityManager.sign(it)
            }
            ownerSigningIdentityManager.markConfirmed()
            val payload = blob.toByteArray(Charsets.UTF_8)

            val ownerIdToken = tryGetOwnerIdToken()
            val uploadTarget = fetchUploadUrl(ownerEmail = email, heirEmail = heirEmail, deviceToken = deviceToken, ownerIdToken = ownerIdToken)
            uploadToStorage(uploadTarget, payload)

            runCatching { markVaultSealed(email, heirEmail, deviceToken, ownerIdToken) }
                .onFailure { android.util.Log.w(TAG, "markVaultSealed falló (se reintentará): ${it.message}") }

            inheritancePrefs.saveHeirEncryptionEntry(
                InheritancePrefs.HeirEncryptionInfo(
                    heirEmail              = heirEmail,
                    encryptedHeirSubBase64 = "",
                    pepperBase64           = "",
                    heirPublicKeyB64       = heirPublicKeyB64
                )
            )
        }

    suspend fun reEncryptAll(): ReEncryptAllResult = withContext(Dispatchers.IO) {
        val email = drivePrefs.getConnectedEmail()
            ?: return@withContext ReEncryptAllResult(attempted = false, succeeded = emptyList(), failed = emptyList())
        val heirs = inheritancePrefs.getHeirEncryptionData()
        if (heirs.isEmpty()) {
            return@withContext ReEncryptAllResult(attempted = false, succeeded = emptyList(), failed = emptyList())
        }

        val plainJson = serializeVaults()

        val succeeded = mutableListOf<String>()
        val failed    = mutableListOf<HeirReEncryptFailure>()

        for (heir in heirs) {
            if (heir.heirPublicKeyB64.isEmpty()) {
                failed.add(HeirReEncryptFailure(heir.heirEmail, ReEncryptFailureReason.LOCAL_KEY_LOST))
                continue
            }
            runCatching {
                val deviceToken = inheritancePrefs.getDeviceToken()
                    ?: error(context.getString(R.string.inheritance_error_device_token_unavailable))
                val ownerIdToken = tryGetOwnerIdToken()
                val blob      = inheritanceEnvelope.sealForHeir(plainJson, heir.heirPublicKeyB64) {
                    ownerSigningIdentityManager.sign(it)
                }
                val payload   = blob.toByteArray(Charsets.UTF_8)
                val uploadTarget = fetchUploadUrl(ownerEmail = email, heirEmail = heir.heirEmail, deviceToken = deviceToken, ownerIdToken = ownerIdToken)
                uploadToStorage(uploadTarget, payload)
            }.onSuccess {
                succeeded.add(heir.heirEmail)
            }.onFailure { e ->
                val razon = if (e is SigningKeyLostException)
                    ReEncryptFailureReason.SIGNING_KEY_LOST
                else
                    ReEncryptFailureReason.UPLOAD_ERROR
                failed.add(HeirReEncryptFailure(heir.heirEmail, razon))
            }
        }

        ReEncryptAllResult(attempted = true, succeeded = succeeded, failed = failed)
    }

    suspend fun hasAnyEntries(): Boolean {
        val allowedIds = vaultMetadataRepo.observeAll().first()
            .filter { !it.isInherited && !it.isPrivate }
            .map { it.id }
            .toSet()
        if (allowedIds.isEmpty()) return false

        fun <T : Any> List<T>.anyInAllowed(getId: (T) -> String) = any { getId(it) in allowedIds }

        return vaultRepo.getAllForBackup().anyInAllowed          { it.vaultId } ||
            twoFactorRepo.getAllForBackup().anyInAllowed         { it.vaultId } ||
            creditCardRepo.getAllForBackup().anyInAllowed        { it.vaultId } ||
            bankAccountRepo.getAllForBackup().anyInAllowed       { it.vaultId } ||
            cryptoWalletRepo.getAllForBackup().anyInAllowed      { it.vaultId } ||
            secureNoteRepo.getAllForBackup().anyInAllowed        { it.vaultId } ||
            customEntryRepo.getAllForBackup().anyInAllowed       { it.vaultId } ||
            passkeyRepo.getAllForBackup().anyInAllowed           { it.vaultId } ||
            identityDocRepo.getAllForBackup().anyInAllowed       { it.vaultId } ||
            vehicleRepo.getAllForBackup().anyInAllowed           { it.vaultId }
    }

    private suspend fun serializeVaults(): String {
        val allowedIds = vaultMetadataRepo.observeAll().first()
            .filter { !it.isInherited && !it.isPrivate }
            .map { it.id }
            .toSet()

        fun <T : Any> List<T>.filterVaults(getId: (T) -> String) =
            filter { getId(it) in allowedIds }

        val passwords  = vaultRepo.getAllForBackup().filterVaults      { it.vaultId }
        val twoFactor  = twoFactorRepo.getAllForBackup().filterVaults  { it.vaultId }
        val cards      = creditCardRepo.getAllForBackup().filterVaults  { it.vaultId }
        val banks      = bankAccountRepo.getAllForBackup().filterVaults { it.vaultId }
        val crypto     = cryptoWalletRepo.getAllForBackup().filterVaults { it.vaultId }
        val notes      = secureNoteRepo.getAllForBackup().filterVaults  { it.vaultId }
        val custom     = customEntryRepo.getAllForBackup().filterVaults  { it.vaultId }
        val passkeys   = passkeyRepo.getAllForBackup().filterVaults     { it.vaultId }
        val identities = identityDocRepo.getAllForBackup().filterVaults { it.vaultId }
        val vehicles   = vehicleRepo.getAllForBackup().filterVaults     { it.vaultId }

        return ExportSerializer.toJson(
            passwords, twoFactor, cards, banks,
            crypto, notes, custom, passkeys, identities, vehicles
        )
    }

    private fun fetchUploadUrl(
        ownerEmail:   String,
        heirEmail:    String,
        deviceToken:  String,
        ownerIdToken: String? = null
    ): UploadTarget {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val idTokenField = if (ownerIdToken != null) ""","ownerIdToken":"${ownerIdToken.esc()}"""" else ""
        val body = """{"ownerEmail":"${ownerEmail.esc()}","heirEmail":"${heirEmail.esc()}","deviceToken":"${deviceToken.esc()}"$idTokenField}"""
        val conn = URL(UPLOAD_URL_ENDPOINT).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 15_000
        conn.readTimeout    = 15_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        if (code !in 200..299) {
            throw Exception("onGetInheritanceUploadUrl respondió HTTP $code")
        }
        val response = conn.inputStream.bufferedReader().readText()
        val json     = JSONObject(response)
        val headers  = mutableMapOf<String, String>()
        json.optJSONObject("requiredHeaders")?.let { req ->
            req.keys().forEach { k -> headers[k] = req.getString(k) }
        }
        return UploadTarget(url = json.getString("uploadUrl"), headers = headers)
    }

    private data class UploadTarget(val url: String, val headers: Map<String, String>)

    private fun markVaultSealed(
        ownerEmail:   String,
        heirEmail:    String,
        deviceToken:  String,
        ownerIdToken: String? = null
    ) {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val idTokenField = if (ownerIdToken != null) ""","ownerIdToken":"${ownerIdToken.esc()}"""" else ""
        val body = """{"ownerEmail":"${ownerEmail.esc()}","heirEmail":"${heirEmail.esc()}","driveFileId":"$V4_SEALED_MARKER","deviceToken":"${deviceToken.esc()}"$idTokenField}"""
        val conn = URL(STORE_FILE_ID_ENDPOINT).openConnection() as HttpURLConnection
        conn.requestMethod  = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 15_000
        conn.readTimeout    = 15_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        if (code !in 200..299) throw Exception("onStoreVaultFileId respondió HTTP $code")
    }

    private fun uploadToStorage(target: UploadTarget, payload: ByteArray) {
        val conn = URL(target.url).openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.setRequestProperty("Content-Type", "application/octet-stream")
        target.headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
        conn.connectTimeout = 20_000
        conn.readTimeout    = 40_000
        conn.doOutput       = true
        conn.outputStream.use { it.write(payload) }
        val code = conn.responseCode
        if (code !in 200..299) {
            throw Exception("Storage PUT respondió HTTP $code")
        }
    }

    private suspend fun tryGetOwnerIdToken(): String? =
        getFirebaseIdentityTokenOrNull(context)?.idToken
}

data class ReEncryptAllResult(
    val attempted: Boolean,
    val succeeded: List<String>,
    val failed:    List<HeirReEncryptFailure>
)

data class HeirReEncryptFailure(
    val heirEmail: String,
    val reason:    ReEncryptFailureReason
)

enum class ReEncryptFailureReason {
    LOCAL_KEY_LOST,

    UPLOAD_ERROR,

    SIGNING_KEY_LOST
}
