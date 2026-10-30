@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.util.Log
import com.thormakk.keyarmor.data.database.dao.PasswordHistoryDao
import com.thormakk.keyarmor.data.database.dao.VaultEntryDao
import com.thormakk.keyarmor.data.database.entity.PasswordHistoryEntity
import com.thormakk.keyarmor.data.database.entity.VaultEntryEntity
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.prefs.TravelModePrefs
import com.thormakk.keyarmor.di.DefaultDispatcher
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.PasswordHistoryItem
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.VaultMetadata
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
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_DECRYPT = "VaultRepository"

@Singleton
class VaultRepository @Inject constructor(
    private val dao:              VaultEntryDao,
    private val historyDao:       PasswordHistoryDao,
    private val crypto:           CryptoManager,
    private val session:          SessionManager,
    private val panicPinPrefs:    PanicPinPrefs,
    private val travelModePrefs:  TravelModePrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    private val twoFactorRepository:   TwoFactorRepository,
    private val passkeyRepository:     PasskeyRepository,
    private val bankAccountRepository: BankAccountRepository,
    private val cryptoWalletRepository: CryptoWalletRepository,
    private val attachmentRepository:  AttachmentRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    fun observeEntries(): Flow<List<VaultEntry>> =
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

    fun observeTrash(): Flow<List<VaultEntry>> =
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

    suspend fun getEntry(id: String): VaultEntry? {
        val key = session.getSecretKey() ?: return null
        val entry = dao.getById(id)?.let { decryptWith(it, key) } ?: return null
        if (panicPinPrefs.panicModeFlow.value && entry.vaultId !in VaultMetadata.HIDE_VAULT_IDS) return null
        return if (travelModePrefs.travelModeFlow.value && !entry.isTravelSafe) null else entry
    }

    fun observeById(id: String): Flow<VaultEntry?> =
        combine(panicPinPrefs.panicModeFlow, travelModePrefs.travelModeFlow) { p, t -> p to t }
            .flatMapLatest { (isPanic, isTravel) ->
                session.isUnlockedFlow.flatMapLatest { unlocked ->
                    val key = if (unlocked) session.getSecretKey() else null
                    if (key == null) flowOf(null)
                    else dao.observeById(id).map { entity ->
                        val entry = entity?.let { decryptWith(it, key) }
                        when {
                            entry == null -> null
                            isPanic && entry.vaultId !in VaultMetadata.HIDE_VAULT_IDS -> null
                            isTravel && !entry.isTravelSafe -> null
                            else -> entry
                        }
                    }
                }
            }
            .flowOn(defaultDispatcher)

    @Throws(SessionExpiredException::class)
    suspend fun saveEntry(entry: VaultEntry, soloMetadatos: Boolean = false): Boolean {
        val key = session.getSecretKey() ?: throw SessionExpiredException()

        val protegida = isProtected(entry.id)
        if (protegida && !soloMetadatos) return false

        if (entry.password.isNotEmpty()) {
            val existing = dao.getById(entry.id)
            if (existing != null) {
                val oldPassword = runCatching {
                    if (existing.password.isNotEmpty()) crypto.decrypt(existing.password, key) else ""
                }.getOrDefault("")
                if (oldPassword.isNotEmpty() && oldPassword != entry.password) {
                    val hash = crypto.historyHash(oldPassword, key)
                    val encryptedOld = runCatching { crypto.encrypt(oldPassword, key) }.getOrDefault("")
                    historyDao.insert(
                        PasswordHistoryEntity(
                            entryId           = entry.id,
                            passwordHash      = hash,
                            encryptedPassword = encryptedOld,
                            savedAt           = System.currentTimeMillis()
                        )
                    )
                    historyDao.trimHistory(entry.id, keepCount = 20)
                }
            }
        }
        dao.upsert(encrypt(if (soloMetadatos) entry.copy(isProtected = protegida) else entry, key))
        return true
    }

    @Throws(SessionExpiredException::class)
    suspend fun saveAll(items: List<VaultEntry>): Int {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        val protegidas = protectedAmong(items.map { it.id })
        val aGuardar   = if (protegidas.isEmpty()) items else items.filterNot { it.id in protegidas }
        val entidades = withContext(defaultDispatcher) { aGuardar.map { encrypt(it, key) } }
        dao.upsertAll(entidades)
        return protegidas.size
    }

    suspend fun isProtected(id: String): Boolean = dao.getProtectedIds(listOf(id)).isNotEmpty()

    suspend fun setProtected(id: String, isProtected: Boolean) = dao.setProtected(id, isProtected)

    suspend fun protectedAmong(ids: List<String>): Set<String> =
        ids.chunked(500).flatMap { dao.getProtectedIds(it) }.toSet()

    suspend fun saveAllWithKey(items: List<VaultEntry>, key: SecretKeySpec) {
        val entidades = withContext(defaultDispatcher) { items.map { encrypt(it, key) } }
        dao.upsertAll(entidades)
    }

    suspend fun deleteAllByVault(vaultId: String) = dao.deleteAllByVaultId(vaultId)

    suspend fun getPasswordHashes(entryId: String): List<String> =
        historyDao.getHashesForEntry(entryId)

    suspend fun computeHistoryHash(password: String): String? {
        val key = session.getSecretKey() ?: return null
        return crypto.historyHash(password, key)
    }

    suspend fun getPasswordHistory(entryId: String): List<PasswordHistoryItem> {
        val key = session.getSecretKey() ?: return emptyList()
        return historyDao.getHistoryForEntry(entryId)
            .filter { it.encryptedPassword.isNotEmpty() }
            .mapNotNull { entity ->
                runCatching {
                    PasswordHistoryItem(
                        id       = entity.id,
                        password = crypto.decrypt(entity.encryptedPassword, key),
                        savedAt  = entity.savedAt
                    )
                }.onFailure {
                    Log.e(TAG_DECRYPT, "historial de contraseñas id=${entity.id} ilegible, " +
                        "no aparece en Time Machine", it)
                }.getOrNull()
            }
    }

    suspend fun deletePasswordHistory(entryId: String) =
        historyDao.deleteForEntry(entryId)

    suspend fun setFavorite(id: String, isFavorite: Boolean) = dao.setFavorite(id, isFavorite)
    suspend fun setTravelSafe(id: String, isTravelSafe: Boolean) = dao.setTravelSafe(id, isTravelSafe)
    suspend fun setHidden(id: String, isHidden: Boolean) = dao.setHidden(id, isHidden)
    suspend fun unhideAll() = dao.unhideAll()

    suspend fun countAllByVault(vaultId: String): Int = dao.countAllByVault(vaultId)
    suspend fun countActiveByVault(vaultId: String): Int = dao.countActive(vaultId)

    suspend fun countAllEntries(): Int = dao.countAll()

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
    suspend fun restore(id: String)              = dao.restore(id)

    suspend fun deletePermanently(id: String): Boolean {
        if (isProtected(id)) return false
        dao.deletePermanently(id)
        limpiarResiduosDe(id)
        return true
    }

    suspend fun getExpiredTrashedIds(cutoff: Long): List<String> = dao.getExpiredTrashedIds(cutoff)

    suspend fun deleteAll()                      = dao.deleteAll()
    suspend fun archive(id: String) {
        if (dao.getById(id)?.isHidden == true) return
        dao.archive(id)
    }
    suspend fun archiveBatch(ids: List<String>) {
        val eligible = ids.filterNot { dao.getById(it)?.isHidden == true }
        if (eligible.isNotEmpty()) dao.archiveBatch(eligible)
    }
    suspend fun unarchive(id: String)             = dao.unarchive(id)
    suspend fun archiveAll(vaultId: String)                     = dao.archiveAll(vaultId)

    suspend fun emptyTrash() {
        val vaultId = activeVaultPrefs.activeVaultId
        val trashedIds = dao.getTrashedIds(vaultId)
        val protegidas = protectedAmong(trashedIds)
        dao.emptyTrash(vaultId)
        trashedIds.filterNot { it in protegidas }.forEach { limpiarResiduosDe(it) }
    }

    private suspend fun limpiarResiduosDe(vaultEntryId: String) {
        unlinkFromAllTypes(vaultEntryId)
        attachmentRepository.deleteAllForEntry(vaultEntryId)
        historyDao.deleteForEntry(vaultEntryId)
    }

    private suspend fun unlinkFromAllTypes(vaultEntryId: String) {
        twoFactorRepository.unlinkFromPassword(vaultEntryId)
        passkeyRepository.unlinkFromPassword(vaultEntryId)
        bankAccountRepository.unlinkFromPassword(vaultEntryId)
        cryptoWalletRepository.unlinkFromPassword(vaultEntryId)
    }

    private fun decryptWith(entity: VaultEntryEntity, key: SecretKeySpec): VaultEntry? =
        try {
            val urlList = when {
                entity.urls.isNotEmpty() ->
                    entity.urls.split(",")
                        .filter { it.isNotEmpty() }
                        .mapNotNull { cifrada ->
                            runCatching { crypto.decrypt(cifrada, key) }
                                .onFailure { Log.e(TAG_DECRYPT, "una URL de la entrada " +
                                    "id=${entity.id} es ilegible y se descarta", it) }
                                .getOrNull()
                        }
                        .filter { it.isNotEmpty() }
                entity.url.isNotEmpty()  ->
                    listOf(crypto.decrypt(entity.url, key))
                else -> emptyList()
            }
            VaultEntry(
                id           = entity.id,
                title        = crypto.decrypt(entity.title, key),
                username     = if (entity.username.isNotEmpty()) crypto.decrypt(entity.username, key) else "",
                email        = if (entity.email.isNotEmpty())    crypto.decrypt(entity.email, key) else "",
                password     = if (entity.password.isNotEmpty()) crypto.decrypt(entity.password, key) else "",
                urls         = urlList,
                notes        = if (entity.notes.isNotEmpty())    crypto.decrypt(entity.notes, key) else "",
                category     = EntryCategory.fromString(entity.category),
                isFavorite   = entity.isFavorite,
                isTravelSafe = entity.isTravelSafe,
                isPinned     = entity.isPinned,
                isHidden     = entity.isHidden,
                createdAt    = entity.createdAt,
                updatedAt    = entity.updatedAt,
                isDeleted    = entity.isDeleted,
                deletedAt    = entity.deletedAt,
                archivedAt   = entity.archivedAt,
                tags          = entity.tags.split(",").filter { it.isNotEmpty() },
                expiryDate    = entity.expiryDate,
                vaultId       = entity.vaultId,
                inheritedFrom = entity.inheritedFrom,
                passwordChangedAt = entity.passwordChangedAt,
                fieldLabel        = entity.fieldLabel,
                isProtected       = entity.isProtected,
            )
        } catch (e: Exception) {
            Log.e(TAG_DECRYPT, "decrypt falló para id=${entity.id}", e)
            null
        }

    private fun encrypt(entry: VaultEntry, key: SecretKeySpec): VaultEntryEntity {
        val encryptedUrls = entry.urls
            .filter { it.isNotEmpty() }
            .joinToString(",") { crypto.encrypt(it, key) }
        return VaultEntryEntity(
            id           = entry.id,
            title        = crypto.encrypt(entry.title, key),
            username     = if (entry.username.isNotEmpty()) crypto.encrypt(entry.username, key) else "",
            email        = if (entry.email.isNotEmpty())    crypto.encrypt(entry.email, key) else "",
            password     = if (entry.password.isNotEmpty()) crypto.encrypt(entry.password, key) else "",
            url          = "",
            urls         = encryptedUrls,
            notes        = if (entry.notes.isNotEmpty())    crypto.encrypt(entry.notes, key) else "",
            category     = entry.category.name,
            isFavorite   = entry.isFavorite,
            isTravelSafe = entry.isTravelSafe,
            isPinned     = entry.isPinned,
            isHidden     = entry.isHidden,
            createdAt    = entry.createdAt,
            updatedAt    = System.currentTimeMillis(),
            isDeleted    = entry.isDeleted,
            deletedAt    = entry.deletedAt,
            isArchived   = entry.archivedAt != null,
            archivedAt   = entry.archivedAt,
            tags         = entry.tags.joinToString(","),
            expiryDate   = entry.expiryDate,
            vaultId      = entry.vaultId,
            inheritedFrom     = entry.inheritedFrom,
            passwordChangedAt = entry.passwordChangedAt,
            fieldLabel        = entry.fieldLabel,
            isProtected       = entry.isProtected,
        )
    }

    suspend fun getExpiryTimestamps(): List<Long> =
        dao.getExpiryDates()

    suspend fun getExpiringEntryIds(now: Long, warnMs: Long): List<String> =
        dao.getExpiringIds(now, now + warnMs)

    suspend fun getExpiringEntryPairs(now: Long, warnMs: Long): List<Pair<String, String>> {
        session.getSecretKey() ?: return emptyList()
        return dao.getExpiryEntryInfos()
            .filter { it.expiryDate in now..(now + warnMs) }
            .mapNotNull { info ->
                val entry = getEntry(info.id) ?: return@mapNotNull null
                info.id to entry.title
            }
    }

    suspend fun setPin(id: String, isPinned: Boolean) = dao.setPin(id, isPinned)

    suspend fun countPinned(): Int = dao.countPinned(activeVaultPrefs.activeVaultId)
    fun observeArchived(): Flow<List<VaultEntry>> =
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

    fun observeEntriesForAutofill(): Flow<List<VaultEntry>> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(emptyList())
            else combine(dao.observeActiveAllVaults(), travelModePrefs.travelModeFlow) { list, isTravel ->
                val items = list.mapNotNull { decryptWith(it, key) }.filterNot { it.isHidden }
                if (isTravel) items.filter { it.isTravelSafe } else items
            }
        }
            .flowOn(defaultDispatcher)

    fun observeAllForSearch(): Flow<List<VaultEntry>> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(emptyList())
            else combine(dao.observeAll(), hiddenEntriesPrefs.revealActiveFlow, travelModePrefs.travelModeFlow) { list, reveal, isTravel ->
                val items = list.mapNotNull { decryptWith(it, key) }.filter { it.isHidden == reveal }
                if (isTravel) items.filter { it.isTravelSafe } else items
            }
        }
            .flowOn(defaultDispatcher)

    suspend fun getAllIds(): Set<String> = dao.getAllIds().toSet()

    suspend fun getAllForBackup(): List<VaultEntry> {
        val key = session.getSecretKey() ?: return emptyList()
        val entidades = dao.getAllEntities()
        return withContext(defaultDispatcher) {
            entidades.filter { it.vaultId !in VaultMetadata.COERCION_VAULT_IDS }
                .mapNotNull { entity ->
                    val item = decryptWith(entity, key)
                    if (item == null) {
                        Log.e(TAG_DECRYPT, "getAllForBackup: VaultEntry id=${entity.id} ilegible, " +
                            "queda FUERA del backup")
                    }
                    item
                }
        }
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

    @Throws(SessionExpiredException::class)
    suspend fun copyEntriesToVault(
        ids:          Set<String>,
        targetVaultId: String,
        inheritedFrom: String? = null
    ) {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        ids.forEach { id ->
            val original = dao.getById(id)?.let { decryptWith(it, key) } ?: return@forEach
            val copy = original.copy(
                id            = java.util.UUID.randomUUID().toString(),
                vaultId       = targetVaultId,
                inheritedFrom = inheritedFrom,
                isPinned      = false,
                isProtected   = false,
                createdAt     = System.currentTimeMillis(),
                updatedAt     = System.currentTimeMillis()
            )
            dao.upsert(encrypt(copy, key))
        }
    }

    suspend fun removeInheritedFrom(id: String) = dao.clearInheritedFrom(id)
}
