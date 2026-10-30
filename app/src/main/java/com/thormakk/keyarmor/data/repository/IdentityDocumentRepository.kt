@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.content.Context
import android.util.Log
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.database.dao.IdentityDocumentDao
import com.thormakk.keyarmor.data.database.entity.IdentityDocumentEntity
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.prefs.TravelModePrefs
import com.thormakk.keyarmor.data.storage.AttachmentStorage
import com.thormakk.keyarmor.di.DefaultDispatcher
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.IdentityDocument
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.SessionExpiredException
import com.thormakk.keyarmor.security.SessionManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_DECRYPT = "IdentityDocumentRepository"

@Singleton
class IdentityDocumentRepository @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val dao:               IdentityDocumentDao,
    private val crypto:            CryptoManager,
    private val session:           SessionManager,
    private val panicPinPrefs:     PanicPinPrefs,
    private val travelModePrefs:   TravelModePrefs,
    private val activeVaultPrefs:  ActiveVaultPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    private val attachmentStorage: AttachmentStorage,
    private val attachmentRepository: AttachmentRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    fun observeDocuments(): Flow<List<IdentityDocument>> =
        combine(panicPinPrefs.panicModeFlow, travelModePrefs.travelModeFlow, activeVaultPrefs.activeVaultIdFlow) { p, t, v -> Triple(p, t, v) }
            .flatMapLatest { (isPanic, isTravel, vaultId) ->
                if (isPanic && vaultId !in VaultMetadata.HIDE_VAULT_IDS) flowOf(emptyList())
                else session.isUnlockedFlow.flatMapLatest { unlocked ->
                    val key = if (unlocked) session.getSecretKey() else null
                    if (key == null) flowOf(emptyList())
                    else dao.observeActive(vaultId).map { list ->
                        val items = list.mapNotNull { decryptWith(it, key) }.filterNot { it.isHidden }
                        if (isTravel) items.filter { it.isTravelSafe } else items
                    }
                }
            }
            .flowOn(defaultDispatcher)

    fun observeTrash(): Flow<List<IdentityDocument>> =
        combine(panicPinPrefs.panicModeFlow, travelModePrefs.travelModeFlow, activeVaultPrefs.activeVaultIdFlow) { p, t, v -> Triple(p, t, v) }
            .flatMapLatest { (isPanic, isTravel, vaultId) ->
                if (isPanic && vaultId !in VaultMetadata.HIDE_VAULT_IDS) flowOf(emptyList())
                else session.isUnlockedFlow.flatMapLatest { unlocked ->
                    val key = if (unlocked) session.getSecretKey() else null
                    if (key == null) flowOf(emptyList())
                    else dao.observeDeleted(vaultId).map { list ->
                        val items = list.mapNotNull { decryptWith(it, key) }.filterNot { it.isHidden }
                        if (isTravel) items.filter { it.isTravelSafe } else items
                    }
                }
            }
            .flowOn(defaultDispatcher)

    fun observeArchived(): Flow<List<IdentityDocument>> =
        combine(panicPinPrefs.panicModeFlow, travelModePrefs.travelModeFlow, activeVaultPrefs.activeVaultIdFlow) { p, t, v -> Triple(p, t, v) }
            .flatMapLatest { (isPanic, isTravel, vaultId) ->
                if (isPanic && vaultId !in VaultMetadata.HIDE_VAULT_IDS) flowOf(emptyList())
                else session.isUnlockedFlow.flatMapLatest { unlocked ->
                    val key = if (unlocked) session.getSecretKey() else null
                    if (key == null) flowOf(emptyList())
                    else dao.observeArchived(vaultId).map { list ->
                        val items = list.mapNotNull { decryptWith(it, key) }.filterNot { it.isHidden }
                        if (isTravel) items.filter { it.isTravelSafe } else items
                    }
                }
            }
            .flowOn(defaultDispatcher)

    fun observeAllForSearch(): Flow<List<IdentityDocument>> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(emptyList())
            else combine(dao.observeAll(), hiddenEntriesPrefs.revealActiveFlow, travelModePrefs.travelModeFlow) { list, reveal, isTravel ->
                val items = list.mapNotNull { decryptWith(it, key) }.filter { it.isHidden == reveal }
                if (isTravel) items.filter { it.isTravelSafe } else items
            }
        }
            .flowOn(defaultDispatcher)


    suspend fun getDocument(id: String): IdentityDocument? {
        val key = session.getSecretKey() ?: return null
        val doc = dao.getById(id)?.let { decryptWith(it, key) } ?: return null
        if (panicPinPrefs.panicModeFlow.value && doc.vaultId !in VaultMetadata.HIDE_VAULT_IDS) return null
        return if (travelModePrefs.travelModeFlow.value && !doc.isTravelSafe) null else doc
    }

    fun observeDocumentById(id: String): Flow<IdentityDocument?> =
        combine(panicPinPrefs.panicModeFlow, travelModePrefs.travelModeFlow) { p, t -> p to t }
            .flatMapLatest { (isPanic, isTravel) ->
                session.isUnlockedFlow.flatMapLatest { unlocked ->
                    val key = if (unlocked) session.getSecretKey() else null
                    if (key == null) flowOf(null)
                    else dao.observeById(id).map { entity ->
                        val doc = entity?.let { decryptWith(it, key) }
                        when {
                            doc == null -> null
                            isPanic && doc.vaultId !in VaultMetadata.HIDE_VAULT_IDS -> null
                            isTravel && !doc.isTravelSafe -> null
                            else -> doc
                        }
                    }
                }
            }
            .flowOn(defaultDispatcher)

    @Throws(SessionExpiredException::class)
    suspend fun saveDocument(doc: IdentityDocument, soloMetadatos: Boolean = false): Boolean {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        val protegida = isProtected(doc.id)
        if (protegida && !soloMetadatos) return false
        dao.upsert(encrypt(if (soloMetadatos) doc.copy(isProtected = protegida) else doc, key))
        return true
    }

    @Throws(SessionExpiredException::class)
    suspend fun saveAll(items: List<IdentityDocument>): Int {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        val protegidas = protectedAmong(items.map { it.id })
        val aGuardar   = if (protegidas.isEmpty()) items else items.filterNot { it.id in protegidas }
        val entidades = withContext(defaultDispatcher) { aGuardar.map { encrypt(it, key) } }
        dao.upsertAll(entidades)
        return protegidas.size
    }

    suspend fun capturePhotoPaths(): Map<String, Pair<String?, String?>> =
        dao.getAllEntities()
            .filter { it.frontPhotoPath != null || it.backPhotoPath != null }
            .associate { it.id to (it.frontPhotoPath to it.backPhotoPath) }

    suspend fun restorePhotoPaths(paths: Map<String, Pair<String?, String?>>) {
        if (paths.isEmpty()) return
        val actualizadas = dao.getAllEntities().mapNotNull { entity ->
            val (front, back) = paths[entity.id] ?: return@mapNotNull null
            if (entity.frontPhotoPath != null && entity.backPhotoPath != null) return@mapNotNull null
            entity.copy(
                frontPhotoPath = entity.frontPhotoPath ?: front,
                backPhotoPath  = entity.backPhotoPath  ?: back,
            )
        }
        if (actualizadas.isNotEmpty()) dao.upsertAll(actualizadas)
    }

    suspend fun setFavorite(id: String, isFavorite: Boolean) = dao.setFavorite(id, isFavorite)
    suspend fun setTravelSafe(id: String, isTravelSafe: Boolean) = dao.setTravelSafe(id, isTravelSafe)
    suspend fun setHidden(id: String, isHidden: Boolean) = dao.setHidden(id, isHidden)
    suspend fun unhideAll() = dao.unhideAll()

    suspend fun isProtected(id: String): Boolean = dao.getProtectedIds(listOf(id)).isNotEmpty()
    suspend fun setProtected(id: String, isProtected: Boolean) = dao.setProtected(id, isProtected)
    suspend fun protectedAmong(ids: List<String>): Set<String> =
        ids.chunked(500).flatMap { dao.getProtectedIds(it) }.toSet()

    suspend fun savePhoto(id: String, bytes: ByteArray, isFront: Boolean) {
        val key  = session.getSecretKey() ?: return
        val path = attachmentStorage.saveEncrypted(bytes.inputStream(), key)
        if (isFront) dao.setFrontPhotoPath(id, path)
        else         dao.setBackPhotoPath(id, path)
    }

    data class PhotoForBackup(
        val documentId: String,
        val isFront:    Boolean,
        val storedPath: String,
    )

    suspend fun photosForBackup(): List<PhotoForBackup> =
        dao.getAllEntities().flatMap { e ->
            listOfNotNull(
                e.frontPhotoPath?.takeIf { it.isNotBlank() }
                    ?.let { PhotoForBackup(e.id, isFront = true,  storedPath = it) },
                e.backPhotoPath?.takeIf { it.isNotBlank() }
                    ?.let { PhotoForBackup(e.id, isFront = false, storedPath = it) },
            )
        }

    suspend fun restorePhotoFromBackup(documentId: String, isFront: Boolean, encBytes: ByteArray) {
        val path = attachmentStorage.saveRawEncrypted(encBytes)
        if (isFront) dao.setFrontPhotoPath(documentId, path)
        else         dao.setBackPhotoPath(documentId, path)
    }

    suspend fun photoBackupSources(): List<AttachmentBackupSource> {
        val key = session.getSecretKey() ?: return emptyList()
        return photosForBackup().map { f ->
            AttachmentBackupSource(
                id           = "idphoto_${f.documentId}_${if (f.isFront) "front" else "back"}",
                entryId      = f.documentId,
                fileName     = "",
                mimeType     = "image/jpeg",
                fileSize     = 0L,
                createdAt    = System.currentTimeMillis(),
                plainContent = { runCatching { attachmentStorage.decryptToBytes(f.storedPath, key) }.getOrNull() },
                kind         = if (f.isFront) "ID_PHOTO_FRONT" else "ID_PHOTO_BACK",
            )
        }
    }

    suspend fun restoreDecryptedPhoto(documentId: String, isFront: Boolean, plainBytes: ByteArray): Boolean {
        val key = session.getSecretKey() ?: return false
        val path = attachmentStorage.saveEncrypted(plainBytes.inputStream(), key)
        if (isFront) dao.setFrontPhotoPath(documentId, path)
        else         dao.setBackPhotoPath(documentId, path)
        return true
    }

    suspend fun deletePhoto(id: String, existingPath: String, isFront: Boolean) {
        attachmentStorage.delete(existingPath)
        if (isFront) dao.setFrontPhotoPath(id, null)
        else         dao.setBackPhotoPath(id, null)
    }

    suspend fun prepareReEncryptPhotos(
        oldKey: javax.crypto.spec.SecretKeySpec,
        newKey: javax.crypto.spec.SecretKeySpec
    ): List<Pair<String, String>> {
        val creados = mutableListOf<String>()
        val pares   = mutableListOf<Pair<String, String>>()
        return try {
            dao.getAllEntities().forEach { entity ->
                listOfNotNull(entity.frontPhotoPath, entity.backPhotoPath).forEach { rutaVieja ->
                    try {
                        val bytes = attachmentStorage.decryptToBytes(rutaVieja, oldKey)
                        val rutaNueva = attachmentStorage.saveEncrypted(bytes.inputStream(), newKey)
                        creados.add(rutaNueva)
                        pares.add(rutaVieja to rutaNueva)
                    } catch (e: Exception) {
                        android.util.Log.w(
                            "IdentityDocumentRepo",
                            "prepareReEncryptPhotos: foto omitida (archivo ilegible)", e
                        )
                    }
                }
            }
            pares
        } catch (e: Exception) {
            creados.forEach { runCatching { attachmentStorage.delete(it) } }
            throw e
        }
    }

    suspend fun commitPhotoPathUpdates(pathMap: Map<String, String>) {
        dao.getAllEntities().forEach { entity ->
            val front = entity.frontPhotoPath?.let { pathMap[it] }
            val back  = entity.backPhotoPath?.let { pathMap[it] }
            if (front == null && back == null) return@forEach
            dao.upsertAll(
                listOf(
                    entity.copy(
                        frontPhotoPath = front ?: entity.frontPhotoPath,
                        backPhotoPath  = back ?: entity.backPhotoPath,
                    )
                )
            )
        }
    }

    suspend fun deleteOldPhotoFiles(rutasViejas: List<String>) {
        rutasViejas.forEach { runCatching { attachmentStorage.delete(it) } }
    }

    suspend fun decryptPhoto(path: String): ByteArray? {
        val key = session.getSecretKey() ?: return null
        return runCatching { attachmentStorage.decryptToBytes(path, key) }.getOrNull()
    }

    suspend fun countAllByVault(vaultId: String): Int = dao.countAllByVault(vaultId)
    suspend fun countAllDocuments(): Int = dao.countAll()
    suspend fun softDelete(id: String): Boolean {
        if (isProtected(id)) return false
        if (dao.getById(id)?.isHidden == true) deletePermanently(id) else dao.softDelete(id)
        return true
    }
    suspend fun softDeleteBatch(ids: List<String>): Int {
        val protegidas = protectedAmong(ids)
        val libres     = ids.filterNot { it in protegidas }
        val (hidden, normal) = libres.partition { dao.getById(it)?.isHidden == true }
        hidden.forEach { deletePermanently(it) }
        if (normal.isNotEmpty()) dao.softDeleteBatch(normal)
        return protegidas.size
    }
    suspend fun restore(id: String) = dao.restore(id)
    suspend fun deletePermanently(id: String): Boolean {
        if (isProtected(id)) return false
        borrarSinChequear(id)
        return true
    }

    private suspend fun borrarSinChequear(id: String) {
        val entity = dao.getById(id)
        entity?.frontPhotoPath?.let { attachmentStorage.delete(it) }
        entity?.backPhotoPath?.let  { attachmentStorage.delete(it) }
        dao.deletePermanently(id)
        attachmentRepository.deleteAllForEntry(id)
    }

    suspend fun emptyTrash() {
        val vaultId = activeVaultPrefs.activeVaultId
        val trashedIds = dao.getTrashedIds(vaultId)
        val trashedEntities = trashedIds.mapNotNull { dao.getById(it) }
        dao.emptyTrash(vaultId)
        trashedEntities.forEach { entity ->
            entity.frontPhotoPath?.let { attachmentStorage.delete(it) }
            entity.backPhotoPath?.let  { attachmentStorage.delete(it) }
        }
        trashedIds.forEach { attachmentRepository.deleteAllForEntry(it) }
    }
    suspend fun getExpiredTrashedIds(cutoff: Long): List<String> = dao.getExpiredTrashedIds(cutoff)

    suspend fun deleteAll() = dao.deleteAll()
    suspend fun archive(id: String) {
        if (dao.getById(id)?.isHidden == true) return
        dao.archive(id)
    }
    suspend fun archiveBatch(ids: List<String>) {
        val eligible = ids.filterNot { dao.getById(it)?.isHidden == true }
        if (eligible.isNotEmpty()) dao.archiveBatch(eligible)
    }
    suspend fun unarchive(id: String) = dao.unarchive(id)
    suspend fun setPin(id: String, isPinned: Boolean) = dao.setPin(id, isPinned)
    suspend fun countPinned(): Int = dao.countPinned(activeVaultPrefs.activeVaultId)
    suspend fun getExpiryTimestamps(): List<Long> = dao.getExpiryTimestamps()

    suspend fun getExpiringEntryIds(now: Long, windowMs: Long): List<String> =
        dao.getExpiringIds(now, now + windowMs)

    suspend fun getExpiringEntryPairs(now: Long, windowMs: Long): List<Pair<String, String>> {
        session.getSecretKey() ?: return emptyList()
        return dao.getExpiringIds(now, now + windowMs)
            .mapNotNull { id ->
                val doc = getDocument(id) ?: return@mapNotNull null
                val label = doc.holderName.ifEmpty { doc.docType.ifEmpty { context.getString(R.string.archive_type_identity_document) } }
                id to label
            }
    }
    suspend fun archiveAll(vaultId: String)                      = dao.archiveAll(vaultId)
    suspend fun getAllIds(): Set<String>           = dao.getAllIds().toSet()
    suspend fun getAllForBackup(): List<IdentityDocument> {
        val key = session.getSecretKey() ?: return emptyList()
        val entidades = dao.getAllEntities()
        return withContext(defaultDispatcher) {
            entidades.filter { it.vaultId !in VaultMetadata.COERCION_VAULT_IDS }
                .mapNotNull { entity ->
                    val item = decryptWith(entity, key)
                    if (item == null) {
                        Log.e(TAG_DECRYPT, "getAllForBackup: IdentityDocument id=${entity.id} ilegible, " +
                            "queda FUERA del backup")
                    }
                    item
                }
        }
    }

    private fun decryptWith(entity: IdentityDocumentEntity, key: SecretKeySpec): IdentityDocument? =
        try {
            fun dec(v: String) = if (v.isNotEmpty()) crypto.decrypt(v, key) else ""
            IdentityDocument(
                id             = entity.id,
                docType        = dec(entity.docType),
                number         = dec(entity.number),
                issuingCountry = dec(entity.issuingCountry),
                issueDate      = dec(entity.issueDate),
                expiryDate     = dec(entity.expiryDate),
                birthDate      = dec(entity.birthDate),
                holderName     = dec(entity.holderName),
                notes          = dec(entity.notes),
                expiryTimestamp  = entity.expiryTimestamp,
                expiryAlertDays  = entity.expiryAlertDays,
                category       = EntryCategory.entries.firstOrNull { it.name == entity.category }
                                     ?: EntryCategory.SIN_CATEGORIZAR,
                tags           = entity.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                isFavorite     = entity.isFavorite,
                isTravelSafe   = entity.isTravelSafe,
                isPinned       = entity.isPinned,
                isHidden       = entity.isHidden,
                isProtected = entity.isProtected,
                createdAt      = entity.createdAt,
                updatedAt      = entity.updatedAt,
                isDeleted      = entity.isDeleted,
                deletedAt      = entity.deletedAt,
                archivedAt     = entity.archivedAt,
                vaultId        = entity.vaultId,
                inheritedFrom  = entity.inheritedFrom,
                frontPhotoPath = entity.frontPhotoPath,
                backPhotoPath  = entity.backPhotoPath,
            )
        } catch (e: Exception) {
            Log.e(TAG_DECRYPT, "decrypt falló para id=${entity.id}", e)
            null
        }

    private fun encrypt(doc: IdentityDocument, key: SecretKeySpec): IdentityDocumentEntity {
        fun enc(v: String) = if (v.isNotEmpty()) crypto.encrypt(v, key) else ""
        return IdentityDocumentEntity(
            id             = doc.id,
            docType        = enc(doc.docType),
            number         = enc(doc.number),
            issuingCountry = enc(doc.issuingCountry),
            issueDate      = enc(doc.issueDate),
            expiryDate     = enc(doc.expiryDate),
            birthDate      = enc(doc.birthDate),
            holderName     = enc(doc.holderName),
            notes          = enc(doc.notes),
            expiryTimestamp  = doc.expiryTimestamp,
            expiryAlertDays  = doc.expiryAlertDays,
            category       = doc.category.name,
            tags           = doc.tags.joinToString(","),
            isFavorite     = doc.isFavorite,
            isTravelSafe   = doc.isTravelSafe,
            isPinned       = doc.isPinned,
            isHidden       = doc.isHidden,
            isProtected = doc.isProtected,
            createdAt      = doc.createdAt,
            updatedAt      = doc.updatedAt,
            isDeleted      = doc.isDeleted,
            deletedAt      = doc.deletedAt,
            archivedAt     = doc.archivedAt,
            vaultId          = doc.vaultId,
            inheritedFrom    = doc.inheritedFrom,
            frontPhotoPath   = doc.frontPhotoPath,
            backPhotoPath    = doc.backPhotoPath,
        )
    }

    suspend fun moveAllToVault(sourceVaultId: String, targetVaultId: String) =
        dao.moveToVault(sourceVaultId, targetVaultId)

    suspend fun reassignOrphansToVault(validVaultIds: List<String>, targetVaultId: String): Int =
        dao.reassignOrphansToVault(validVaultIds, targetVaultId)

    suspend fun unpinAllByVault(vaultId: String) = dao.unpinAllByVault(vaultId)

    suspend fun stampInheritedFrom(sourceVaultId: String, inheritedFrom: String) =
        dao.stampInheritedFrom(sourceVaultId, inheritedFrom)

    suspend fun trashAllByVault(vaultId: String): Int {
        val protegidas = dao.countProtectedByVault(vaultId)
        dao.softDeleteAllByVault(vaultId, System.currentTimeMillis())
        return protegidas
    }

    suspend fun moveEntriesToVault(ids: Set<String>, targetVaultId: String) {
        ids.forEach { dao.moveEntryToVault(it, targetVaultId) }
    }

    suspend fun copyEntriesToVault(ids: Set<String>, targetVaultId: String, inheritedFrom: String? = null) {
        ids.forEach { id ->
            val original = getDocument(id) ?: return@forEach
            val copy = original.copy(
                id = java.util.UUID.randomUUID().toString(),
                vaultId       = targetVaultId,
                inheritedFrom = inheritedFrom,
            isPinned      = false,
            createdAt     = System.currentTimeMillis(),
            updatedAt     = System.currentTimeMillis()
            )
            saveDocument(copy)
        }
    }

    suspend fun removeInheritedFrom(id: String) = dao.clearInheritedFrom(id)
}
