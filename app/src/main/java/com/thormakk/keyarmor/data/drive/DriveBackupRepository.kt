package com.thormakk.keyarmor.data.drive

import android.content.Context
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.export.ExportSerializer
import com.thormakk.keyarmor.data.prefs.SecurityPrefs
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveBackupRepository @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val vaultRepo:        VaultRepository,
    private val twoFactorRepo:    TwoFactorRepository,
    private val creditCardRepo:   CreditCardRepository,
    private val bankAccountRepo:  BankAccountRepository,
    private val cryptoRepo:       CryptoWalletRepository,
    private val secureNoteRepo:   SecureNoteRepository,
    private val customEntryRepo:  CustomEntryRepository,
    private val passkeyRepo:      PasskeyRepository,
    private val identityDocRepo:  IdentityDocumentRepository,
    private val vehicleRepo:      VehicleRepository,
    private val vaultMetadataRepo: VaultMetadataRepository,
    private val crypto:           CryptoManager,
    private val securityPrefs:    SecurityPrefs,
    private val heirIdentityManager: com.thormakk.keyarmor.security.HeirIdentityManager,
    private val ownerSigningIdentityManager: com.thormakk.keyarmor.security.OwnerSigningIdentityManager
) {

    private val context: Context get() = contextoDelIdioma(contextInyectado)
    companion object {
        private const val BACKUP_FILENAME = "keyarmor_backup.json"
        private const val MIME_JSON       = "application/json"
        private const val MIME_BACKUP     = "application/octet-stream"
    }

    suspend fun uploadBackupFile(account: android.accounts.Account, file: java.io.File): Long =
        withContext(Dispatchers.IO) {
            val drive: Drive = buildDriveService(context, account)
            val existing = findBackupFileId(drive)
            val content  = FileContent(MIME_BACKUP, file)

            try {
                if (existing != null) {
                    drive.files().update(existing, null, content).execute()
                } else {
                    val meta = File().apply {
                        name    = BACKUP_FILENAME
                        parents = listOf("appDataFolder")
                    }
                    drive.files().create(meta, content).execute()
                }
            } catch (e: com.google.api.client.googleapis.json.GoogleJsonResponseException) {
                if (e.details?.errors?.any { it.reason == "storageQuotaExceeded" } == true)
                    throw DriveStorageFullException(e)
                throw e
            }

            val now = System.currentTimeMillis()
            securityPrefs.markBackupDone()
            now
        }

    suspend fun downloadAndDecrypt(account: android.accounts.Account, password: String): String =
        withContext(Dispatchers.IO) {
            val drive   = buildDriveService(context, account)
            val fileId  = findBackupFileId(drive)
                ?: error(context.getString(R.string.drive_error_backup_not_found))

            checkDownloadSize(drive, fileId)

            val bytes   = drive.files().get(fileId)
                .executeMediaAsInputStream()
                .readBytes()
            val payload = JSONObject(String(bytes, Charsets.UTF_8))

            val salt    = crypto.base64ToSalt(payload.getString("salt"))
            val key     = deriveWrapperKey(payload, password, salt)
            crypto.decrypt(payload.getString("data"), key)
        }

    suspend fun downloadBackupToFile(
        account: android.accounts.Account,
        dir:     java.io.File
    ): java.io.File = withContext(Dispatchers.IO) {
        val drive  = buildDriveService(context, account)
        val fileId = findBackupFileId(drive)
            ?: error(context.getString(R.string.drive_error_backup_not_found))
        val dest = java.io.File.createTempFile("drive_restore_", ".bin", dir)
        try {
            drive.files().get(fileId).executeMediaAsInputStream().use { input ->
                dest.outputStream().use { out -> input.copyTo(out) }
            }
        } catch (e: Exception) {
            dest.delete()
            throw e
        }
        dest
    }

    suspend fun getCloudBackupTimestamp(account: android.accounts.Account): Long? =
        withContext(Dispatchers.IO) {
            val drive = buildDriveService(context, account)
            drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$BACKUP_FILENAME'")
                .setFields("files(modifiedTime)")
                .execute()
                .files
                .orEmpty()
                .firstOrNull()
                ?.modifiedTime
                ?.value
        }

    suspend fun uploadSnapshot(
        account:   android.accounts.Account,
        password:  String?,
        id:        String,
        trigger:   SnapshotTrigger,
        timestamp: Long,
        plainJson: String,
        clave:     ByteArray? = null
    ) = withContext(Dispatchers.IO) {
        val payload = if (clave != null) {
            buildKeyWrapper(clave, plainJson)
        } else {
            val pw        = password ?: error("NO_BACKUP_PASSWORD")
            val salt      = crypto.generateSalt()
            val key       = crypto.deriveKeyArgon2id(
                pw, salt,
                CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
                CryptoManager.ARGON2_DEFAULT_ITERATIONS,
                CryptoManager.ARGON2_DEFAULT_PARALLELISM
            )
            buildWrapper(salt, crypto.encrypt(plainJson, key))
        }

        val drive: Drive = buildDriveService(context, account)
        val filename = DriveSnapshotInfo.buildFilename(id, trigger, timestamp)
        val content  = ByteArrayContent(MIME_JSON, payload.toByteArray(Charsets.UTF_8))
        val meta     = File().apply {
            name    = filename
            parents = listOf("appDataFolder")
        }
        drive.files().create(meta, content).execute()
    }

    suspend fun reuploadSnapshot(
        account:     android.accounts.Account,
        driveFileId: String,
        password:    String,
        plainJson:   String
    ) = withContext(Dispatchers.IO) {
        val salt      = crypto.generateSalt()
        val key       = crypto.deriveKeyArgon2id(
            password, salt,
            CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
            CryptoManager.ARGON2_DEFAULT_ITERATIONS,
            CryptoManager.ARGON2_DEFAULT_PARALLELISM
        )
        val encrypted = crypto.encrypt(plainJson, key)
        val payload   = buildWrapper(salt, encrypted)

        val drive: Drive = buildDriveService(context, account)
        val content = ByteArrayContent(MIME_JSON, payload.toByteArray(Charsets.UTF_8))
        drive.files().update(driveFileId, null, content).execute()
    }

    suspend fun listDriveSnapshots(
        account: android.accounts.Account
    ): List<DriveSnapshotInfo> = withContext(Dispatchers.IO) {
        val drive: Drive = buildDriveService(context, account)
        drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name contains 'tm.' and name contains '.snap'")
            .setFields("files(id, name)")
            .execute()
            .files
            .orEmpty()
            .mapNotNull { file ->
                val (id, trigger, timestamp) =
                    DriveSnapshotInfo.parseFilename(file.name ?: "") ?: return@mapNotNull null
                DriveSnapshotInfo(
                    driveFileId = file.id,
                    snapshotId  = id,
                    trigger     = trigger,
                    timestamp   = timestamp
                )
            }
            .sortedByDescending { it.timestamp }
    }

    suspend fun downloadAndDecryptSnapshot(
        account:     android.accounts.Account,
        password:    String?,
        driveFileId: String,
        clave:       ByteArray? = null
    ): String = withContext(Dispatchers.IO) {
        val drive: Drive = buildDriveService(context, account)
        checkDownloadSize(drive, driveFileId)
        val bytes   = drive.files().get(driveFileId)
            .executeMediaAsInputStream()
            .readBytes()
        val payload = JSONObject(String(bytes, Charsets.UTF_8))
        if (payload.optString("mode") == "key") {
            val c = clave ?: error("NO_BACKUP_PASSWORD")
            val fileKey = crypto.unwrapKabFileKey(payload.getString("wrappedKey"), crypto.deriveKabWrapKey(c))
            return@withContext crypto.decrypt(payload.getString("data"), fileKey)
        }
        val pw      = password ?: error("NO_BACKUP_PASSWORD")
        val salt    = crypto.base64ToSalt(payload.getString("salt"))
        val key     = deriveWrapperKey(payload, pw, salt)
        crypto.decrypt(payload.getString("data"), key)
    }

    suspend fun rewrapSnapshots(
        account: android.accounts.Account,
        vieja:   ByteArray,
        nueva:   ByteArray
    ): RewrapResult = withContext(Dispatchers.IO) {
        val drive: Drive = buildDriveService(context, account)
        val wrapVieja = crypto.deriveKabWrapKey(vieja)
        val wrapNueva = crypto.deriveKabWrapKey(nueva)
        val files = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name contains 'tm.' and name contains '.snap'")
            .setFields("files(id, name)")
            .execute()
            .files
            .orEmpty()
        var actualizados = 0; var yaEstaban = 0; var omitidos = 0
        for (f in files) {
            checkDownloadSize(drive, f.id)
            val bytes   = drive.files().get(f.id).executeMediaAsInputStream().readBytes()
            val payload = try { JSONObject(String(bytes, Charsets.UTF_8)) } catch (_: Exception) { omitidos++; continue }
            if (payload.optString("mode") != "key") { omitidos++; continue }
            val wrapped = payload.optString("wrappedKey")
            val fileKey = runCatching { crypto.unwrapKabFileKey(wrapped, wrapVieja) }.getOrNull()
            if (fileKey == null) {
                if (runCatching { crypto.unwrapKabFileKey(wrapped, wrapNueva) }.isSuccess) yaEstaban++ else omitidos++
                continue
            }
            payload.put("wrappedKey", crypto.wrapKabFileKey(fileKey, wrapNueva))
            val content = ByteArrayContent(MIME_JSON, payload.toString().toByteArray(Charsets.UTF_8))
            drive.files().update(f.id, null, content).execute()
            actualizados++
        }
        RewrapResult(actualizados, yaEstaban, omitidos)
    }

    suspend fun deleteSnapshotFromDrive(
        account:     android.accounts.Account,
        driveFileId: String
    ) = withContext(Dispatchers.IO) {
        buildDriveService(context, account).files().delete(driveFileId).execute()
    }

    suspend fun deleteSnapshotFromDriveBySnapshotId(
        account:    android.accounts.Account,
        snapshotId: String
    ) = withContext(Dispatchers.IO) {
        val drive: Drive = buildDriveService(context, account)
        val files = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name contains 'tm.$snapshotId.'")
            .setFields("files(id)")
            .execute()
            .files
            .orEmpty()
        files.forEach { runCatching { drive.files().delete(it.id).execute() } }
    }

    suspend fun deleteBackup(account: android.accounts.Account): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val drive  = buildDriveService(context, account)
                val fileId = findBackupFileId(drive) ?: return@runCatching false
                drive.files().delete(fileId).execute()
                true
            }.getOrDefault(false)
        }

    private fun checkDownloadSize(drive: Drive, fileId: String) {
        val size = drive.files().get(fileId).setFields("size").execute().getSize()
        val max  = maxOf(Runtime.getRuntime().maxMemory() / 4, 32L * 1024 * 1024)
        if (size != null && size > max) {
            error(context.getString(
                R.string.import_error_file_too_large,
                size / (1024 * 1024), max / (1024 * 1024)
            ))
        }
    }

    private fun buildKeyWrapper(clave: ByteArray, plainJson: String): String {
        val fileKey = crypto.generateKabFileKey()
        return JSONObject().apply {
            put("version",    3)
            put("mode",       "key")
            put("wrappedKey", crypto.wrapKabFileKey(fileKey, crypto.deriveKabWrapKey(clave)))
            put("data",       crypto.encrypt(plainJson, fileKey))
        }.toString()
    }

    private fun buildWrapper(salt: ByteArray, encryptedData: String): String =
        JSONObject().apply {
            put("version",     2)
            put("kdf",         "argon2id")
            put("memoryKb",    CryptoManager.ARGON2_DEFAULT_MEMORY_KB)
            put("iterations",  CryptoManager.ARGON2_DEFAULT_ITERATIONS)
            put("parallelism", CryptoManager.ARGON2_DEFAULT_PARALLELISM)
            put("salt",        crypto.saltToBase64(salt))
            put("data",        encryptedData)
        }.toString()

    private fun deriveWrapperKey(wrapper: JSONObject, password: String, salt: ByteArray): SecretKeySpec =
        crypto.deriveKeyArgon2id(
            password, salt,
            wrapper.getInt("memoryKb"),
            wrapper.getInt("iterations"),
            wrapper.getInt("parallelism")
        )

    private fun findBackupFileId(drive: Drive): String? =
        drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILENAME'")
            .setFields("files(id)")
            .execute()
            .files
            .firstOrNull()
            ?.id
}

data class RewrapResult(val actualizados: Int, val yaEstaban: Int, val omitidos: Int)

class DriveStorageFullException(cause: Throwable) : java.io.IOException("drive_storage_full", cause)
