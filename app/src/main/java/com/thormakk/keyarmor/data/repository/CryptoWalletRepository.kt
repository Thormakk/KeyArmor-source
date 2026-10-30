@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.util.Log
import com.thormakk.keyarmor.data.database.dao.CryptoWalletDao
import com.thormakk.keyarmor.data.database.entity.CryptoWalletEntity
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.prefs.TravelModePrefs
import com.thormakk.keyarmor.di.DefaultDispatcher
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.EntryCategory
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

private const val TAG_DECRYPT = "CryptoWalletRepository"

@Singleton
class CryptoWalletRepository @Inject constructor(
    private val dao:             CryptoWalletDao,
    private val crypto:          CryptoManager,
    private val session:         SessionManager,
    private val panicPinPrefs:   PanicPinPrefs,
    private val travelModePrefs: TravelModePrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    private val attachmentRepository: AttachmentRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    fun observeWallets(): Flow<List<CryptoWallet>> =
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

    fun observeTrash(): Flow<List<CryptoWallet>> =
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

    fun observeWallet(id: String): Flow<CryptoWallet?> =
        combine(panicPinPrefs.panicModeFlow, travelModePrefs.travelModeFlow, activeVaultPrefs.activeVaultIdFlow) { p, t, v -> Triple(p, t, v) }
            .flatMapLatest { (isPanic, isTravel, _) ->
                session.isUnlockedFlow.flatMapLatest { unlocked ->
                    val key = if (unlocked) session.getSecretKey() else null
                    if (key == null) flowOf(null)
                    else dao.observeById(id).map { entity ->
                        entity?.let { decryptWith(it, key) }?.let { wallet ->
                            when {
                                isPanic && wallet.vaultId !in VaultMetadata.HIDE_VAULT_IDS -> null
                                isTravel && !wallet.isTravelSafe -> null
                                else -> wallet
                            }
                        }
                    }
                }
            }
            .flowOn(defaultDispatcher)

    suspend fun getWallet(id: String): CryptoWallet? {
        val key = session.getSecretKey() ?: return null
        val wallet = dao.getById(id)?.let { decryptWith(it, key) } ?: return null
        if (panicPinPrefs.panicModeFlow.value && wallet.vaultId !in VaultMetadata.HIDE_VAULT_IDS) return null
        return if (travelModePrefs.travelModeFlow.value && !wallet.isTravelSafe) null else wallet
    }

    @Throws(SessionExpiredException::class)
    suspend fun saveWallet(wallet: CryptoWallet, soloMetadatos: Boolean = false): Boolean {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        val protegida = isProtected(wallet.id)
        if (protegida && !soloMetadatos) return false
        dao.upsert(encrypt(if (soloMetadatos) wallet.copy(isProtected = protegida) else wallet, key))
        return true
    }

    @Throws(SessionExpiredException::class)
    suspend fun saveAll(items: List<CryptoWallet>): Int {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        val protegidas = protectedAmong(items.map { it.id })
        val aGuardar   = if (protegidas.isEmpty()) items else items.filterNot { it.id in protegidas }
        val entidades = withContext(defaultDispatcher) { aGuardar.map { encrypt(it, key) } }
        dao.upsertAll(entidades)
        return protegidas.size
    }

    suspend fun setFavorite(id: String, isFavorite: Boolean) = dao.setFavorite(id, isFavorite)
    suspend fun setTravelSafe(id: String, isTravelSafe: Boolean) = dao.setTravelSafe(id, isTravelSafe)
    suspend fun setHidden(id: String, isHidden: Boolean) = dao.setHidden(id, isHidden)
    suspend fun unhideAll() = dao.unhideAll()

    suspend fun isProtected(id: String): Boolean = dao.getProtectedIds(listOf(id)).isNotEmpty()
    suspend fun setProtected(id: String, isProtected: Boolean) = dao.setProtected(id, isProtected)
    suspend fun protectedAmong(ids: List<String>): Set<String> =
        ids.chunked(500).flatMap { dao.getProtectedIds(it) }.toSet()

    suspend fun unlinkFromPassword(vaultEntryId: String) = dao.clearLinkForPasswordId(vaultEntryId)

    suspend fun countAllByVault(vaultId: String): Int = dao.countAllByVault(vaultId)

    suspend fun countAllWallets(): Int = dao.countAll()

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
    suspend fun restore(id: String)           = dao.restore(id)
    suspend fun deletePermanently(id: String): Boolean {
        if (isProtected(id)) return false
        borrarSinChequear(id)
        return true
    }

    private suspend fun borrarSinChequear(id: String) {
        dao.deletePermanently(id)
        attachmentRepository.deleteAllForEntry(id)
    }

    suspend fun emptyTrash() {
        val vaultId = activeVaultPrefs.activeVaultId
        val trashedIds = dao.getTrashedIds(vaultId)
        dao.emptyTrash(vaultId)
        trashedIds.forEach { attachmentRepository.deleteAllForEntry(it) }
    }
    suspend fun getExpiredTrashedIds(cutoff: Long): List<String> = dao.getExpiredTrashedIds(cutoff)

    suspend fun deleteAll()                   = dao.deleteAll()
    suspend fun archiveAll(vaultId: String)                     = dao.archiveAll(vaultId)

    private fun decryptWith(entity: CryptoWalletEntity, key: SecretKeySpec): CryptoWallet? =
        try {
            fun dec(v: String) = if (v.isNotEmpty()) crypto.decrypt(v, key) else ""
            CryptoWallet(
                id           = entity.id,
                address      = dec(entity.address),
                seedPhrase   = dec(entity.seedPhrase),
                privateKey   = dec(entity.privateKey),
                notes             = dec(entity.notes),
                manualBalance     = entity.manualBalance,
                exchangeUrl       = entity.exchangeUrl,
                linkedPasswordId  = entity.linkedPasswordId,
                network      = entity.network,
                asset        = entity.asset,
                category     = EntryCategory.entries.firstOrNull { it.name == entity.category }
                                   ?: EntryCategory.SIN_CATEGORIZAR,
                tags         = entity.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                isFavorite   = entity.isFavorite,
                isTravelSafe = entity.isTravelSafe,
                isPinned     = entity.isPinned,
                isHidden     = entity.isHidden,
                isProtected = entity.isProtected,
                createdAt    = entity.createdAt,
                updatedAt    = entity.updatedAt,
                isDeleted    = entity.isDeleted,
                deletedAt    = entity.deletedAt,
                archivedAt  = entity.archivedAt,
                vaultId          = entity.vaultId,
                inheritedFrom    = entity.inheritedFrom,
            )
        } catch (e: Exception) {
            Log.e(TAG_DECRYPT, "decrypt falló para id=${entity.id}", e)
            null
        }

    private fun encrypt(wallet: CryptoWallet, key: SecretKeySpec): CryptoWalletEntity {
        fun enc(v: String) = if (v.isNotEmpty()) crypto.encrypt(v, key) else ""
        return CryptoWalletEntity(
            id           = wallet.id,
            address      = enc(wallet.address),
            seedPhrase   = enc(wallet.seedPhrase),
            privateKey   = enc(wallet.privateKey),
            notes             = enc(wallet.notes),
            manualBalance     = wallet.manualBalance,
            exchangeUrl       = wallet.exchangeUrl,
            linkedPasswordId  = wallet.linkedPasswordId,
            network      = wallet.network,
            asset        = wallet.asset,
            category     = wallet.category.name,
            tags         = wallet.tags.joinToString(","),
            isFavorite   = wallet.isFavorite,
            isTravelSafe = wallet.isTravelSafe,
            isPinned     = wallet.isPinned,
            isHidden     = wallet.isHidden,
            isProtected = wallet.isProtected,
            createdAt    = wallet.createdAt,
            updatedAt    = System.currentTimeMillis(),
            isDeleted    = wallet.isDeleted,
            deletedAt    = wallet.deletedAt,
            vaultId          = wallet.vaultId,
            inheritedFrom    = wallet.inheritedFrom,
        )
    }

    suspend fun setPin(id: String, isPinned: Boolean) = dao.setPin(id, isPinned)

    suspend fun countPinned(): Int = dao.countPinned(activeVaultPrefs.activeVaultId)
    fun observeArchived(): Flow<List<CryptoWallet>> =
        combine(panicPinPrefs.panicModeFlow, travelModePrefs.travelModeFlow, activeVaultPrefs.activeVaultIdFlow) { p, t, v -> Triple(p, t, v) }
            .flatMapLatest { (isPanic, isTravel, vaultId) ->
                if (isPanic && vaultId !in VaultMetadata.HIDE_VAULT_IDS) flowOf(emptyList())
                else session.isUnlockedFlow.flatMapLatest { unlocked ->
                    val key = if (unlocked) session.getSecretKey() else null
                    if (key == null) flowOf(emptyList())
                    else dao.observeArchived(vaultId).map { list ->
                        val items = list.mapNotNull { decryptWith(it, key)}.filterNot { it.isHidden }
                        if (isTravel) items.filter { it.isTravelSafe} else items
                   }
               }
           }
            .flowOn(defaultDispatcher)

    fun observeAllForSearch(): Flow<List<CryptoWallet>> =
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

    suspend fun getAllForBackup(): List<CryptoWallet> {
        val key = session.getSecretKey() ?: return emptyList()
        val entidades = dao.getAllEntities()
        return withContext(defaultDispatcher) {
            entidades.filter { it.vaultId !in VaultMetadata.COERCION_VAULT_IDS }
                .mapNotNull { entity ->
                    val item = decryptWith(entity, key)
                    if (item == null) {
                        Log.e(TAG_DECRYPT, "getAllForBackup: CryptoWallet id=${entity.id} ilegible, " +
                            "queda FUERA del backup")
                    }
                    item
                }
        }
    }


    suspend fun archive(id: String) {
        if (dao.getById(id)?.isHidden == true) return
        dao.archive(id)
    }
    suspend fun archiveBatch(ids: List<String>) {
        val eligible = ids.filterNot { dao.getById(it)?.isHidden == true }
        if (eligible.isNotEmpty()) dao.archiveBatch(eligible)
    }
    suspend fun unarchive(id: String)  = dao.unarchive(id)

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
            val original = getWallet(id) ?: return@forEach
            val copy = original.copy(
                id = java.util.UUID.randomUUID().toString(),
                vaultId       = targetVaultId,
                inheritedFrom = inheritedFrom,
            isPinned      = false,
            createdAt     = System.currentTimeMillis(),
            updatedAt     = System.currentTimeMillis()
            )
            saveWallet(copy)
        }
    }

    suspend fun removeInheritedFrom(id: String) = dao.clearInheritedFrom(id)
}
