package com.thormakk.keyarmor.data.repository

import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import android.provider.OpenableColumns
import android.util.Log
import androidx.room.withTransaction
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.backup.KabAttachmentSource
import com.thormakk.keyarmor.data.backup.KabContainer
import com.thormakk.keyarmor.data.backup.KabContainerReader
import com.thormakk.keyarmor.data.backup.KabContainerWriter
import com.thormakk.keyarmor.data.backup.KabFormatException
import com.thormakk.keyarmor.data.backup.KabHeader
import com.thormakk.keyarmor.data.backup.KabKdf
import com.thormakk.keyarmor.data.database.KeyArmorDatabase
import com.thormakk.keyarmor.data.export.AttachmentBackupEntry
import com.thormakk.keyarmor.data.export.ExportEntryRef
import com.thormakk.keyarmor.data.export.ExportEntryType
import com.thormakk.keyarmor.data.export.ExportSerializer
import com.thormakk.keyarmor.data.export.ImportParser
import com.thormakk.keyarmor.data.export.ImportResult
import com.thormakk.keyarmor.data.export.PdfExportBlock
import com.thormakk.keyarmor.data.export.PdfExporter
import com.thormakk.keyarmor.data.export.PdfFieldEntry
import com.thormakk.keyarmor.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.util.UUID
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

data class ImportOutcome(val insertedEntries: Int, val restoredAttachments: Int, val skippedProtected: Int = 0)

data class PersistedEntries(val count: Int, val idMap: Map<String, String>, val skippedProtected: Int = 0)

enum class RestoreMode {
    REPLACE,
    MERGE,
    IMPORT_ALL
}

