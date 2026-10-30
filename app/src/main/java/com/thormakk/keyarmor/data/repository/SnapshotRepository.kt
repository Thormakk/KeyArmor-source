@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.accounts.Account
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.room.withTransaction
import com.thormakk.keyarmor.data.database.KeyArmorDatabase
import com.thormakk.keyarmor.data.database.dao.VaultSnapshotDao
import com.thormakk.keyarmor.data.database.entity.AttachmentEntity
import com.thormakk.keyarmor.data.database.entity.PasswordHistoryEntity
import com.thormakk.keyarmor.data.database.entity.TagEntity
import com.thormakk.keyarmor.data.database.entity.VaultSnapshotEntity
import com.thormakk.keyarmor.data.backup.KabAttachmentSource
import com.thormakk.keyarmor.data.backup.KabContainer
import com.thormakk.keyarmor.data.backup.KabContainerReader
import com.thormakk.keyarmor.data.backup.KabContainerWriter
import com.thormakk.keyarmor.data.backup.KabHeader
import com.thormakk.keyarmor.data.drive.DriveBackupRepository
import com.thormakk.keyarmor.data.drive.DriveSnapshotInfo
import com.thormakk.keyarmor.data.export.ExportSerializer
import com.thormakk.keyarmor.data.export.ImportParser
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.SnapshotPrefs
import com.thormakk.keyarmor.di.ApplicationScope
import com.thormakk.keyarmor.di.IoDispatcher
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.domain.model.VaultSnapshot
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.SessionExpiredException
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

data class RestoreSummary(
    val restored:     Int,
    val overwritten:  Int
)

data class SnapshotReEncryptPlan(
    val entity:               VaultSnapshotEntity,
    val plainRelativePath:    String,
    val tempContentFile:      File,
    val newEncryptedFilePath: String
)