@Singleton
class ExportImportRepository @Inject constructor(
    private val db:                         KeyArmorDatabase,
    private val vaultRepository:            VaultRepository,
    private val twoFactorRepository:        TwoFactorRepository,
    private val creditCardRepository:       CreditCardRepository,
    private val bankAccountRepository:      BankAccountRepository,
    private val cryptoWalletRepository:     CryptoWalletRepository,
    private val secureNoteRepository:       SecureNoteRepository,
    private val customEntryRepository:      CustomEntryRepository,
    private val passkeyRepository:          PasskeyRepository,
    private val hiddenEntriesPrefs:         com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs,
    private val identityDocumentRepository: IdentityDocumentRepository,
    private val vehicleRepository:          VehicleRepository,
    private val vaultMetadataRepository:    VaultMetadataRepository,
    private val attachmentRepository:       AttachmentRepository,
    private val entryContentKey:            com.thormakk.keyarmor.data.export.EntryContentKey,
    private val cryptoManager:              CryptoManager,
    private val reminderRepository:         ReminderRepository,
    private val secureNoteHistoryRepository: SecureNoteHistoryRepository,
    private val tagRepository:              TagRepository,
    private val heirIdentityManager:        com.thormakk.keyarmor.security.HeirIdentityManager,
    private val inheritancePrefs:           com.thormakk.keyarmor.data.prefs.InheritancePrefs,
    private val ownerSigningIdentityManager: com.thormakk.keyarmor.security.OwnerSigningIdentityManager,
    private val drivePrefs:                 com.thormakk.keyarmor.data.prefs.DrivePrefs,
    private val pinnedHubPrefs:             com.thormakk.keyarmor.data.prefs.PinnedHubPrefs,
    private val snapshotRepository:         SnapshotRepository
) {
    companion object {
        private const val LOCAL_BACKUP_FILENAME = "keyarmor_drive.json"
        private const val LOCAL_BACKUP_FILENAME_KAB = "keyarmor_drive.kab"

        private val CARPETA_EN_DESCARGAS = Environment.DIRECTORY_DOWNLOADS + "/KeyArmor"
    }

    suspend fun exportJson(password: String, uri: Uri, context: Context) =
        withContext(Dispatchers.IO) {
            val json      = gatherAllData()
            val salt      = cryptoManager.generateSalt()
            val key       = cryptoManager.deriveKeyArgon2id(
                password, salt,
                CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
                CryptoManager.ARGON2_DEFAULT_ITERATIONS,
                CryptoManager.ARGON2_DEFAULT_PARALLELISM
            )
            val encrypted = cryptoManager.encrypt(json, key)
            val wrapper   = buildWrapper(salt, encrypted)
            context.contentResolver.openOutputStream(uri)
                ?.use { it.write(wrapper.toByteArray(Charsets.UTF_8)) }
        }

    suspend fun exportManual(
        password: String,
        uri: Uri,
        includeAttachments: Boolean,
        context: Context,
        clave: ByteArray? = null
    ) = withContext(Dispatchers.IO) {
        if (includeAttachments || clave != null) {
            writeKabPasswordMode(password, uri, context, includeAttachments, clave)
        } else {
            exportJson(password, uri, context)
        }
    }

    suspend fun attachmentCount(): Int = withContext(Dispatchers.IO) {
        attachmentRepository.getAllForBackup().size +
            identityDocumentRepository.photosForBackup().size
    }

    suspend fun attachmentsTotalBytes(): Long = withContext(Dispatchers.IO) {
        attachmentRepository.getAllForBackup().sumOf { it.fileSize } +
            identityDocumentRepository.photosForBackup().sumOf {
                runCatching { java.io.File(it.storedPath).length() }.getOrDefault(0L)
            }
    }

    private suspend fun writeKabPasswordMode(
        password: String, uri: Uri, context: Context,
        includeAttachments: Boolean = true, clave: ByteArray? = null
    ) {
        val vaultJson = gatherAllData(includeAttachments = false)
        val sources   = if (includeAttachments) {
            attachmentRepository.getAttachmentBackupSources() +
                identityDocumentRepository.photoBackupSources()
        } else emptyList()
        context.contentResolver.openOutputStream(uri)?.use { out -> writeKabToStream(out, password, vaultJson, sources, clave) }
            ?: error(context.getString(R.string.import_export_error_reading_file))
    }

    private fun writeKabToStream(
        out: OutputStream,
        password: String,
        vaultJson: String,
        sources: List<AttachmentBackupSource>,
        clave: ByteArray? = null,
    ) {
        if (clave != null) {
            val fileKey = cryptoManager.generateKabFileKey()
            val wrapped = cryptoManager.wrapKabFileKey(fileKey, cryptoManager.deriveKabWrapKey(clave))
            val header  = KabHeader(
                mode = KabContainer.MODE_KEY,
                attachmentCount = sources.size,
                kdf = null,
                wrappedKey = wrapped,
            )
            writeKabCore(out, header, fileKey, vaultJson, sources)
            return
        }
        val salt      = cryptoManager.generateSalt()
        val key       = cryptoManager.deriveKeyArgon2id(
            password, salt,
            CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
            CryptoManager.ARGON2_DEFAULT_ITERATIONS,
            CryptoManager.ARGON2_DEFAULT_PARALLELISM
        )
        val header = KabHeader(
            mode = KabContainer.MODE_PASSWORD,
            attachmentCount = sources.size,
            kdf = KabKdf(
                memoryKb    = CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
                iterations  = CryptoManager.ARGON2_DEFAULT_ITERATIONS,
                parallelism = CryptoManager.ARGON2_DEFAULT_PARALLELISM,
                saltB64     = cryptoManager.saltToBase64(salt),
            ),
        )
        writeKabCore(out, header, key, vaultJson, sources)
    }

    private fun writeKabCore(
        out: OutputStream,
        header: KabHeader,
        key: SecretKeySpec,
        vaultJson: String,
        sources: List<AttachmentBackupSource>,
    ) {
        val manifestJson = JSONArray().apply {
            sources.forEach { s ->
                put(JSONObject().apply {
                    put("id",        s.id)
                    put("entryId",   s.entryId)
                    put("fileName",  s.fileName)
                    put("mimeType",  s.mimeType)
                    put("fileSize",  s.fileSize)
                    put("createdAt", s.createdAt)
                    put("kind",      s.kind)
                })
            }
        }.toString()
        val attachmentSeq = sources.asSequence().map { s ->
            KabAttachmentSource(id = s.id) {
                s.plainContent()?.let { cryptoManager.encryptBytes(it, key) }
            }
        }
        KabContainerWriter(cryptoManager).write(
            out = out,
            header = header,
            key = key,
            vaultJson = vaultJson,
            manifestJson = manifestJson,
            attachments = attachmentSeq,
        )
    }

    suspend fun exportCsv(uri: Uri, context: Context) = withContext(Dispatchers.IO) {
        val passwords = vaultRepository.observeEntries().first().filter { !it.isDeleted }
        val csv       = ExportSerializer.toCsv(passwords)
        context.contentResolver.openOutputStream(uri)
            ?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
    }

    private fun Set<ExportEntryRef>.idsByType(): Map<ExportEntryType, Set<String>> =
        groupBy({ it.type }, { it.id }).mapValues { it.value.toSet() }

    suspend fun exportPartialCsv(refs: Set<ExportEntryRef>, uri: Uri, context: Context) =
        withContext(Dispatchers.IO) {
            val ids       = refs.idsByType()[ExportEntryType.PASSWORD].orEmpty()
            val passwords = vaultRepository.getAllForBackup().filter { it.id in ids && !it.isHidden }
            val csv       = ExportSerializer.toCsv(passwords)
            context.contentResolver.openOutputStream(uri)
                ?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
        }

    suspend fun exportPartialJson(refs: Set<ExportEntryRef>, password: String, uri: Uri, context: Context) =
        withContext(Dispatchers.IO) {
            val json      = buildPartialJson(refs.idsByType())
            val salt      = cryptoManager.generateSalt()
            val key       = cryptoManager.deriveKeyArgon2id(
                password, salt,
                CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
                CryptoManager.ARGON2_DEFAULT_ITERATIONS,
                CryptoManager.ARGON2_DEFAULT_PARALLELISM
            )
            val encrypted = cryptoManager.encrypt(json, key)
            val wrapper   = buildWrapper(salt, encrypted)
            context.contentResolver.openOutputStream(uri)
                ?.use { it.write(wrapper.toByteArray(Charsets.UTF_8)) }
        }

    suspend fun exportPartialManual(
        refs: Set<ExportEntryRef>,
        password: String,
        uri: Uri,
        includeAttachments: Boolean,
        context: Context,
        clave: ByteArray? = null
    ) = withContext(Dispatchers.IO) {
        if (includeAttachments || clave != null) {
            val vaultJson   = buildPartialJson(refs.idsByType())
            val selectedIds = refs.map { it.id }.toSet()
            val sources     = if (includeAttachments) {
                (attachmentRepository.getAttachmentBackupSources() +
                    identityDocumentRepository.photoBackupSources())
                    .filter { it.entryId in selectedIds }
            } else emptyList()
            context.contentResolver.openOutputStream(uri)?.use { writeKabToStream(it, password, vaultJson, sources, clave) }
                ?: error(context.getString(R.string.import_export_error_reading_file))
        } else {
            exportPartialJson(refs, password, uri, context)
        }
    }

    suspend fun partialAttachmentInfo(refs: Set<ExportEntryRef>): Pair<Int, Long> =
        withContext(Dispatchers.IO) {
            val selectedIds = refs.map { it.id }.toSet()
            val atts = attachmentRepository.getAllForBackup().filter { it.entryId in selectedIds }
            atts.size to atts.sumOf { it.fileSize }
        }

    private suspend fun buildPartialJson(byType: Map<ExportEntryType, Set<String>>): String =
        ExportSerializer.toJson(
            passwords         = vaultRepository.getAllForBackup().filter { it.id in byType[ExportEntryType.PASSWORD].orEmpty() && !it.isHidden },
            twoFactor         = twoFactorRepository.getAllForBackup().filter { it.id in byType[ExportEntryType.TWO_FACTOR].orEmpty() && !it.isHidden },
            creditCards       = creditCardRepository.getAllForBackup().filter { it.id in byType[ExportEntryType.CREDIT_CARD].orEmpty() && !it.isHidden },
            bankAccounts      = bankAccountRepository.getAllForBackup().filter { it.id in byType[ExportEntryType.BANK_ACCOUNT].orEmpty() && !it.isHidden },
            cryptoWallets     = cryptoWalletRepository.getAllForBackup().filter { it.id in byType[ExportEntryType.CRYPTO_WALLET].orEmpty() && !it.isHidden },
            secureNotes       = secureNoteRepository.getAllForBackup().filter { it.id in byType[ExportEntryType.SECURE_NOTE].orEmpty() && !it.isHidden },
            customEntries     = customEntryRepository.getAllForBackup().filter { it.id in byType[ExportEntryType.CUSTOM_ENTRY].orEmpty() && !it.isHidden },
            passkeys          = passkeyRepository.getAllForBackup().filter { it.credentialId in byType[ExportEntryType.PASSKEY].orEmpty() && !it.isHidden },
            identityDocuments = identityDocumentRepository.getAllForBackup().filter { it.id in byType[ExportEntryType.IDENTITY_DOCUMENT].orEmpty() && !it.isHidden },
            vehicles          = vehicleRepository.getAllForBackup().filter { it.id in byType[ExportEntryType.VEHICLE].orEmpty() && !it.isHidden }
        )

    suspend fun exportPartialPdf(
        refs:                Set<ExportEntryRef>,
        password:            String,
        showSensitiveValues: Boolean,
        uri:                 Uri,
        context:             Context
    ) = withContext(Dispatchers.IO) {
        val byType = refs.idsByType()
        val blocks = mutableListOf<PdfExportBlock>()

        byType[ExportEntryType.PASSWORD]?.let { ids ->
            vaultRepository.getAllForBackup().filter { it.id in ids && !it.isHidden }.forEach { e ->
                blocks += PdfExportBlock("Contraseña", e.title, listOf(
                    PdfFieldEntry("Usuario",    e.username),
                    PdfFieldEntry("Email",      e.email),
                    PdfFieldEntry("Contraseña", e.password, sensitive = true),
                    PdfFieldEntry("URL",        e.url),
                    PdfFieldEntry("Notas",      e.notes),
                    PdfFieldEntry("Etiquetas",  e.tags.joinToString(", "))
                ))
            }
        }
        byType[ExportEntryType.TWO_FACTOR]?.let { ids ->
            twoFactorRepository.getAllForBackup().filter { it.id in ids && !it.isHidden }.forEach { e ->
                blocks += PdfExportBlock("Verificación en dos pasos", e.name, listOf(
                    PdfFieldEntry("Emisor",           e.issuer),
                    PdfFieldEntry("Cuenta",           e.accountLabel),
                    PdfFieldEntry("Secreto TOTP",     e.secret, sensitive = true),
                    PdfFieldEntry(
                        "Códigos de recuperación",
                        e.recoveryCodes.joinToString(", ") { it.code },
                        sensitive = true
                    ),
                    PdfFieldEntry("Notas", e.notes)
                ))
            }
        }
        byType[ExportEntryType.CREDIT_CARD]?.let { ids ->
            creditCardRepository.getAllForBackup().filter { it.id in ids && !it.isHidden }.forEach { e ->
                blocks += PdfExportBlock("Tarjeta", "${e.issuer} ${e.brand}".trim(), listOf(
                    PdfFieldEntry("Titular",  e.cardholder),
                    PdfFieldEntry("Número",   e.cardNumber, sensitive = true),
                    PdfFieldEntry("Vencimiento", e.expiryDate),
                    PdfFieldEntry("CVV",      e.cvv, sensitive = true),
                    PdfFieldEntry("Teléfono del banco", e.bankPhone)
                ))
            }
        }
        byType[ExportEntryType.BANK_ACCOUNT]?.let { ids ->
            bankAccountRepository.getAllForBackup().filter { it.id in ids && !it.isHidden }.forEach { e ->
                blocks += PdfExportBlock("Cuenta bancaria", e.bankName, listOf(
                    PdfFieldEntry("Tipo de cuenta", e.accountType),
                    PdfFieldEntry("Número de cuenta", e.accountNumber, sensitive = true),
                    PdfFieldEntry("CBU/IBAN", e.cbuIban, sensitive = true),
                    PdfFieldEntry("Alias",    e.alias),
                    PdfFieldEntry("SWIFT/BIC", e.swiftBic),
                    PdfFieldEntry("Notas",    e.notes)
                ))
            }
        }
        byType[ExportEntryType.CRYPTO_WALLET]?.let { ids ->
            cryptoWalletRepository.getAllForBackup().filter { it.id in ids && !it.isHidden }.forEach { e ->
                blocks += PdfExportBlock("Billetera cripto", e.asset.ifBlank { e.network }, listOf(
                    PdfFieldEntry("Red",             e.network),
                    PdfFieldEntry("Dirección",       e.address),
                    PdfFieldEntry("Frase semilla",   e.seedPhrase, sensitive = true),
                    PdfFieldEntry("Clave privada",   e.privateKey, sensitive = true),
                    PdfFieldEntry("Saldo manual",    e.manualBalance),
                    PdfFieldEntry("Notas",           e.notes)
                ))
            }
        }
        byType[ExportEntryType.SECURE_NOTE]?.let { ids ->
            secureNoteRepository.getAllForBackup().filter { it.id in ids && !it.isHidden }.forEach { e ->
                blocks += PdfExportBlock("Nota segura", e.title, listOf(
                    PdfFieldEntry("Contenido", e.content)
                ))
            }
        }
        byType[ExportEntryType.CUSTOM_ENTRY]?.let { ids ->
            customEntryRepository.getAllForBackup().filter { it.id in ids && !it.isHidden }.forEach { e ->
                blocks += PdfExportBlock(
                    "Entrada personalizada", e.title,
                    e.fields.map { f -> PdfFieldEntry(f.label, f.value, sensitive = f.type.isSecret) }
                )
            }
        }
        byType[ExportEntryType.PASSKEY]?.let { ids ->
            passkeyRepository.getAllForBackup().filter { it.credentialId in ids && !it.isHidden }.forEach { e ->
                blocks += PdfExportBlock("Passkey", e.rpName, listOf(
                    PdfFieldEntry("Sitio (RP ID)",   e.rpId),
                    PdfFieldEntry("Usuario",         e.userName),
                    PdfFieldEntry("Nombre visible",  e.userDisplayName),
                    PdfFieldEntry("Notas",           e.notes)
                ))
            }
        }
        byType[ExportEntryType.IDENTITY_DOCUMENT]?.let { ids ->
            identityDocumentRepository.getAllForBackup().filter { it.id in ids && !it.isHidden }.forEach { e ->
                blocks += PdfExportBlock("Documento de identidad", e.holderName.ifBlank { e.docType }, listOf(
                    PdfFieldEntry("Tipo",        e.docType),
                    PdfFieldEntry("Número",      e.number, sensitive = true),
                    PdfFieldEntry("País emisor", e.issuingCountry),
                    PdfFieldEntry("Vencimiento", e.expiryDate),
                    PdfFieldEntry("Notas",       e.notes)
                ))
            }
        }
        byType[ExportEntryType.VEHICLE]?.let { ids ->
            vehicleRepository.getAllForBackup().filter { it.id in ids && !it.isHidden }.forEach { e ->
                blocks += PdfExportBlock("Vehículo", "${e.make} ${e.model}".trim(), listOf(
                    PdfFieldEntry("Año",             e.year),
                    PdfFieldEntry("Patente",         e.licensePlate),
                    PdfFieldEntry("VIN",             e.vin, sensitive = true),
                    PdfFieldEntry("Aseguradora",     e.insuranceCompany),
                    PdfFieldEntry("N° de póliza",    e.policyNumber, sensitive = true),
                    PdfFieldEntry("Notas",           e.notes)
                ))
            }
        }

        PdfExporter.writePdf(blocks, showSensitiveValues, password, uri, context)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun filaEnDescargas(context: Context, fileName: String): Pair<Long, Long>? =
        context.contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DATE_MODIFIED,
                MediaStore.Downloads.RELATIVE_PATH,
            ),
            "${MediaStore.Downloads.DISPLAY_NAME} = ?",
            arrayOf(fileName),
            null,
        )?.use { c ->
            val colId = c.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            val colFecha = c.getColumnIndexOrThrow(MediaStore.Downloads.DATE_MODIFIED)
            val colRuta = c.getColumnIndexOrThrow(MediaStore.Downloads.RELATIVE_PATH)
            var encontrado: Pair<Long, Long>? = null
            while (encontrado == null && c.moveToNext()) {
                val donde = c.getString(colRuta).orEmpty().trim('/')
                if (donde.equals(CARPETA_EN_DESCARGAS.trim('/'), ignoreCase = true)) {
                    encontrado = c.getLong(colId) to c.getLong(colFecha)
                }
            }
            encontrado
        }

    suspend fun buildKabBackupFile(password: String, context: Context, clave: ByteArray? = null): java.io.File =
        withContext(Dispatchers.IO) {
            val vaultJson = gatherAllData(includeAttachments = false)
            val sources   = attachmentRepository.getAttachmentBackupSources() +
                            identityDocumentRepository.photoBackupSources()
            val dir  = java.io.File(context.cacheDir, "drive_backup").also { it.mkdirs() }
            val file = java.io.File.createTempFile("backup_", ".kab", dir)
            try {
                file.outputStream().use { writeKabToStream(it, password, vaultJson, sources, clave) }
            } catch (e: Exception) {
                file.delete()
                throw e
            }
            file
        }

    suspend fun exportJsonToDownloads(password: String, context: Context, clave: ByteArray? = null): String =
        withContext(Dispatchers.IO) {
            val vaultJson = gatherAllData(includeAttachments = false)
            val sources   = attachmentRepository.getAttachmentBackupSources() +
                            identityDocumentRepository.photoBackupSources()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val existingUri = filaEnDescargas(context, LOCAL_BACKUP_FILENAME_KAB)?.let { (id, _) ->
                    ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                }
                val uri = existingUri ?: run {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, LOCAL_BACKUP_FILENAME_KAB)
                        put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                        put(MediaStore.Downloads.RELATIVE_PATH, CARPETA_EN_DESCARGAS)
                    }
                    resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: error(context.getString(R.string.export_error_creating_downloads_file))
                }
                resolver.openOutputStream(uri, "wt")?.use { writeKabToStream(it, password, vaultJson, sources, clave) }
                deleteLegacyDownloadsJson(context)
            } else {
                val dir = java.io.File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "KeyArmor"
                ).also { it.mkdirs() }
                java.io.File(dir, LOCAL_BACKUP_FILENAME_KAB).outputStream().use { writeKabToStream(it, password, vaultJson, sources, clave) }
                java.io.File(dir, LOCAL_BACKUP_FILENAME).takeIf { it.exists() }?.delete()
            }
            LOCAL_BACKUP_FILENAME_KAB
        }

    private fun deleteLegacyDownloadsJson(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        runCatching {
            filaEnDescargas(context, LOCAL_BACKUP_FILENAME)?.let { (id, _) ->
                context.contentResolver.delete(
                    ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id), null, null
                )
            }
        }
    }

    data class LocalBackupInfo(val uri: Uri, val timestampMs: Long, val fileName: String)

    suspend fun findLatestDownloadsBackup(context: Context): LocalBackupInfo? =
        withContext(Dispatchers.IO) {
            findDownloadsBackupByName(context, LOCAL_BACKUP_FILENAME_KAB)
                ?: findDownloadsBackupByName(context, LOCAL_BACKUP_FILENAME)
        }

    private fun findDownloadsBackupByName(context: Context, fileName: String): LocalBackupInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            filaEnDescargas(context, fileName)?.let { (id, modifiedSec) ->
                LocalBackupInfo(
                    ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id),
                    modifiedSec * 1000L, fileName
                )
            }
        } else {
            val file = java.io.File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "KeyArmor/$fileName"
            )
            if (file.exists()) LocalBackupInfo(Uri.fromFile(file), file.lastModified(), fileName) else null
        }

    suspend fun deleteLocalDownloadsBackup(context: Context): Int = withContext(Dispatchers.IO) {
        val nombres = listOf(LOCAL_BACKUP_FILENAME_KAB, LOCAL_BACKUP_FILENAME)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            nombres.count { nombre ->
                runCatching {
                    filaEnDescargas(context, nombre)?.let { (id, _) ->
                        context.contentResolver.delete(
                            ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id), null, null
                        )
                    } ?: 0
                }.getOrDefault(0) > 0
            }
        } else {
            val dir = java.io.File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "KeyArmor"
            )
            nombres.count { nombre ->
                runCatching { java.io.File(dir, nombre).delete() }.getOrDefault(false)
            }
        }
    }

    suspend fun restoreLocalBackup(
        uri: Uri, password: String, context: Context, mode: RestoreMode, clave: ByteArray? = null
    ): Pair<ImportResult, ImportOutcome> = withContext(Dispatchers.IO) {
        val preview = importKeyArmor(password, uri, context, clave)
        val outcome = if (preview.isKabContainer) persistKabImport(uri, password, context, preview, mode, clave = clave)
                      else persistImport(preview, mode)
        preview to outcome
    }

    private fun contentSize(uri: Uri, context: Context): Long {
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { c ->
                    val idx = c.getColumnIndex(OpenableColumns.SIZE)
                    if (c.moveToFirst() && idx >= 0 && !c.isNull(idx)) c.getLong(idx) else -1L
                }
        }.getOrNull()?.takeIf { it > 0L }?.let { return it }
        return runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
        }.getOrNull()?.takeIf { it > 0L } ?: -1L
    }

    private fun maxImportBytes(): Long =
        maxOf(Runtime.getRuntime().maxMemory() / 4, 32L * 1024 * 1024)

    private fun checkImportSize(context: Context, sizeBytes: Long) {
        val max = maxImportBytes()
        if (sizeBytes > 0L && sizeBytes > max) {
            error(context.getString(
                R.string.import_error_file_too_large,
                sizeBytes / (1024 * 1024), max / (1024 * 1024)
            ))
        }
    }

    private fun checkImportSize(uri: Uri, context: Context) =
        checkImportSize(context, contentSize(uri, context))

    suspend fun importKeyArmor(password: String, uri: Uri, context: Context, clave: ByteArray? = null): ImportResult =
        withContext(Dispatchers.IO) {
            if (isKabContainer(uri, context)) {
                return@withContext previewKabImport(uri, password, context, clave)
            }
            checkImportSize(uri, context)
            val content = context.contentResolver.openInputStream(uri)
                ?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: error(context.getString(R.string.import_export_error_reading_file))
            if (!content.trimStart().startsWith("{")) {
                ImportParser.parseGenericCsv(content)
            } else {
                val wrapper = JSONObject(content)
                val salt    = cryptoManager.base64ToSalt(wrapper.getString("salt"))
                val key     = deriveWrapperKey(wrapper, password, salt)
                val json    = cryptoManager.decrypt(wrapper.getString("data"), key)
                ImportParser.parseKeyArmorJson(json)
            }
        }

    private fun isKabContainer(uri: Uri, context: Context): Boolean =
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val head = ByteArray(4)
                val n = input.read(head)
                n == 4 && KabContainer.looksLikeKab(head)
            } ?: false
        }.getOrDefault(false)

    private fun deriveKabKey(header: KabHeader, password: String, clave: ByteArray? = null): SecretKeySpec {
        if (header.mode == KabContainer.MODE_KEY) {
            if (clave == null) throw KabFormatException("kab_needs_key")
            val wrapped = header.wrappedKey ?: throw KabFormatException("kab_no_wrapped_key")
            return try {
                cryptoManager.unwrapKabFileKey(wrapped, cryptoManager.deriveKabWrapKey(clave))
            } catch (e: Exception) {
                throw KabFormatException("kab_wrong_key")
            }
        }
        val kdf = header.kdf ?: throw KabFormatException("kab_no_kdf")
        return cryptoManager.deriveKeyArgon2id(
            password, cryptoManager.base64ToSalt(kdf.saltB64),
            kdf.memoryKb, kdf.iterations, kdf.parallelism
        )
    }

    private suspend fun previewKabImport(uri: Uri, password: String, context: Context, clave: ByteArray? = null): ImportResult {
        val (header, vaultJson) = context.contentResolver.openInputStream(uri)?.use { input ->
            KabContainerReader(cryptoManager).readHeaderAndVault(input) { h -> deriveKabKey(h, password, clave) }
        } ?: error(context.getString(R.string.import_export_error_reading_file))
        return ImportParser.parseKeyArmorJson(vaultJson)
            .copy(attachmentCount = header.attachmentCount, isKabContainer = true)
    }

    private suspend fun crearPuntoPrevio(result: ImportResult, mode: RestoreMode, origen: SnapshotTrigger) {
        if (mode == RestoreMode.MERGE && !hayEntradasNuevas(result, origen == SnapshotTrigger.PRE_IMPORT_CSV)) return
        snapshotRepository.createSnapshotBestEffort(origen)
    }

    private fun claveCsv(e: com.thormakk.keyarmor.domain.model.VaultEntry): List<String> =
        listOf(e.title, e.username, e.password, e.urls.firstOrNull().orEmpty())

    private suspend fun hayEntradasNuevas(r: ImportResult, esCsv: Boolean): Boolean {
        fun <T> alguna(items: List<T>, existentes: List<T>, id: (T) -> String, clave: (T) -> Any): Boolean {
            val ids    = existentes.map(id).toSet()
            val claves = existentes.map(clave).toSet()
            return items.any { id(it) !in ids && clave(it) !in claves }
        }
        val passwords = vaultRepository.getAllForBackup()
        val hayPasswordNueva =
            if (esCsv) { val claves = passwords.map { claveCsv(it) }.toSet(); r.passwords.any { claveCsv(it) !in claves } }
            else       alguna(r.passwords, passwords, { it.id }, { entryContentKey.of(it) })
        return hayPasswordNueva ||
            alguna(r.twoFactor,        twoFactorRepository.getAllForBackup(),        { it.id }, { entryContentKey.of(it) }) ||
            alguna(r.passkeys,         passkeyRepository.getAllForBackup(),          { it.credentialId }, { entryContentKey.of(it) }) ||
            alguna(r.bankAccounts,     bankAccountRepository.getAllForBackup(),      { it.id }, { entryContentKey.of(it) }) ||
            alguna(r.creditCards,      creditCardRepository.getAllForBackup(),       { it.id }, { entryContentKey.of(it) }) ||
            alguna(r.secureNotes,      secureNoteRepository.getAllForBackup(),       { it.id }, { entryContentKey.of(it) }) ||
            alguna(r.customEntries,    customEntryRepository.getAllForBackup(),      { it.id }, { entryContentKey.of(it) }) ||
            alguna(r.identityDocuments, identityDocumentRepository.getAllForBackup(), { it.id }, { entryContentKey.of(it) }) ||
            alguna(r.vehicles,         vehicleRepository.getAllForBackup(),          { it.id }, { entryContentKey.of(it) }) ||
            alguna(r.cryptoWallets,    cryptoWalletRepository.getAllForBackup(),     { it.id }, { entryContentKey.of(it) })
    }

    suspend fun persistKabImport(
        uri: Uri,
        password: String,
        context: Context,
        preview: ImportResult,
        mode: RestoreMode = RestoreMode.MERGE,
        vaultId: String? = null,
        clave: ByteArray? = null,
    ): ImportOutcome = withContext(Dispatchers.IO) {
        crearPuntoPrevio(preview, mode, SnapshotTrigger.PRE_IMPORT_KAB)
        val persisted = persistEntries(preview, mode, vaultId)
        desocultarSiNoHayFrase()
        val (insertedCount, entryIdMap) = persisted
        restorePinned(preview, mode, entryIdMap)

        val existingAttachmentIds =
            if (mode == RestoreMode.MERGE) attachmentRepository.getAllForBackup().map { it.id }.toSet()
            else emptySet()
        val manifest = HashMap<String, JSONObject>()
        var key: SecretKeySpec? = null
        var restoredAttachments = 0

        context.contentResolver.openInputStream(uri)?.use { input ->
            KabContainerReader(cryptoManager).read(
                input = input,
                keyProvider = { h -> deriveKabKey(h, password, clave).also { key = it } },
                onVault = {   },
                onManifest = { json ->
                    val arr = JSONArray(json)
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        manifest[o.optString("id")] = o
                    }
                },
                onAttachment = { id, storedBytes ->
                    val meta = manifest[id]
                    val k = key
                    val dup = mode == RestoreMode.MERGE && id in existingAttachmentIds
                    if (meta != null && k != null && !dup) {
                        val origEntryId = meta.optString("entryId")
                        val targetEntryId =
                            if (mode == RestoreMode.MERGE) origEntryId else entryIdMap[origEntryId]
                        val plain = runCatching { cryptoManager.decryptBytes(storedBytes, k) }.getOrNull()
                        val kind  = meta.optString("kind", "FILE")
                        if (targetEntryId != null && plain != null && kind != "FILE") {
                            runCatching {
                                identityDocumentRepository.restoreDecryptedPhoto(
                                    documentId = targetEntryId,
                                    isFront    = kind == "ID_PHOTO_FRONT",
                                    plainBytes = plain
                                )
                            }.onFailure {
                                Log.w("ExportImportRepository", "persistKabImport: foto $id omitida al restaurar", it)
                            }
                        } else if (targetEntryId != null && plain != null) {
                            runCatching {
                                if (attachmentRepository.restoreDecryptedAttachment(
                                        id            = if (mode == RestoreMode.MERGE) id else UUID.randomUUID().toString(),
                                        entryId       = targetEntryId,
                                        plainFileName = meta.optString("fileName"),
                                        plainMimeType = meta.optString("mimeType", "application/octet-stream"),
                                        fileSize      = meta.optLong("fileSize", plain.size.toLong()),
                                        createdAt     = meta.optLong("createdAt", System.currentTimeMillis()),
                                        plainBytes    = plain
                                    )) restoredAttachments++
                            }.onFailure {
                                Log.w("ExportImportRepository", "persistKabImport: adjunto $id omitido al restaurar", it)
                            }
                        }
                    }
                },
            )
        } ?: error(context.getString(R.string.import_export_error_reading_file))

        restoreHeirIdentityIfPresent(preview)
        val recordatoriosOmitidos = restoreRemindersAndNoteHistory(preview)
        tagRepository.reconcileAfterRestore()
        vaultMetadataRepository.healSystemVaultFlags()
        repararEntradasHuerfanas()
        ImportOutcome(insertedCount, restoredAttachments, persisted.skippedProtected + recordatoriosOmitidos)
    }

    suspend fun importGenericCsv(uri: Uri, context: Context): ImportResult =
        withContext(Dispatchers.IO) {
            checkImportSize(uri, context)
            val csv = context.contentResolver.openInputStream(uri)
                ?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: error(context.getString(R.string.import_export_error_reading_file))
            ImportParser.parseGenericCsv(csv)
        }

    suspend fun persistImport(
        result: ImportResult,
        mode:   RestoreMode = RestoreMode.MERGE,
        vaultId: String? = null,
        esCsv:  Boolean = false
    ): ImportOutcome = withContext(Dispatchers.IO) {
        crearPuntoPrevio(result, mode, if (esCsv) SnapshotTrigger.PRE_IMPORT_CSV else SnapshotTrigger.PRE_IMPORT_JSON)
        val persisted = persistEntries(result, mode, vaultId, esCsv)
        desocultarSiNoHayFrase()
        val (insertedCount, entryIdMap) = persisted
        restorePinned(result, mode, entryIdMap)
        val restoredAttachments = restoreAttachmentsInMemory(result.attachments, mode, entryIdMap)
        restoreHeirIdentityIfPresent(result)
        val recordatoriosOmitidos = restoreRemindersAndNoteHistory(result)
        tagRepository.reconcileAfterRestore()
        vaultMetadataRepository.healSystemVaultFlags()
        repararEntradasHuerfanas()
        ImportOutcome(insertedCount, restoredAttachments, persisted.skippedProtected + recordatoriosOmitidos)
    }

    private suspend fun repararEntradasHuerfanas() {
        val reasignadas = runCatching { vaultMetadataRepository.reasignarEntradasHuerfanas() }
            .getOrElse {
                android.util.Log.e("ExportImportRepository", "No se pudieron reasignar las huérfanas", it)
                0
            }
        if (reasignadas > 0) {
            android.util.Log.w(
                "ExportImportRepository",
                "$reasignadas entradas sin bóveda reasignadas al panel principal"
            )
        }
    }

    private fun restorePinned(result: ImportResult, mode: RestoreMode, entryIdMap: Map<String, String>) {
        val refs = result.pinned ?: return
        runCatching {
            pinnedHubPrefs.restoreFromBackup(refs, entryIdMap, completar = mode != RestoreMode.REPLACE)
        }
            .onFailure { Log.w("ExportImportRepository", "No se pudieron restaurar los accesos rápidos", it) }
    }

    private suspend fun restoreRemindersAndNoteHistory(result: ImportResult): Int {
        val omitidos = runCatching { reminderRepository.restoreFromBackup(result.reminders) }
            .onFailure { android.util.Log.e("Restore", "No se pudieron restaurar los recordatorios", it) }
            .getOrDefault(0)
        runCatching { secureNoteHistoryRepository.restoreFromBackup(result.secureNoteHistory) }
            .onFailure { android.util.Log.e("Restore", "No se pudo restaurar el historial de notas", it) }
        runCatching { restoreTags(result) }
            .onFailure { android.util.Log.e("Restore", "No se pudo restaurar el pool de etiquetas", it) }
        return omitidos
    }

    private suspend fun restoreTags(result: ImportResult) {
        val usadas: Set<String> = buildSet {
            result.passwords.forEach         { addAll(it.tags) }
            result.twoFactor.forEach         { addAll(it.tags) }
            result.creditCards.forEach       { addAll(it.tags) }
            result.bankAccounts.forEach      { addAll(it.tags) }
            result.cryptoWallets.forEach     { addAll(it.tags) }
            result.secureNotes.forEach       { addAll(it.tags) }
            result.customEntries.forEach     { addAll(it.tags) }
            result.identityDocuments.forEach { addAll(it.tags) }
            result.vehicles.forEach          { addAll(it.tags) }
            result.reminders.forEach         { addAll(it.tags) }
        }
        val delBackup = result.tags
        val nombresDelBackup = delBackup.map { it.name }.toSet()
        val derivadas = usadas
            .filter { it.isNotBlank() && it !in nombresDelBackup }
            .map { com.thormakk.keyarmor.data.database.entity.TagEntity(id = java.util.UUID.randomUUID().toString(), name = it) }
        if (delBackup.isNotEmpty() || derivadas.isNotEmpty()) tagRepository.upsertAll(delBackup + derivadas)
    }

    private fun restoreHeirIdentityIfPresent(result: ImportResult) {
        result.inheritanceIdentity?.let { id ->
            runCatching { heirIdentityManager.importIdentity(id.publicKeyB64, id.privateKeyCode) }
                .onFailure { Log.w("ExportImportRepository", "No se pudo restaurar la identidad del heredero", it) }
            val ownerDelBackup = id.ownerEmail
            val pinDelBackup   = id.pinnedOwnerSigningKey
            if (ownerDelBackup != null && inheritancePrefs.getHeirOfOwnerEmail() == null) {
                inheritancePrefs.setHeirOfOwnerEmail(ownerDelBackup)
                if (pinDelBackup != null) {
                    inheritancePrefs.savePinnedOwnerSigningKey(ownerDelBackup, pinDelBackup)
                }
            }
        }
        result.ownerSigningIdentity?.let { id ->
            runCatching { ownerSigningIdentityManager.importFromBackup(id.publicKeyB64, id.privatePkcs8B64, id.ownerEmail) }
                .onFailure { Log.w("ExportImportRepository", "No se pudo restaurar la identidad de firma del dueño", it) }
        }
    }

    private suspend fun persistEntries(
        result: ImportResult,
        mode:   RestoreMode,
        vaultId: String?,
        esCsv:  Boolean = false
    ): PersistedEntries = withContext(Dispatchers.IO) {
        when (mode) {

            RestoreMode.REPLACE -> db.withTransaction {
                val bovedasDestino: Set<String> = if (vaultId != null) setOf(vaultId) else buildSet {
                    result.vaults.forEach            { add(it.id) }
                    result.passwords.forEach         { add(it.vaultId) }
                    result.twoFactor.forEach         { add(it.vaultId) }
                    result.creditCards.forEach       { add(it.vaultId) }
                    result.bankAccounts.forEach      { add(it.vaultId) }
                    result.cryptoWallets.forEach     { add(it.vaultId) }
                    result.secureNotes.forEach       { add(it.vaultId) }
                    result.customEntries.forEach     { add(it.vaultId) }
                    result.identityDocuments.forEach { add(it.vaultId) }
                    result.vehicles.forEach          { add(it.vaultId) }
                }
                bovedasDestino.forEach { boveda ->
                    vaultRepository.archiveAll(boveda)
                    twoFactorRepository.archiveAll(boveda)
                    creditCardRepository.archiveAll(boveda)
                    bankAccountRepository.archiveAll(boveda)
                    cryptoWalletRepository.archiveAll(boveda)
                    secureNoteRepository.archiveAll(boveda)
                    customEntryRepository.archiveAll(boveda)
                    identityDocumentRepository.archiveAll(boveda)
                    vehicleRepository.archiveAll(boveda)
                    passkeyRepository.deleteAllExceptHiddenInVault(boveda)
                }

                result.vaults.forEach { vaultMetadataRepository.save(it) }

                val protegidas = vaultRepository.protectedAmong(result.passwords.map { it.id })
                val protTwoFactor = twoFactorRepository.protectedAmong(result.twoFactor.map { it.id })
                val protCards     = creditCardRepository.protectedAmong(result.creditCards.map { it.id })
                val protBanks     = bankAccountRepository.protectedAmong(result.bankAccounts.map { it.id })
                val protWallets   = cryptoWalletRepository.protectedAmong(result.cryptoWallets.map { it.id })
                val protNotes     = secureNoteRepository.protectedAmong(result.secureNotes.map { it.id })
                val protCustom    = customEntryRepository.protectedAmong(result.customEntries.map { it.id })
                val protPasskeys  = passkeyRepository.protectedAmong(result.passkeys.map { it.credentialId })
                val protDocs      = identityDocumentRepository.protectedAmong(result.identityDocuments.map { it.id })
                val protVehicles  = vehicleRepository.protectedAmong(result.vehicles.map { it.id })
                val omitidas   = protegidas.size + protTwoFactor.size + protCards.size + protBanks.size +
                    protWallets.size + protNotes.size + protCustom.size + protPasskeys.size +
                    protDocs.size + protVehicles.size
                val passwordIdMap = result.passwords.filterNot { it.id in protegidas }.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    vaultRepository.saveEntry(e.copy(id = newId, vaultId = vaultId ?: e.vaultId))
                    e.id to newId
                }
                result.twoFactor.filterNot { it.id in protTwoFactor }.forEach { twoFactorRepository.saveAccount(it.copy(id = UUID.randomUUID().toString(), vaultId = vaultId ?: it.vaultId)) }
                val creditCardIdMap = result.creditCards.filterNot { it.id in protCards }.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    creditCardRepository.saveCard(e.copy(id = newId))
                    e.id to newId
                }
                val bankAccountIdMap = result.bankAccounts.filterNot { it.id in protBanks }.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    bankAccountRepository.saveAccount(e.copy(id = newId))
                    e.id to newId
                }
                val cryptoWalletIdMap = result.cryptoWallets.filterNot { it.id in protWallets }.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    cryptoWalletRepository.saveWallet(e.copy(id = newId))
                    e.id to newId
                }
                val secureNoteIdMap = result.secureNotes.filterNot { it.id in protNotes }.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    secureNoteRepository.saveNote(e.copy(id = newId))
                    e.id to newId
                }
                val customEntryIdMap = result.customEntries.filterNot { it.id in protCustom }.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    customEntryRepository.saveEntry(e.copy(id = newId))
                    e.id to newId
                }
                result.passkeys.filterNot { it.credentialId in protPasskeys }.forEach { passkeyRepository.savePasskey(it) }
                val identityDocumentIdMap = result.identityDocuments.filterNot { it.id in protDocs }.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    identityDocumentRepository.saveDocument(e.copy(id = newId))
                    e.id to newId
                }
                val vehicleIdMap = result.vehicles.filterNot { it.id in protVehicles }.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    vehicleRepository.saveVehicle(e.copy(id = newId))
                    e.id to newId
                }
                PersistedEntries(
                    count = result.totalCount - omitidas,
                    idMap = passwordIdMap + creditCardIdMap + bankAccountIdMap +
                        cryptoWalletIdMap + secureNoteIdMap + customEntryIdMap +
                        identityDocumentIdMap + vehicleIdMap,
                    skippedProtected = omitidas,
                )
            }

            RestoreMode.MERGE -> db.withTransaction {
                val exPass   = vaultRepository.getAllForBackup()
                val exTf     = twoFactorRepository.getAllForBackup()
                val exCard   = creditCardRepository.getAllForBackup()
                val exBank   = bankAccountRepository.getAllForBackup()
                val exCrypto = cryptoWalletRepository.getAllForBackup()
                val exNote   = secureNoteRepository.getAllForBackup()
                val exCustom = customEntryRepository.getAllForBackup()
                val exPk     = passkeyRepository.getAllForBackup()
                val exDoc    = identityDocumentRepository.getAllForBackup()
                val exVeh    = vehicleRepository.getAllForBackup()

                val passIds     = exPass.map { it.id }.toSet();           val passContent   = exPass.map { entryContentKey.of(it) }.toSet()
                val tfIds       = exTf.map { it.id }.toSet();             val tfContent     = exTf.map { entryContentKey.of(it) }.toSet()
                val cardIds     = exCard.map { it.id }.toSet();           val cardContent   = exCard.map { entryContentKey.of(it) }.toSet()
                val bankIds     = exBank.map { it.id }.toSet();           val bankContent   = exBank.map { entryContentKey.of(it) }.toSet()
                val cryptoIds   = exCrypto.map { it.id }.toSet();         val cryptoContent = exCrypto.map { entryContentKey.of(it) }.toSet()
                val noteIds     = exNote.map { it.id }.toSet();           val noteContent   = exNote.map { entryContentKey.of(it) }.toSet()
                val customIds   = exCustom.map { it.id }.toSet();         val customContent = exCustom.map { entryContentKey.of(it) }.toSet()
                val pkIds       = exPk.map { it.credentialId }.toSet();   val pkContent     = exPk.map { entryContentKey.of(it) }.toSet()
                val docIds      = exDoc.map { it.id }.toSet();            val docContent    = exDoc.map { entryContentKey.of(it) }.toSet()
                val vehIds      = exVeh.map { it.id }.toSet();            val vehContent    = exVeh.map { entryContentKey.of(it) }.toSet()

                val newPasswords = if (esCsv) {
                    val clavesCsv = exPass.map { claveCsv(it) }.toSet()
                    result.passwords.filter { claveCsv(it) !in clavesCsv }
                } else {
                    result.passwords.filter { it.id !in passIds && entryContentKey.of(it) !in passContent }
                }
                val newTwoFactor         = result.twoFactor.filter          { it.id !in tfIds       && entryContentKey.of(it) !in tfContent }
                val newCreditCards       = result.creditCards.filter        { it.id !in cardIds     && entryContentKey.of(it) !in cardContent }
                val newBankAccounts      = result.bankAccounts.filter       { it.id !in bankIds     && entryContentKey.of(it) !in bankContent }
                val newCryptoWallets     = result.cryptoWallets.filter      { it.id !in cryptoIds   && entryContentKey.of(it) !in cryptoContent }
                val newSecureNotes       = result.secureNotes.filter        { it.id !in noteIds     && entryContentKey.of(it) !in noteContent }
                val newCustomEntries     = result.customEntries.filter      { it.id !in customIds   && entryContentKey.of(it) !in customContent }
                val newPasskeys          = result.passkeys.filter           { it.credentialId !in pkIds && entryContentKey.of(it) !in pkContent }
                val newIdentityDocuments = result.identityDocuments.filter  { it.id !in docIds      && entryContentKey.of(it) !in docContent }
                val newVehicles          = result.vehicles.filter           { it.id !in vehIds      && entryContentKey.of(it) !in vehContent }

                newPasswords.forEach          { vaultRepository.saveEntry(if (vaultId != null) it.copy(vaultId = vaultId) else it) }
                newTwoFactor.forEach          { twoFactorRepository.saveAccount(if (vaultId != null) it.copy(vaultId = vaultId) else it) }
                newCreditCards.forEach        { creditCardRepository.saveCard(it) }
                newBankAccounts.forEach       { bankAccountRepository.saveAccount(it) }
                newCryptoWallets.forEach      { cryptoWalletRepository.saveWallet(it) }
                newSecureNotes.forEach        { secureNoteRepository.saveNote(it) }
                newCustomEntries.forEach      { customEntryRepository.saveEntry(it) }
                newPasskeys.forEach           { passkeyRepository.savePasskey(it) }
                newIdentityDocuments.forEach  { identityDocumentRepository.saveDocument(it) }
                newVehicles.forEach           { vehicleRepository.saveVehicle(it) }
                val existingVaultIds = vaultMetadataRepository.getAll().map { it.id }.toSet()
                result.vaults.filter { it.id !in existingVaultIds }.forEach { vaultMetadataRepository.save(it) }

                val count = newPasswords.size + newTwoFactor.size + newCreditCards.size + newBankAccounts.size +
                    newCryptoWallets.size + newSecureNotes.size + newCustomEntries.size +
                    newPasskeys.size + newIdentityDocuments.size + newVehicles.size
                PersistedEntries(count, emptyMap())
            }

            RestoreMode.IMPORT_ALL -> db.withTransaction {
                val passwordIdMap = result.passwords.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    vaultRepository.saveEntry(e.copy(id = newId, vaultId = vaultId ?: e.vaultId))
                    e.id to newId
                }
                result.twoFactor.forEach { twoFactorRepository.saveAccount(it.copy(id = UUID.randomUUID().toString(), vaultId = vaultId ?: it.vaultId)) }
                val creditCardIdMap = result.creditCards.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    creditCardRepository.saveCard(e.copy(id = newId))
                    e.id to newId
                }
                val bankAccountIdMap = result.bankAccounts.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    bankAccountRepository.saveAccount(e.copy(id = newId))
                    e.id to newId
                }
                val cryptoWalletIdMap = result.cryptoWallets.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    cryptoWalletRepository.saveWallet(e.copy(id = newId))
                    e.id to newId
                }
                val secureNoteIdMap = result.secureNotes.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    secureNoteRepository.saveNote(e.copy(id = newId))
                    e.id to newId
                }
                val customEntryIdMap = result.customEntries.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    customEntryRepository.saveEntry(e.copy(id = newId))
                    e.id to newId
                }
                result.passkeys.forEach { passkeyRepository.savePasskey(it) }
                val identityDocumentIdMap = result.identityDocuments.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    identityDocumentRepository.saveDocument(e.copy(id = newId))
                    e.id to newId
                }
                val vehicleIdMap = result.vehicles.associate { e ->
                    val newId = UUID.randomUUID().toString()
                    vehicleRepository.saveVehicle(e.copy(id = newId))
                    e.id to newId
                }
                result.vaults.forEach { vaultMetadataRepository.save(it) }
                PersistedEntries(
                    count = result.totalCount,
                    idMap = passwordIdMap + creditCardIdMap + bankAccountIdMap +
                        cryptoWalletIdMap + secureNoteIdMap + customEntryIdMap +
                        identityDocumentIdMap + vehicleIdMap,
                )
            }
        }

    }

    private suspend fun restoreAttachmentsInMemory(
        attachments: List<AttachmentBackupEntry>,
        mode: RestoreMode,
        entryIdMap: Map<String, String>,
    ): Int {
        if (attachments.isEmpty()) return 0
        var restored = 0
        when (mode) {
            RestoreMode.MERGE -> {
                val existingAttachmentIds = attachmentRepository.getAllForBackup().map { it.id }.toSet()
                attachments.filter { it.id !in existingAttachmentIds }.forEach { a ->
                    runCatching {
                        if (attachmentRepository.restoreDecryptedAttachment(
                                id = a.id, entryId = a.entryId, plainFileName = a.fileName,
                                plainMimeType = a.mimeType, fileSize = a.fileSize,
                                createdAt = a.createdAt, plainBytes = a.content
                            )) restored++
                    }.onFailure {
                        Log.w("ExportImportRepository", "restoreAttachmentsInMemory: adjunto ${a.id} omitido (MERGE)", it)
                    }
                }
            }
            RestoreMode.REPLACE, RestoreMode.IMPORT_ALL -> {
                attachments.forEach { a ->
                    val newEntryId = entryIdMap[a.entryId] ?: return@forEach
                    runCatching {
                        if (attachmentRepository.restoreDecryptedAttachment(
                                id = UUID.randomUUID().toString(), entryId = newEntryId,
                                plainFileName = a.fileName, plainMimeType = a.mimeType,
                                fileSize = a.fileSize, createdAt = a.createdAt, plainBytes = a.content
                            )) restored++
                    }.onFailure {
                        Log.w("ExportImportRepository", "restoreAttachmentsInMemory: adjunto ${a.id} omitido ($mode)", it)
                    }
                }
            }
        }
        return restored
    }

    suspend fun countAllEntries(): Int = withContext(Dispatchers.IO) {
        vaultRepository.getAllForBackup().size +
        twoFactorRepository.getAllForBackup().size +
        creditCardRepository.getAllForBackup().size +
        bankAccountRepository.getAllForBackup().size +
        cryptoWalletRepository.getAllForBackup().size +
        secureNoteRepository.getAllForBackup().size +
        customEntryRepository.getAllForBackup().size +
        passkeyRepository.getAllForBackup().size +
        identityDocumentRepository.getAllForBackup().size +
        vehicleRepository.getAllForBackup().size
    }

    private fun buildWrapper(salt: ByteArray, encryptedData: String): String =
        JSONObject().apply {
            put("version",     2)
            put("kdf",         "argon2id")
            put("memoryKb",    CryptoManager.ARGON2_DEFAULT_MEMORY_KB)
            put("iterations",  CryptoManager.ARGON2_DEFAULT_ITERATIONS)
            put("parallelism", CryptoManager.ARGON2_DEFAULT_PARALLELISM)
            put("salt",        cryptoManager.saltToBase64(salt))
            put("data",        encryptedData)
        }.toString()

    private fun deriveWrapperKey(wrapper: JSONObject, password: String, salt: ByteArray): SecretKeySpec {
        val memoryKb    = wrapper.getInt("memoryKb")
        val iterations  = wrapper.getInt("iterations")
        val parallelism = wrapper.getInt("parallelism")
        require(memoryKb in 1024..262_144 && iterations in 1..20 && parallelism in 1..8) {
            "bad_kdf_params"
        }
        return cryptoManager.deriveKeyArgon2id(password, salt, memoryKb, iterations, parallelism)
    }

    private suspend fun gatherAllData(includeAttachments: Boolean = false): String {
        val vaults            = vaultMetadataRepository.getAll()
        val passwords         = vaultRepository.getAllForBackup()
        val twoFactor         = twoFactorRepository.getAllForBackup()
        val creditCards       = creditCardRepository.getAllForBackup()
        val bankAccounts      = bankAccountRepository.getAllForBackup()
        val cryptoWallets     = cryptoWalletRepository.getAllForBackup()
        val secureNotes       = secureNoteRepository.getAllForBackup()
        val customEntries     = customEntryRepository.getAllForBackup()
        val passkeys          = passkeyRepository.getAllForBackup()
        val identityDocuments = identityDocumentRepository.getAllForBackup()
        val vehicles          = vehicleRepository.getAllForBackup()
        val attachments       = if (includeAttachments) attachmentRepository.getAllDecryptedForBackup() else emptyList()
        val reminders         = reminderRepository.getAllDecryptedForBackup()
        val noteHistory       = secureNoteHistoryRepository.getAllDecryptedForBackup()
        val tags              = tagRepository.getAllForBackup()
        val heirOwner   = inheritancePrefs.getHeirOfOwnerEmail()
        val heirOwnerPin = heirOwner?.let { inheritancePrefs.getPinnedOwnerSigningKey(it) }
        val heirId            = heirIdentityManager.getIdentityForBackup()
        val ownerSignId       = ownerSigningIdentityManager.getForBackup()
        return ExportSerializer.toJson(
            passwords, twoFactor, creditCards, bankAccounts,
            cryptoWallets, secureNotes, customEntries, passkeys,
            identityDocuments, vehicles, vaults, attachments,
            reminders               = reminders,
            secureNoteHistory       = noteHistory,
            tags                    = tags,
            heirIdentityPublicB64   = heirId?.first,
            heirIdentityPrivateCode = heirId?.second,
            heirOfOwnerEmail        = heirOwner,
            pinnedOwnerSigningKey   = heirOwnerPin,
            ownerSigningPublicB64    = ownerSignId?.first,
            ownerSigningPrivatePkcs8 = ownerSignId?.second,
            ownerSigningEmail        = drivePrefs.getConnectedEmail(),
            pinned                   = pinnedHubPrefs.currentRefs()
        )
    }

    private suspend fun desocultarSiNoHayFrase() {
        if (!hiddenEntriesPrefs.isEnabled()) unhideAllEntries()
    }

    suspend fun unhideAllEntries() {
        vaultRepository.unhideAll()
        bankAccountRepository.unhideAll()
        creditCardRepository.unhideAll()
        identityDocumentRepository.unhideAll()
        vehicleRepository.unhideAll()
        secureNoteRepository.unhideAll()
        twoFactorRepository.unhideAll()
        customEntryRepository.unhideAll()
        cryptoWalletRepository.unhideAll()
        passkeyRepository.unhideAll()
    }

    suspend fun deleteAllHiddenEntriesPermanently() {
        vaultRepository.getAllForBackup().filter { it.isHidden }
            .forEach { vaultRepository.deletePermanently(it.id) }
        bankAccountRepository.getAllForBackup().filter { it.isHidden }
            .forEach { bankAccountRepository.deletePermanently(it.id) }
        creditCardRepository.getAllForBackup().filter { it.isHidden }
            .forEach { creditCardRepository.deletePermanently(it.id) }
        identityDocumentRepository.getAllForBackup().filter { it.isHidden }
            .forEach { identityDocumentRepository.deletePermanently(it.id) }
        vehicleRepository.getAllForBackup().filter { it.isHidden }
            .forEach { vehicleRepository.deletePermanently(it.id) }
        secureNoteRepository.getAllForBackup().filter { it.isHidden }
            .forEach { secureNoteRepository.deletePermanently(it.id) }
        twoFactorRepository.getAllForBackup().filter { it.isHidden }
            .forEach { twoFactorRepository.deletePermanently(it.id) }
        customEntryRepository.getAllForBackup().filter { it.isHidden }
            .forEach { customEntryRepository.deletePermanently(it.id) }
        cryptoWalletRepository.getAllForBackup().filter { it.isHidden }
            .forEach { cryptoWalletRepository.deletePermanently(it.id) }
        passkeyRepository.getAllForBackup().filter { it.isHidden }
            .forEach { passkeyRepository.deletePermanently(it.credentialId) }
    }

    suspend fun countHiddenEntries(): Int =
        vaultRepository.getAllForBackup().count { it.isHidden } +
        bankAccountRepository.getAllForBackup().count { it.isHidden } +
        creditCardRepository.getAllForBackup().count { it.isHidden } +
        identityDocumentRepository.getAllForBackup().count { it.isHidden } +
        vehicleRepository.getAllForBackup().count { it.isHidden } +
        secureNoteRepository.getAllForBackup().count { it.isHidden } +
        twoFactorRepository.getAllForBackup().count { it.isHidden } +
        customEntryRepository.getAllForBackup().count { it.isHidden } +
        cryptoWalletRepository.getAllForBackup().count { it.isHidden } +
        passkeyRepository.getAllForBackup().count { it.isHidden }
}