@Singleton
class SnapshotRepository @Inject constructor(
    private val dao:                        VaultSnapshotDao,
    private val session:                    SessionManager,
    private val crypto:                     CryptoManager,
    @ApplicationContext private val context: Context,
    private val vaultRepository:            VaultRepository,
    private val twoFactorRepository:        TwoFactorRepository,
    private val passkeyRepository:          PasskeyRepository,
    private val bankAccountRepository:      BankAccountRepository,
    private val creditCardRepository:       CreditCardRepository,
    private val secureNoteRepository:       SecureNoteRepository,
    private val customEntryRepository:      CustomEntryRepository,
    private val identityDocumentRepository: IdentityDocumentRepository,
    private val vehicleRepository:          VehicleRepository,
    private val cryptoWalletRepository:     CryptoWalletRepository,
    private val tagRepository:              TagRepository,
    private val hiddenEntriesPrefs:         com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs,
    private val passwordHistoryRepository:  PasswordHistoryRepository,
    private val vaultMetadataRepository:    VaultMetadataRepository,
    private val attachmentRepository:       AttachmentRepository,
    private val entryContentKey:            com.thormakk.keyarmor.data.export.EntryContentKey,
    private val wipeRepository:             WipeRepository,
    private val snapshotPrefs:              SnapshotPrefs,
    private val drivePrefs:                 DrivePrefs,
    private val driveBackupRepository:      DriveBackupRepository,
    private val claveDeRespaldo:            com.thormakk.keyarmor.data.backup.ClaveDeRespaldo,
    private val pinnedHubPrefs:             PinnedHubPrefs,
    private val db:                         KeyArmorDatabase,
    private val panicPinPrefs:              com.thormakk.keyarmor.data.prefs.PanicPinPrefs,
    private val planPrefs:                  com.thormakk.keyarmor.data.prefs.PlanPrefs,
    private val reminderRepository:         ReminderRepository,
    private val secureNoteHistoryRepository: SecureNoteHistoryRepository,
    private val heirIdentityManager:        com.thormakk.keyarmor.security.HeirIdentityManager,
    private val inheritancePrefs:           com.thormakk.keyarmor.data.prefs.InheritancePrefs,
    private val ownerSigningIdentityManager: com.thormakk.keyarmor.security.OwnerSigningIdentityManager,
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    init {
        appScope.launch {
            session.isUnlockedFlow.collect { unlocked ->
                val coercionActive = panicPinPrefs.coercionActiveFlow.value
                if (unlocked && !coercionActive && canCreateSnapshots() && snapshotPrefs.isWeeklyEnabled() && snapshotPrefs.isWeeklyDue()) {
                    try {
                        createSnapshot(SnapshotTrigger.WEEKLY)
                        snapshotPrefs.markWeeklyDone()
                    } catch (_: Exception) {   }
                }
                if (unlocked && !coercionActive && snapshotPrefs.isSyncToDriveEnabled()) {
                    try {
                        localizeDriveOnlySnapshots()
                    } catch (_: Exception) {   }
                }
            }
        }
    }

    private val snapshotDir: File
        get() = File(context.filesDir, "snapshots").also { it.mkdirs() }

    private fun encField(value: String, key: SecretKeySpec): String =
        if (value.isEmpty()) "" else crypto.encrypt(value, key)

    private fun decField(value: String, key: SecretKeySpec): String =
        if (value.isEmpty()) "" else runCatching { crypto.decrypt(value, key) }.getOrDefault(value)

    fun observeSnapshots(): Flow<List<VaultSnapshot>> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(emptyList())
            else dao.observeAll().map { list -> list.map { it.toDomain(key) } }
        }

    suspend fun getSnapshotById(id: String): VaultSnapshot? {
        val key = session.getSecretKey() ?: return null
        return dao.getById(id)?.toDomain(key)
    }

    fun canCreateSnapshots(): Boolean =
        planPrefs.effectivePlan() != com.thormakk.keyarmor.data.prefs.UserPlan.FREE

    private suspend fun createPreRestoreSnapshot() {
        if (canCreateSnapshots()) createSnapshot(SnapshotTrigger.PRE_RESTORE)
    }

    suspend fun createSnapshotBestEffort(trigger: SnapshotTrigger) {
        if (!canCreateSnapshots()) return
        try {
            createSnapshot(trigger)
        } catch (e: Exception) {
            Log.w("SnapshotRepo", "snapshot best-effort falló (trigger=$trigger)", e)
        }
    }

    @Throws(SessionExpiredException::class)
    suspend fun createSnapshot(trigger: SnapshotTrigger): VaultSnapshot =
        withContext(io) { createSnapshotInternal(trigger) }

    private suspend fun createSnapshotInternal(trigger: SnapshotTrigger): VaultSnapshot {
        val key = session.getSecretKey() ?: throw SessionExpiredException()

        val passwords         = vaultRepository.getAllForBackup()
        val twoFactor         = twoFactorRepository.getAllForBackup()
        val passkeys          = passkeyRepository.getAllForBackup()
        val bankAccounts      = bankAccountRepository.getAllForBackup()
        val creditCards       = creditCardRepository.getAllForBackup()
        val secureNotes       = secureNoteRepository.getAllForBackup()
        val customEntries     = customEntryRepository.getAllForBackup()
        val identityDocuments = identityDocumentRepository.getAllForBackup()
        val vehicles          = vehicleRepository.getAllForBackup()
        val cryptoWallets     = cryptoWalletRepository.getAllForBackup()
        val tags              = tagRepository.getAllForBackup()
        val history           = passwordHistoryRepository.getAllForBackup()
        val vaults            = vaultMetadataRepository.getAll()
        val reminders         = reminderRepository.getAllDecryptedForBackup()
        val noteHistory       = secureNoteHistoryRepository.getAllDecryptedForBackup()

        if (trigger == SnapshotTrigger.WEEKLY &&
            passwords.size + twoFactor.size + passkeys.size + bankAccounts.size +
            creditCards.size + secureNotes.size + customEntries.size +
            identityDocuments.size + vehicles.size + cryptoWallets.size == 0) {
            throw EmptyVaultSkippedException()
        }

        val heirOwner   = inheritancePrefs.getHeirOfOwnerEmail()
        val heirOwnerPin = heirOwner?.let { inheritancePrefs.getPinnedOwnerSigningKey(it) }
        val heirId            = heirIdentityManager.getIdentityForBackup()
        val ownerSignId       = ownerSigningIdentityManager.getForBackup()
        val baseJson = ExportSerializer.toJson(
            passwords, twoFactor, creditCards, bankAccounts, cryptoWallets,
            secureNotes, customEntries, passkeys, identityDocuments, vehicles, vaults,
            reminders               = reminders,
            secureNoteHistory       = noteHistory,
            heirIdentityPublicB64   = heirId?.first,
            heirIdentityPrivateCode = heirId?.second,
            heirOfOwnerEmail        = heirOwner,
            pinnedOwnerSigningKey   = heirOwnerPin,
            ownerSigningPublicB64    = ownerSignId?.first,
            ownerSigningPrivatePkcs8 = ownerSignId?.second,
            ownerSigningEmail        = drivePrefs.getConnectedEmail()
        )
        val root = JSONObject(baseJson)

        val tagsArray = JSONArray()
        tags.forEach { tag ->
            tagsArray.put(JSONObject().apply {
                put("id",   tag.id)
                put("name", tag.name)
            })
        }
        root.put("tags", tagsArray)
        root.put("pinned", PinnedHubPrefs.toJson(pinnedHubPrefs.currentRefs()))

        val historyArray = JSONArray()
        history.forEach { h ->
            historyArray.put(JSONObject().apply {
                put("id",     h.id)
                put("entryId", h.entryId)
                put("hash",   h.passwordHash)
                put("encPwd", h.encryptedPassword)
                put("savedAt", h.savedAt)
            })
        }
        root.put("passwordHistory", historyArray)

        root.put("snapshotTrigger", trigger.name)

        val plaintext = root.toString()

        val includeAttachments = snapshotPrefs.isIncludeAttachmentsEnabled() ||
            trigger == SnapshotTrigger.PRE_RESTORE ||
            trigger.esPreImport ||
            trigger == SnapshotTrigger.PRE_BULK_DELETE ||
            trigger == SnapshotTrigger.PRE_MASTER_CHANGE
        val attachments = if (includeAttachments) attachmentRepository.getAllForBackup() else emptyList()
        val fotos = (if (includeAttachments) identityDocumentRepository.photosForBackup() else emptyList()).map { f ->
            FotoEnManifiesto(
                manifestId = "idphoto_${f.documentId}_${if (f.isFront) "front" else "back"}",
                documentId = f.documentId,
                isFront    = f.isFront,
                storedPath = f.storedPath,
            )
        }
        val id       = UUID.randomUUID().toString()
        val fileName = "$id.kab"
        val file     = File(snapshotDir, fileName)

        val manifestJson = JSONArray().apply {
            attachments.forEach { att ->
                put(JSONObject().apply {
                    put("id",        att.id)
                    put("entryId",   att.entryId)
                    put("fileName",  att.fileName)
                    put("mimeType",  att.mimeType)
                    put("fileSize",  att.fileSize)
                    put("createdAt", att.createdAt)
                    put("kind",      KIND_FILE)
                })
            }
            fotos.forEach { foto ->
                put(JSONObject().apply {
                    put("id",        foto.manifestId)
                    put("entryId",   foto.documentId)
                    put("fileName",  "")
                    put("mimeType",  "image/jpeg")
                    put("fileSize",  0L)
                    put("createdAt", System.currentTimeMillis())
                    put("kind",      if (foto.isFront) KIND_PHOTO_FRONT else KIND_PHOTO_BACK)
                })
            }
        }.toString()
        val header  = KabHeader(
            mode = KabContainer.MODE_SESSION,
            attachmentCount = attachments.size + fotos.size,
            kdf = null
        )
        val sources = attachments.asSequence().map { att ->
            KabAttachmentSource(att.id) {
                runCatching { attachmentRepository.readRawEncryptedBytes(att.storedPath) }.getOrNull()
            }
        } + fotos.asSequence().map { foto ->
            KabAttachmentSource(foto.manifestId) {
                runCatching { attachmentRepository.readRawEncryptedBytes(foto.storedPath) }.getOrNull()
            }
        }
        file.outputStream().use { out ->
            KabContainerWriter(crypto).write(out, header, key, plaintext, manifestJson, sources)
        }

        val entryCount = passwords.size + twoFactor.size + passkeys.size +
                         bankAccounts.size + creditCards.size + secureNotes.size +
                         customEntries.size + identityDocuments.size +
                         vehicles.size + cryptoWallets.size

        val entity = VaultSnapshotEntity(
            id            = id,
            timestamp     = System.currentTimeMillis(),
            trigger       = trigger.name,
            entryCount    = entryCount,
            filePath      = encField("snapshots/$fileName", key),
            fileSizeBytes = file.length(),
            syncedToDrive = false,
            includesAttachments = includeAttachments
        )
        dao.insert(entity)

        pruneIfNeeded()

        if (snapshotPrefs.isSyncToDriveEnabled()) {
            appScope.launch(Dispatchers.IO) {
                try {
                    val account = driveAccount() ?: return@launch
                    claveDeRespaldo.paraRespaldo { clave ->
                        val password = if (clave == null) (drivePassword() ?: return@launch) else null
                        driveBackupRepository.uploadSnapshot(
                            account, password, id, trigger, entity.timestamp, plaintext, clave
                        )
                    }
                    dao.markSyncedToDrive(id)
                    pruneDriveIfNeeded(account)
                } catch (_: Exception) {   }
            }
        }

        return entity.toDomain(key)
    }

    suspend fun pruneIfNeeded() {
        val weekly = dao.getAllWeekly()
        if (weekly.size > RetencionPuntos.CUPO_SEMANALES) {
            weekly.drop(RetencionPuntos.CUPO_SEMANALES).forEach { deleteSnapshot(it.id) }
        }
        val nonWeekly = dao.getAllNonWeekly()
        RetencionPuntos.aBorrar(
            nonWeekly.map { RetencionPuntos.Punto(it.id, it.timestamp, it.trigger == SnapshotTrigger.MANUAL.name) }
        ).forEach { deleteSnapshot(it) }
    }

    private suspend fun pruneDriveIfNeeded(account: Account) {
        val allDrive = runCatching {
            driveBackupRepository.listDriveSnapshots(account)
        }.getOrElse { return }

        if (allDrive.isEmpty()) return

        val localIds = dao.getAll().map { it.id }.toSet()
        val driveOnly = allDrive.filter { it.snapshotId !in localIds }

        if (driveOnly.isEmpty()) return

        val localWeeklyCount    = dao.getAllWeekly().size
        val driveWeekly    = driveOnly.filter { it.trigger == SnapshotTrigger.WEEKLY }
        val driveNonWeekly = driveOnly.filter { it.trigger != SnapshotTrigger.WEEKLY }

        val weeklyExcess = (localWeeklyCount + driveWeekly.size) - RetencionPuntos.CUPO_SEMANALES

        if (weeklyExcess > 0) {
            driveWeekly.takeLast(weeklyExcess).forEach { info ->
                runCatching {
                    driveBackupRepository.deleteSnapshotFromDrive(account, info.driveFileId)
                }
            }
        }

        val combinados = dao.getAllNonWeekly()
            .map { RetencionPuntos.Punto(it.id, it.timestamp, it.trigger == SnapshotTrigger.MANUAL.name) } +
            driveNonWeekly.map { RetencionPuntos.Punto(it.snapshotId, it.timestamp, it.trigger == SnapshotTrigger.MANUAL) }
        val aBorrar = RetencionPuntos.aBorrar(combinados)
        driveNonWeekly.filter { it.snapshotId in aBorrar }.forEach { info ->
            runCatching {
                driveBackupRepository.deleteSnapshotFromDrive(account, info.driveFileId)
            }
        }
    }

    suspend fun deleteSnapshot(id: String) {
        val entity = dao.getById(id) ?: return
        if (entity.syncedToDrive) {
            val account = driveAccount()
            if (account != null) {
                runCatching {
                    driveBackupRepository.deleteSnapshotFromDriveBySnapshotId(account, id)
                }
            }
        }
        val key = session.getSecretKey()
        if (key != null) {
            File(context.filesDir, decField(entity.filePath, key)).delete()
        }
        dao.deleteById(id)
    }

    suspend fun deleteSnapshotsBatch(
        localIds:     Set<String>,
        driveFileIds: Set<String>,
        deleteAll:    Boolean
    ) {
        val account = driveAccount()
        val locales = if (deleteAll) dao.getAll() else localIds.mapNotNull { dao.getById(it) }

        val necesitaDrive = account != null &&
            (deleteAll || driveFileIds.isNotEmpty() || locales.any { it.syncedToDrive })
        if (necesitaDrive) {
            val idsLocales = locales.map { it.id }.toSet()
            val enDrive = driveBackupRepository.listDriveSnapshots(account)
            enDrive
                .filter { deleteAll || it.driveFileId in driveFileIds || it.snapshotId in idsLocales }
                .forEach { info ->
                    try {
                        driveBackupRepository.deleteSnapshotFromDrive(account, info.driveFileId)
                    } catch (e: com.google.api.client.googleapis.json.GoogleJsonResponseException) {
                        if (e.statusCode != 404) throw e
                    }
                }
        }

        val key = session.getSecretKey()
        locales.forEach { entity ->
            if (key != null) File(context.filesDir, decField(entity.filePath, key)).delete()
            dao.deleteById(entity.id)
        }
    }

    @Throws(SessionExpiredException::class)
    suspend fun loadSnapshotData(id: String): String {
        val key  = session.getSecretKey() ?: throw SessionExpiredException()
        val file = snapshotFileFor(id, key)
        return if (isKabFile(file)) readKabVaultJson(file, key)
               else crypto.decrypt(file.readText(), key)
    }

    @Throws(SessionExpiredException::class)
    suspend fun restoreReplace(id: String) {
        val key  = session.getSecretKey() ?: throw SessionExpiredException()
        val file = snapshotFileFor(id, key)
        if (isKabFile(file)) {
            val json = readKabVaultJson(file, key)
            createPreRestoreSnapshot()
            executeReplace(json)
            streamRestoreKabAttachments(file, key, RestoreMode.REPLACE, emptyMap())
        } else {
            val json = crypto.decrypt(file.readText(), key)
            createPreRestoreSnapshot()
            executeReplace(json)
        }
    }

    @Throws(SessionExpiredException::class)
    suspend fun restoreMerge(id: String): RestoreSummary {
        val key  = session.getSecretKey() ?: throw SessionExpiredException()
        val file = snapshotFileFor(id, key)
        return if (isKabFile(file)) {
            val json = readKabVaultJson(file, key)
            if (contarNuevosParaFusionar(json) > 0) createPreRestoreSnapshot()
            val summary = executeMerge(json)
            streamRestoreKabAttachments(file, key, RestoreMode.MERGE, emptyMap())
            summary
        } else {
            val json = crypto.decrypt(file.readText(), key)
            if (contarNuevosParaFusionar(json) > 0) createPreRestoreSnapshot()
            executeMerge(json)
        }
    }

    @Throws(SessionExpiredException::class)
    suspend fun restoreAddAll(id: String) {
        val key  = session.getSecretKey() ?: throw SessionExpiredException()
        val file = snapshotFileFor(id, key)
        if (isKabFile(file)) {
            val json = readKabVaultJson(file, key)
            createPreRestoreSnapshot()
            val idMap = executeAddAll(json)
            streamRestoreKabAttachments(file, key, RestoreMode.IMPORT_ALL, idMap)
        } else {
            val json = crypto.decrypt(file.readText(), key)
            createPreRestoreSnapshot()
            executeAddAll(json)
        }
    }

    private suspend fun snapshotFileFor(id: String, key: SecretKeySpec): File {
        val entity = dao.getById(id) ?: error("Snapshot no encontrado: $id")
        return File(context.filesDir, decField(entity.filePath, key))
    }

    private fun isKabFile(file: File): Boolean =
        runCatching {
            file.inputStream().use { val head = ByteArray(4); it.read(head) == 4 && KabContainer.looksLikeKab(head) }
        }.getOrDefault(false)

    private suspend fun readKabVaultJson(file: File, key: SecretKeySpec): String =
        file.inputStream().use { KabContainerReader(crypto).readHeaderAndVault(it) { key } }.second

    private suspend fun streamRestoreKabAttachments(
        file: File, key: SecretKeySpec, mode: RestoreMode, entryIdMap: Map<String, String>
    ) {
        val existingIds = if (mode == RestoreMode.MERGE)
            attachmentRepository.getAllForBackup().map { it.id }.toSet() else emptySet()
        val manifest = HashMap<String, JSONObject>()
        file.inputStream().use { input ->
            KabContainerReader(crypto).read(
                input = input,
                keyProvider = { key },
                onVault = { },
                onManifest = { json ->
                    val arr = JSONArray(json)
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        manifest[o.optString("id")] = o
                    }
                },
                onAttachment = { attId, rawBytes ->
                    val m = manifest[attId]
                    val dup = mode == RestoreMode.MERGE && attId in existingIds
                    val kind = m?.optString("kind", KIND_FILE) ?: KIND_FILE
                    if (m != null && kind != KIND_FILE) {
                        val origEntryId   = m.optString("entryId")
                        val targetEntryId = if (mode == RestoreMode.IMPORT_ALL) entryIdMap[origEntryId] else origEntryId
                        if (targetEntryId != null) {
                            runCatching {
                                identityDocumentRepository.restorePhotoFromBackup(
                                    documentId = targetEntryId,
                                    isFront    = kind == KIND_PHOTO_FRONT,
                                    encBytes   = rawBytes
                                )
                            }.onFailure { Log.w("SnapshotRepository", "streamRestoreKabAttachments: foto $attId omitida", it) }
                        }
                    } else if (m != null && !dup) {
                        val origEntryId   = m.optString("entryId")
                        val targetEntryId = if (mode == RestoreMode.IMPORT_ALL) entryIdMap[origEntryId] else origEntryId
                        val newId         = if (mode == RestoreMode.IMPORT_ALL) UUID.randomUUID().toString() else attId
                        if (targetEntryId != null) {
                            runCatching {
                                attachmentRepository.restoreFromSnapshot(
                                    AttachmentEntity(
                                        id         = newId,
                                        entryId    = targetEntryId,
                                        fileName   = m.optString("fileName"),
                                        mimeType   = m.optString("mimeType", "application/octet-stream"),
                                        fileSize   = m.optLong("fileSize", 0L),
                                        storedPath = "",
                                        createdAt  = m.optLong("createdAt", System.currentTimeMillis())
                                    ),
                                    rawBytes
                                )
                            }.onFailure { Log.w("SnapshotRepository", "streamRestoreKabAttachments: adjunto $attId omitido", it) }
                        }
                    }
                }
            )
        }
    }

    private fun reEncryptKabFile(src: File, dst: File, oldKey: SecretKeySpec, newKey: SecretKeySpec) {
        ZipInputStream(src.inputStream().buffered()).use { zin ->
            ZipOutputStream(dst.outputStream().buffered()).use { zout ->
                var entry = zin.nextEntry
                while (entry != null) {
                    val name = entry.name
                    val raw  = zin.readBytes()
                    zin.closeEntry()
                    val outBytes: ByteArray? = when {
                        name == KabContainer.ENTRY_META -> raw
                        name == KabContainer.ENTRY_VAULT || name == KabContainer.ENTRY_MANIFEST ->
                            crypto.encrypt(crypto.decrypt(raw.toString(Charsets.UTF_8), oldKey), newKey).toByteArray(Charsets.UTF_8)
                        KabContainer.attIdFromEntry(name) != null ->
                            runCatching { crypto.encryptBytes(crypto.decryptBytes(raw, oldKey), newKey) }.getOrNull()
                        else -> raw
                    }
                    if (outBytes != null) {
                        zout.putNextEntry(ZipEntry(name))
                        zout.write(outBytes)
                        zout.closeEntry()
                    }
                    entry = zin.nextEntry
                }
            }
        }
    }

    private fun parseTags(root: JSONObject): List<TagEntity> {
        val arr = root.optJSONArray("tags") ?: return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(TagEntity(
                    id   = obj.optString("id"),
                    name = obj.optString("name")
                ))
            }
        }
    }

    private fun parsePasswordHistory(root: JSONObject): List<PasswordHistoryEntity> {
        val arr = root.optJSONArray("passwordHistory") ?: return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(PasswordHistoryEntity(
                    id                = obj.optInt("id", 0),
                    entryId           = obj.optString("entryId"),
                    passwordHash      = obj.optString("hash"),
                    encryptedPassword = obj.optString("encPwd", ""),
                    savedAt           = obj.optLong("savedAt")
                ))
            }
        }
    }

    private data class SnapshotAttachment(
        val id:        String,
        val entryId:   String,
        val fileName:  String,
        val mimeType:  String,
        val fileSize:  Long,
        val createdAt: Long,
        val encBytes:  ByteArray,
        val kind:      String = "FILE"
    )

    private companion object Kinds {
        const val KIND_FILE        = "FILE"
        const val KIND_PHOTO_FRONT = "ID_PHOTO_FRONT"
        const val KIND_PHOTO_BACK  = "ID_PHOTO_BACK"
    }

    private data class FotoEnManifiesto(
        val manifestId: String,
        val documentId: String,
        val isFront:    Boolean,
        val storedPath: String,
    )

    private fun parseAttachments(root: JSONObject): List<SnapshotAttachment> {
        val arr = root.optJSONArray("attachments") ?: return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val b64 = obj.optString("encBytes", "")
                if (b64.isEmpty()) continue
                add(SnapshotAttachment(
                    id        = obj.optString("id"),
                    entryId   = obj.optString("entryId"),
                    fileName  = obj.optString("fileName"),
                    mimeType  = obj.optString("mimeType", "application/octet-stream"),
                    fileSize  = obj.optLong("fileSize", 0L),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    encBytes  = Base64.decode(b64, Base64.NO_WRAP),
                    kind      = obj.optString("kind", KIND_FILE)
                ))
            }
        }
    }

    suspend fun fetchDriveSnapshots(): List<DriveSnapshotInfo> {
        val account = driveAccount() ?: return emptyList()
        return driveBackupRepository.listDriveSnapshots(account)
            .also { snapshotPrefs.setDriveHasPoints(it.isNotEmpty()) }
    }

    fun hasDriveAccount(): Boolean = drivePrefs.hasConnectedEmail()

    @Throws(SessionExpiredException::class)
    suspend fun restoreReplaceFromDrive(info: DriveSnapshotInfo, claveManual: ByteArray? = null) {
        val json = downloadDriveSnapshotJson(info, claveManual)
        createPreRestoreSnapshot()
        executeReplace(json)
    }

    @Throws(SessionExpiredException::class)
    suspend fun restoreMergeFromDrive(info: DriveSnapshotInfo, claveManual: ByteArray? = null): RestoreSummary {
        val json = downloadDriveSnapshotJson(info, claveManual)
        if (contarNuevosParaFusionar(json) > 0) createPreRestoreSnapshot()
        return executeMerge(json)
    }

    @Throws(SessionExpiredException::class)
    suspend fun restoreAddAllFromDrive(info: DriveSnapshotInfo, claveManual: ByteArray? = null) {
        val json = downloadDriveSnapshotJson(info, claveManual)
        createPreRestoreSnapshot()
        executeAddAll(json)
    }

    suspend fun deleteFromDrive(info: DriveSnapshotInfo) {
        val account = driveAccount() ?: return
        try { driveBackupRepository.deleteSnapshotFromDrive(account, info.driveFileId) } catch (_: Exception) {}
    }

    suspend fun localizeDriveOnlySnapshots() {
        val account = driveAccount() ?: return
        val allDrive = runCatching { driveBackupRepository.listDriveSnapshots(account) }
            .getOrElse { return }
        snapshotPrefs.setDriveHasPoints(allDrive.isNotEmpty())
        if (allDrive.isEmpty()) return

        val localIds = dao.getAll().map { it.id }.toSet()
        val driveOnly = allDrive.filter { it.snapshotId !in localIds }
        if (driveOnly.isEmpty()) return

        val localWeeklyCount    = dao.getAllWeekly().size
        val localNonWeeklyCount = dao.getAllNonWeekly().size

        val driveWeekly    = driveOnly.filter { it.trigger == SnapshotTrigger.WEEKLY }
        val driveNonWeekly = driveOnly.filter { it.trigger != SnapshotTrigger.WEEKLY }
        val weeklySlotsLeft    = (RetencionPuntos.CUPO_SEMANALES - localWeeklyCount).coerceAtLeast(0)
        val nonWeeklySlotsLeft = (RetencionPuntos.CUPO_COMPARTIDO - localNonWeeklyCount).coerceAtLeast(0)

        val combinados = dao.getAllNonWeekly()
            .map { RetencionPuntos.Punto(it.id, it.timestamp, it.trigger == SnapshotTrigger.MANUAL.name) } +
            driveNonWeekly.map { RetencionPuntos.Punto(it.snapshotId, it.timestamp, it.trigger == SnapshotTrigger.MANUAL) }
        val descartados = RetencionPuntos.aBorrar(combinados)

        (driveWeekly.take(weeklySlotsLeft) +
            driveNonWeekly.filter { it.snapshotId !in descartados }.take(nonWeeklySlotsLeft)).forEach { info ->
            runCatching { localizeSnapshotFromDrive(info) }
        }
    }

    @Throws(SessionExpiredException::class)
    suspend fun localizeSnapshotFromDrive(info: DriveSnapshotInfo) {
        val key  = session.getSecretKey() ?: throw SessionExpiredException()
        val json = downloadDriveSnapshotJson(info)

        val encrypted = crypto.encrypt(json, key)
        val fileName  = "${info.snapshotId}.snap"
        val file      = File(snapshotDir, fileName)
        file.writeText(encrypted)

        val entryCount = runCatching { ImportParser.parseKeyArmorJson(json).totalCount }.getOrDefault(0)

        dao.insert(
            VaultSnapshotEntity(
                id            = info.snapshotId,
                timestamp     = info.timestamp,
                trigger       = info.trigger.name,
                entryCount    = entryCount,
                filePath      = encField("snapshots/$fileName", key),
                fileSizeBytes = file.length(),
                syncedToDrive = true,
                includesAttachments = false
            )
        )
    }

    suspend fun prepareReEncryptSnapshots(
        oldKey: javax.crypto.spec.SecretKeySpec,
        newKey: javax.crypto.spec.SecretKeySpec
    ): List<SnapshotReEncryptPlan> {
        val all     = dao.getAll()
        val created = mutableListOf<File>()
        val plans   = mutableListOf<SnapshotReEncryptPlan>()
        return try {
            all.forEach { entity ->
                try {
                    val plainRelativePath = decField(entity.filePath, oldKey)
                    val original  = File(context.filesDir, plainRelativePath)
                    val tempFile  = File(snapshotDir, "${entity.id}.snap.new")
                    if (isKabFile(original)) {
                        reEncryptKabFile(original, tempFile, oldKey, newKey)
                    } else {
                        val plaintext = crypto.decrypt(original.readText(), oldKey)
                        tempFile.writeText(crypto.encrypt(plaintext, newKey))
                    }
                    created.add(tempFile)
                    plans.add(
                        SnapshotReEncryptPlan(
                            entity               = entity,
                            plainRelativePath    = plainRelativePath,
                            tempContentFile      = tempFile,
                            newEncryptedFilePath = encField(plainRelativePath, newKey)
                        )
                    )
                } catch (e: Exception) {
                    Log.w("SnapshotRepository", "prepareReEncryptSnapshots: snapshot ${entity.id} omitido (archivo ilegible)", e)
                }
            }
            plans
        } catch (e: Exception) {
            created.forEach { runCatching { it.delete() } }
            throw e
        }
    }

    suspend fun commitReEncryptedSnapshots(plans: List<SnapshotReEncryptPlan>) {
        plans.forEach { plan ->
            runCatching {
                val original = File(context.filesDir, plan.plainRelativePath)
                plan.tempContentFile.copyTo(original, overwrite = true)
                plan.tempContentFile.delete()
                dao.insert(
                    plan.entity.copy(
                        filePath      = plan.newEncryptedFilePath,
                        fileSizeBytes = original.length()
                    )
                )
            }
        }
    }

    suspend fun rollbackReEncryptedSnapshots(plans: List<SnapshotReEncryptPlan>) {
        plans.forEach { runCatching { it.tempContentFile.delete() } }
    }

    private suspend fun downloadDriveSnapshotJson(info: DriveSnapshotInfo, claveManual: ByteArray? = null): String {
        val account  = driveAccount()  ?: error("Drive no configurado")
        val password = drivePassword()
        if (claveManual != null) {
            return driveBackupRepository.downloadAndDecryptSnapshot(account, password, info.driveFileId, claveManual)
        }
        return claveDeRespaldo.paraRestaurar { clave ->
            if (clave == null && password == null) error("NO_BACKUP_PASSWORD")
            driveBackupRepository.downloadAndDecryptSnapshot(account, password, info.driveFileId, clave)
        }
    }

    private fun driveAccount(): Account? {
        val email = drivePrefs.getConnectedEmail() ?: return null
        return Account(email, "com.google")
    }

    private fun drivePassword(): String? =
        (drivePrefs.loadPassword() as? DrivePrefs.LoadResult.Success)?.password

    private suspend fun restoreRemindersAndNoteHistory(result: com.thormakk.keyarmor.data.export.ImportResult) {
        runCatching { reminderRepository.restoreFromBackup(result.reminders) }
            .onFailure { Log.e("SnapshotRepository", "No se pudieron restaurar los recordatorios", it) }
        runCatching { secureNoteHistoryRepository.restoreFromBackup(result.secureNoteHistory) }
            .onFailure { Log.e("SnapshotRepository", "No se pudo restaurar el historial de notas", it) }
    }

    private fun restoreHeirIdentity(result: com.thormakk.keyarmor.data.export.ImportResult) {
        result.inheritanceIdentity?.let { id ->
            runCatching { heirIdentityManager.importIdentity(id.publicKeyB64, id.privateKeyCode) }
                .onFailure { Log.w("SnapshotRepository", "No se pudo restaurar la identidad del heredero desde snapshot", it) }
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
                .onFailure { Log.w("SnapshotRepository", "No se pudo restaurar la identidad de firma del dueño desde snapshot", it) }
        }
    }

    private suspend fun reasignarHuerfanasAlFinal() {
        runCatching { vaultMetadataRepository.reasignarEntradasHuerfanas() }
            .onFailure { Log.e("SnapshotRepository", "No se pudieron reasignar las huérfanas", it) }
    }

    private suspend fun executeReplace(json: String) {
        executeReplaceTx(json)
        tagRepository.reconcileAfterRestore()
        desocultarSiNoHayFrase()
    }

    private suspend fun desocultarSiNoHayFrase() {
        if (hiddenEntriesPrefs.isEnabled()) return
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

    private suspend fun executeReplaceTx(json: String) = db.withTransaction {
        val result = ImportParser.parseKeyArmorJson(json)
        restoreHeirIdentity(result)
        restoreRemindersAndNoteHistory(result)
        val root   = JSONObject(json)
        val fotosPrevias = identityDocumentRepository.capturePhotoPaths()
        wipeRepository.wipeAll()
        pinnedHubPrefs.unpinAll()
        result.vaults.forEach { vaultMetadataRepository.save(it) }
        vaultRepository.saveAll(result.passwords)
        twoFactorRepository.saveAll(result.twoFactor)
        passkeyRepository.saveAll(result.passkeys)
        bankAccountRepository.saveAll(result.bankAccounts)
        creditCardRepository.saveAll(result.creditCards)
        secureNoteRepository.saveAll(result.secureNotes)
        customEntryRepository.saveAll(result.customEntries)
        identityDocumentRepository.saveAll(result.identityDocuments)
        identityDocumentRepository.restorePhotoPaths(fotosPrevias)
        vehicleRepository.saveAll(result.vehicles)
        cryptoWalletRepository.saveAll(result.cryptoWallets)
        val tags = parseTags(root)
        if (tags.isNotEmpty()) tagRepository.upsertAll(tags)
        root.optJSONArray("pinned")?.let { pinnedHubPrefs.restoreFromBackup(PinnedHubPrefs.fromJson(it)) }
        val history = parsePasswordHistory(root)
        if (history.isNotEmpty()) passwordHistoryRepository.upsertAll(history)
        parseAttachments(root).forEach { a ->
            if (a.kind != KIND_FILE) {
                identityDocumentRepository.restorePhotoFromBackup(
                    documentId = a.entryId,
                    isFront    = a.kind == KIND_PHOTO_FRONT,
                    encBytes   = a.encBytes
                )
                return@forEach
            }
            attachmentRepository.restoreFromSnapshot(
                AttachmentEntity(
                    id         = a.id,
                    entryId    = a.entryId,
                    fileName   = a.fileName,
                    mimeType   = a.mimeType,
                    fileSize   = a.fileSize,
                    storedPath = "",
                    createdAt  = a.createdAt
                ),
                a.encBytes
            )
        }
        vaultMetadataRepository.healSystemVaultFlags()
        reasignarHuerfanasAlFinal()
    }

    private fun contentKey(e: com.thormakk.keyarmor.domain.model.VaultEntry)        = entryContentKey.of(e)
    private fun contentKey(e: com.thormakk.keyarmor.domain.model.TwoFactorAccount)  = entryContentKey.of(e)
    private fun contentKey(e: com.thormakk.keyarmor.domain.model.PasskeyCredential) = entryContentKey.of(e)
    private fun contentKey(e: com.thormakk.keyarmor.domain.model.BankAccount)       = entryContentKey.of(e)
    private fun contentKey(e: com.thormakk.keyarmor.domain.model.CreditCard)        = entryContentKey.of(e)
    private fun contentKey(e: com.thormakk.keyarmor.domain.model.SecureNote)        = entryContentKey.of(e)
    private fun contentKey(e: com.thormakk.keyarmor.domain.model.CustomEntry)       = entryContentKey.of(e)
    private fun contentKey(e: com.thormakk.keyarmor.domain.model.IdentityDocument)  = entryContentKey.of(e)
    private fun contentKey(e: com.thormakk.keyarmor.domain.model.Vehicle)           = entryContentKey.of(e)
    private fun contentKey(e: com.thormakk.keyarmor.domain.model.CryptoWallet)      = entryContentKey.of(e)

    private suspend fun contarNuevosParaFusionar(json: String): Int {
        val r = ImportParser.parseKeyArmorJson(json)
        fun <T> nuevos(items: List<T>, existentes: List<T>, id: (T) -> String, clave: (T) -> Any): Int {
            val ids = existentes.map(id).toSet()
            val claves = existentes.map(clave).toSet()
            return items.count { id(it) !in ids && clave(it) !in claves }
        }
        return nuevos(r.passwords, vaultRepository.getAllForBackup(), { it.id }, { contentKey(it) }) +
            nuevos(r.twoFactor, twoFactorRepository.getAllForBackup(), { it.id }, { contentKey(it) }) +
            nuevos(r.passkeys, passkeyRepository.getAllForBackup(), { it.credentialId }, { contentKey(it) }) +
            nuevos(r.bankAccounts, bankAccountRepository.getAllForBackup(), { it.id }, { contentKey(it) }) +
            nuevos(r.creditCards, creditCardRepository.getAllForBackup(), { it.id }, { contentKey(it) }) +
            nuevos(r.secureNotes, secureNoteRepository.getAllForBackup(), { it.id }, { contentKey(it) }) +
            nuevos(r.customEntries, customEntryRepository.getAllForBackup(), { it.id }, { contentKey(it) }) +
            nuevos(r.identityDocuments, identityDocumentRepository.getAllForBackup(), { it.id }, { contentKey(it) }) +
            nuevos(r.vehicles, vehicleRepository.getAllForBackup(), { it.id }, { contentKey(it) }) +
            nuevos(r.cryptoWallets, cryptoWalletRepository.getAllForBackup(), { it.id }, { contentKey(it) })
    }

    private suspend fun executeMerge(json: String): RestoreSummary = executeMergeTx(json).also { tagRepository.reconcileAfterRestore(); desocultarSiNoHayFrase() }

    private suspend fun executeMergeTx(json: String): RestoreSummary = db.withTransaction {
        val result = ImportParser.parseKeyArmorJson(json)
        restoreHeirIdentity(result)
        restoreRemindersAndNoteHistory(result)
        val root   = JSONObject(json)
        val existingVault         = vaultRepository.getAllForBackup()
        val existingVaultEntryIds = existingVault.map { it.id }.toSet()
        val existingVaultContent  = existingVault.map(::contentKey).toSet()
        val newPasswords = result.passwords.filter {
            it.id !in existingVaultEntryIds && contentKey(it) !in existingVaultContent
        }

        val existingTwoFactor        = twoFactorRepository.getAllForBackup()
        val existingTwoFactorIds     = existingTwoFactor.map { it.id }.toSet()
        val existingTwoFactorContent = existingTwoFactor.map(::contentKey).toSet()
        val newTwoFactor = result.twoFactor.filter {
            it.id !in existingTwoFactorIds && contentKey(it) !in existingTwoFactorContent
        }

        val existingPasskeys        = passkeyRepository.getAllForBackup()
        val existingPasskeyIds      = existingPasskeys.map { it.credentialId }.toSet()
        val existingPasskeyContent  = existingPasskeys.map(::contentKey).toSet()
        val newPasskeys = result.passkeys.filter {
            it.credentialId !in existingPasskeyIds && contentKey(it) !in existingPasskeyContent
        }

        val existingBankAccounts        = bankAccountRepository.getAllForBackup()
        val existingBankAccountIds      = existingBankAccounts.map { it.id }.toSet()
        val existingBankAccountContent  = existingBankAccounts.map(::contentKey).toSet()
        val newBankAccounts = result.bankAccounts.filter {
            it.id !in existingBankAccountIds && contentKey(it) !in existingBankAccountContent
        }

        val existingCreditCards        = creditCardRepository.getAllForBackup()
        val existingCreditCardIds      = existingCreditCards.map { it.id }.toSet()
        val existingCreditCardContent  = existingCreditCards.map(::contentKey).toSet()
        val newCreditCards = result.creditCards.filter {
            it.id !in existingCreditCardIds && contentKey(it) !in existingCreditCardContent
        }

        val existingSecureNotes        = secureNoteRepository.getAllForBackup()
        val existingSecureNoteIds      = existingSecureNotes.map { it.id }.toSet()
        val existingSecureNoteContent  = existingSecureNotes.map(::contentKey).toSet()
        val newSecureNotes = result.secureNotes.filter {
            it.id !in existingSecureNoteIds && contentKey(it) !in existingSecureNoteContent
        }

        val existingCustomEntries        = customEntryRepository.getAllForBackup()
        val existingCustomEntryIds       = existingCustomEntries.map { it.id }.toSet()
        val existingCustomEntryContent   = existingCustomEntries.map(::contentKey).toSet()
        val newCustomEntries = result.customEntries.filter {
            it.id !in existingCustomEntryIds && contentKey(it) !in existingCustomEntryContent
        }

        val existingIdentityDocuments        = identityDocumentRepository.getAllForBackup()
        val existingIdentityDocumentIds      = existingIdentityDocuments.map { it.id }.toSet()
        val existingIdentityDocumentContent  = existingIdentityDocuments.map(::contentKey).toSet()
        val newIdentityDocuments = result.identityDocuments.filter {
            it.id !in existingIdentityDocumentIds && contentKey(it) !in existingIdentityDocumentContent
        }

        val existingVehicles        = vehicleRepository.getAllForBackup()
        val existingVehicleIds      = existingVehicles.map { it.id }.toSet()
        val existingVehicleContent  = existingVehicles.map(::contentKey).toSet()
        val newVehicles = result.vehicles.filter {
            it.id !in existingVehicleIds && contentKey(it) !in existingVehicleContent
        }

        val existingCryptoWallets        = cryptoWalletRepository.getAllForBackup()
        val existingCryptoWalletIds      = existingCryptoWallets.map { it.id }.toSet()
        val existingCryptoWalletContent  = existingCryptoWallets.map(::contentKey).toSet()
        val newCryptoWallets = result.cryptoWallets.filter {
            it.id !in existingCryptoWalletIds && contentKey(it) !in existingCryptoWalletContent
        }

        if (newPasswords.isNotEmpty())         vaultRepository.saveAll(newPasswords)
        if (newTwoFactor.isNotEmpty())         twoFactorRepository.saveAll(newTwoFactor)
        if (newPasskeys.isNotEmpty())          passkeyRepository.saveAll(newPasskeys)
        if (newBankAccounts.isNotEmpty())      bankAccountRepository.saveAll(newBankAccounts)
        if (newCreditCards.isNotEmpty())       creditCardRepository.saveAll(newCreditCards)
        if (newSecureNotes.isNotEmpty())       secureNoteRepository.saveAll(newSecureNotes)
        if (newCustomEntries.isNotEmpty())     customEntryRepository.saveAll(newCustomEntries)
        if (newIdentityDocuments.isNotEmpty()) identityDocumentRepository.saveAll(newIdentityDocuments)
        if (newVehicles.isNotEmpty())          vehicleRepository.saveAll(newVehicles)
        if (newCryptoWallets.isNotEmpty())     cryptoWalletRepository.saveAll(newCryptoWallets)
        val existingVaultIds = vaultMetadataRepository.getAll().map { it.id }.toSet()
        result.vaults.filter { it.id !in existingVaultIds }.forEach { vaultMetadataRepository.save(it) }

        val existingTagIds = tagRepository.getAllForBackup().map { it.id }.toSet()
        val newTags = parseTags(root).filter { it.id !in existingTagIds }
        if (newTags.isNotEmpty()) tagRepository.upsertAll(newTags)
        root.optJSONArray("pinned")?.let {
            pinnedHubPrefs.restoreFromBackup(PinnedHubPrefs.fromJson(it), completar = true)
        }

        val existingHistoryKeys = passwordHistoryRepository.getAllForBackup()
            .map { Triple(it.entryId, it.passwordHash, it.savedAt) }
            .toSet()
        val newHistory = parsePasswordHistory(root)
            .filter { Triple(it.entryId, it.passwordHash, it.savedAt) !in existingHistoryKeys }
            .map { it.copy(id = 0) }
        if (newHistory.isNotEmpty()) passwordHistoryRepository.upsertAll(newHistory)

        val existingAttachmentIds = attachmentRepository.getAllForBackup().map { it.id }.toSet()
        parseAttachments(root)
            .filter { it.kind != KIND_FILE || it.id !in existingAttachmentIds }
            .forEach { a ->
                if (a.kind != KIND_FILE) {
                    identityDocumentRepository.restorePhotoFromBackup(
                        documentId = a.entryId,
                        isFront    = a.kind == KIND_PHOTO_FRONT,
                        encBytes   = a.encBytes
                    )
                    return@forEach
                }
                attachmentRepository.restoreFromSnapshot(
                    AttachmentEntity(
                        id         = a.id,
                        entryId    = a.entryId,
                        fileName   = a.fileName,
                        mimeType   = a.mimeType,
                        fileSize   = a.fileSize,
                        storedPath = "",
                        createdAt  = a.createdAt
                    ),
                    a.encBytes
                )
            }

        val restored = newPasswords.size + newTwoFactor.size + newPasskeys.size +
                       newBankAccounts.size + newCreditCards.size + newSecureNotes.size +
                       newCustomEntries.size + newIdentityDocuments.size +
                       newVehicles.size + newCryptoWallets.size
        vaultMetadataRepository.healSystemVaultFlags()
        reasignarHuerfanasAlFinal()
        RestoreSummary(restored = restored, overwritten = 0)
    }

    private suspend fun executeAddAll(json: String): Map<String, String> = executeAddAllTx(json).also { tagRepository.reconcileAfterRestore(); desocultarSiNoHayFrase() }

    private suspend fun executeAddAllTx(json: String): Map<String, String> = db.withTransaction {
        val result = ImportParser.parseKeyArmorJson(json)
        restoreHeirIdentity(result)
        restoreRemindersAndNoteHistory(result)
        val root   = JSONObject(json)

        val passwordIdMap         = result.passwords.associate         { it.id to UUID.randomUUID().toString() }
        val bankAccountIdMap      = result.bankAccounts.associate      { it.id to UUID.randomUUID().toString() }
        val creditCardIdMap       = result.creditCards.associate       { it.id to UUID.randomUUID().toString() }
        val secureNoteIdMap       = result.secureNotes.associate       { it.id to UUID.randomUUID().toString() }
        val customEntryIdMap      = result.customEntries.associate     { it.id to UUID.randomUUID().toString() }
        val identityDocumentIdMap = result.identityDocuments.associate { it.id to UUID.randomUUID().toString() }
        val vehicleIdMap          = result.vehicles.associate          { it.id to UUID.randomUUID().toString() }
        val cryptoWalletIdMap     = result.cryptoWallets.associate     { it.id to UUID.randomUUID().toString() }

        vaultRepository.saveAll(result.passwords.map                   { it.copy(id = passwordIdMap.getValue(it.id)) })
        twoFactorRepository.saveAll(result.twoFactor.map                { it.copy(id = UUID.randomUUID().toString()) })
        passkeyRepository.saveAll(result.passkeys.map                   { it.copy(credentialId = UUID.randomUUID().toString()) })
        bankAccountRepository.saveAll(result.bankAccounts.map           { it.copy(id = bankAccountIdMap.getValue(it.id)) })
        creditCardRepository.saveAll(result.creditCards.map             { it.copy(id = creditCardIdMap.getValue(it.id)) })
        secureNoteRepository.saveAll(result.secureNotes.map             { it.copy(id = secureNoteIdMap.getValue(it.id)) })
        customEntryRepository.saveAll(result.customEntries.map          { it.copy(id = customEntryIdMap.getValue(it.id)) })
        identityDocumentRepository.saveAll(result.identityDocuments.map { it.copy(id = identityDocumentIdMap.getValue(it.id)) })
        vehicleRepository.saveAll(result.vehicles.map                   { it.copy(id = vehicleIdMap.getValue(it.id)) })
        cryptoWalletRepository.saveAll(result.cryptoWallets.map         { it.copy(id = cryptoWalletIdMap.getValue(it.id)) })
        result.vaults.forEach { vaultMetadataRepository.save(it) }
        parseTags(root).takeIf { it.isNotEmpty() }?.let {
            tagRepository.upsertAll(it.map { t -> t.copy(id = UUID.randomUUID().toString()) })
        }
        root.optJSONArray("pinned")?.let {
            val mapaIds = passwordIdMap + bankAccountIdMap + creditCardIdMap + secureNoteIdMap +
                customEntryIdMap + identityDocumentIdMap + vehicleIdMap + cryptoWalletIdMap
            pinnedHubPrefs.restoreFromBackup(PinnedHubPrefs.fromJson(it), mapaIds, completar = true)
        }
        parsePasswordHistory(root).takeIf { it.isNotEmpty() }?.let {
            passwordHistoryRepository.upsertAll(it.map { h -> h.copy(id = 0) })
        }

        val entryIdMap = passwordIdMap + bankAccountIdMap + creditCardIdMap + secureNoteIdMap +
                         customEntryIdMap + identityDocumentIdMap + vehicleIdMap + cryptoWalletIdMap
        parseAttachments(root).forEach { a ->
            val newEntryId = entryIdMap[a.entryId] ?: return@forEach
            if (a.kind != KIND_FILE) {
                identityDocumentRepository.restorePhotoFromBackup(
                    documentId = newEntryId,
                    isFront    = a.kind == KIND_PHOTO_FRONT,
                    encBytes   = a.encBytes
                )
                return@forEach
            }
            attachmentRepository.restoreFromSnapshot(
                AttachmentEntity(
                    id         = UUID.randomUUID().toString(),
                    entryId    = newEntryId,
                    fileName   = a.fileName,
                    mimeType   = a.mimeType,
                    fileSize   = a.fileSize,
                    storedPath = "",
                    createdAt  = a.createdAt
                ),
                a.encBytes
            )
        }

        vaultMetadataRepository.healSystemVaultFlags()
        reasignarHuerfanasAlFinal()
        entryIdMap
    }

    private fun VaultSnapshotEntity.toDomain(key: SecretKeySpec) = VaultSnapshot(
        id            = id,
        timestamp     = timestamp,
        trigger       = SnapshotTrigger.valueOf(trigger),
        entryCount    = entryCount,
        filePath      = decField(filePath, key),
        fileSizeBytes = fileSizeBytes,
        syncedToDrive = syncedToDrive,
        includesAttachments = includesAttachments
    )
}

class EmptyVaultSkippedException : Exception("Bóveda vacía: no se crea el punto semanal")
