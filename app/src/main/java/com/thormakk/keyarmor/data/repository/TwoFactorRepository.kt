@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.util.Log
import com.thormakk.keyarmor.data.database.dao.TwoFactorDao
import com.thormakk.keyarmor.data.database.entity.TwoFactorEntity
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.prefs.TravelModePrefs
import com.thormakk.keyarmor.di.DefaultDispatcher
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.RecoveryCode
import com.thormakk.keyarmor.domain.model.TotpAlgorithm
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
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
import org.json.JSONArray
import org.json.JSONObject
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_DECRYPT = "TwoFactorRepository"

@Singleton
class TwoFactorRepository @Inject constructor(
    private val dao:             TwoFactorDao,
    private val crypto:          CryptoManager,
    private val session:         SessionManager,
    private val panicPinPrefs:   PanicPinPrefs,
    private val travelModePrefs: TravelModePrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    fun observeAccounts(): Flow<List<TwoFactorAccount>> =
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

    fun observeTrash(): Flow<List<TwoFactorAccount>> =
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

    suspend fun getAccount(id: String): TwoFactorAccount? {
        val key = session.getSecretKey() ?: return null
        val account = dao.getById(id)?.let { decryptWith(it, key) } ?: return null
        if (panicPinPrefs.panicModeFlow.value && account.vaultId !in VaultMetadata.HIDE_VAULT_IDS) return null
        return if (travelModePrefs.travelModeFlow.value && !account.isTravelSafe) null else account
    }

    fun observeAccountById(id: String): Flow<TwoFactorAccount?> =
        combine(panicPinPrefs.panicModeFlow, travelModePrefs.travelModeFlow) { p, t -> p to t }
            .flatMapLatest { (isPanic, isTravel) ->
                session.isUnlockedFlow.flatMapLatest { unlocked ->
                    val key = if (unlocked) session.getSecretKey() else null
                    if (key == null) flowOf(null)
                    else dao.observeById(id).map { entity ->
                        val account = entity?.let { decryptWith(it, key) }
                        when {
                            account == null -> null
                            isPanic && account.vaultId !in VaultMetadata.HIDE_VAULT_IDS -> null
                            isTravel && !account.isTravelSafe -> null
                            else -> account
                        }
                    }
                }
            }
            .flowOn(defaultDispatcher)

    @Throws(SessionExpiredException::class)
    suspend fun saveAccount(account: TwoFactorAccount, soloMetadatos: Boolean = false): Boolean {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        val protegida = isProtected(account.id)
        if (protegida && !soloMetadatos) return false
        dao.upsert(encrypt(if (soloMetadatos) account.copy(isProtected = protegida) else account, key))
        return true
    }

    @Throws(SessionExpiredException::class)
    suspend fun saveAll(items: List<TwoFactorAccount>): Int {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        val protegidas = protectedAmong(items.map { it.id })
        val aGuardar   = if (protegidas.isEmpty()) items else items.filterNot { it.id in protegidas }
        val entidades = withContext(defaultDispatcher) { aGuardar.map { encrypt(it, key) } }
        dao.upsertAll(entidades)
        return protegidas.size
    }

    fun observeLinkedToPassword(vaultEntryId: String): Flow<TwoFactorAccount?> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(null)
            else dao.observeByLinkedPasswordId(vaultEntryId).map { entity ->
                entity?.let { decryptWith(it, key) }
            }
        }
            .flowOn(defaultDispatcher)

    fun observeLinkedPasswordIds(): Flow<Set<String>> =
        dao.observeLinkedPasswordIds().map { it.toSet() }

    @Throws(SessionExpiredException::class)
    suspend fun linkToPassword(twoFactorId: String, vaultEntryId: String) {
        dao.clearLinkForPasswordId(vaultEntryId)
        val entity = dao.getById(twoFactorId) ?: return
        dao.upsert(entity.copy(linkedPasswordId = vaultEntryId))
    }

    suspend fun unlinkFromPassword(vaultEntryId: String) {
        dao.clearLinkForPasswordId(vaultEntryId)
    }

    suspend fun setFavorite(id: String, isFavorite: Boolean) = dao.setFavorite(id, isFavorite)
    suspend fun setTravelSafe(id: String, isTravelSafe: Boolean) = dao.setTravelSafe(id, isTravelSafe)
    suspend fun setHidden(id: String, isHidden: Boolean) = dao.setHidden(id, isHidden)
    suspend fun unhideAll() = dao.unhideAll()

    suspend fun isProtected(id: String): Boolean = dao.getProtectedIds(listOf(id)).isNotEmpty()
    suspend fun setProtected(id: String, isProtected: Boolean) = dao.setProtected(id, isProtected)
    suspend fun protectedAmong(ids: List<String>): Set<String> =
        ids.chunked(500).flatMap { dao.getProtectedIds(it) }.toSet()

    suspend fun countAllByVault(vaultId: String): Int = dao.countAllByVault(vaultId)
    suspend fun countActive(): Int = dao.countActive(activeVaultPrefs.activeVaultId)

    suspend fun countAll(): Int = dao.countAll()

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
        dao.deletePermanently(id)
        return true
    }
    suspend fun emptyTrash()                  = dao.emptyTrash(activeVaultPrefs.activeVaultId)
    suspend fun getExpiredTrashedIds(cutoff: Long): List<String> = dao.getExpiredTrashedIds(cutoff)

    suspend fun deleteAll()                   = dao.deleteAll()
    suspend fun archiveAll(vaultId: String)                     = dao.archiveAll(vaultId)

    private fun decryptWith(entity: TwoFactorEntity, key: SecretKeySpec): TwoFactorAccount? =
        try {
            val recoveryJson = if (entity.recoveryCodesJson.isNotEmpty())
                crypto.decrypt(entity.recoveryCodesJson, key) else "[]"
            TwoFactorAccount(
                id               = entity.id,
                name             = if (entity.name.isNotEmpty())             crypto.decrypt(entity.name, key) else "",
                issuer           = if (entity.issuer.isNotEmpty())           crypto.decrypt(entity.issuer, key) else "",
                accountLabel     = if (entity.accountLabel.isNotEmpty())     crypto.decrypt(entity.accountLabel, key) else "",
                secret           = if (entity.secret.isNotEmpty())           crypto.decrypt(entity.secret, key) else "",
                notes            = if (entity.notes.isNotEmpty())            crypto.decrypt(entity.notes, key) else "",
                url              = if (entity.url.isNotEmpty())             crypto.decrypt(entity.url, key) else "",
                linkedPasswordId = entity.linkedPasswordId,
                digits           = entity.digits,
                period           = entity.period,
                algorithm        = parseAlgorithm(entity.algorithm),
                recoveryCodes    = parseRecoveryCodes(recoveryJson),
                category         = EntryCategory.fromString(entity.category),
                tags             = entity.tags.split(",").filter { it.isNotEmpty() },
                isFavorite       = entity.isFavorite,
                isTravelSafe     = entity.isTravelSafe,
                isPinned     = entity.isPinned,
                isHidden     = entity.isHidden,
                isProtected = entity.isProtected,
                createdAt        = entity.createdAt,
                updatedAt        = entity.updatedAt,
                isDeleted        = entity.isDeleted,
                deletedAt        = entity.deletedAt,
                archivedAt  = entity.archivedAt,
                vaultId          = entity.vaultId,
                inheritedFrom    = entity.inheritedFrom,
            )
        } catch (e: Exception) {
            Log.e(TAG_DECRYPT, "decrypt falló para id=${entity.id}", e)
            null
        }

    private fun encrypt(account: TwoFactorAccount, key: SecretKeySpec): TwoFactorEntity {
        return TwoFactorEntity(
            id                = account.id,
            name              = if (account.name.isNotEmpty())             crypto.encrypt(account.name, key) else "",
            issuer            = if (account.issuer.isNotEmpty())           crypto.encrypt(account.issuer, key) else "",
            accountLabel      = if (account.accountLabel.isNotEmpty())     crypto.encrypt(account.accountLabel, key) else "",
            secret            = if (account.secret.isNotEmpty())           crypto.encrypt(account.secret, key) else "",
            notes             = if (account.notes.isNotEmpty())            crypto.encrypt(account.notes, key) else "",
            url               = if (account.url.isNotEmpty())            crypto.encrypt(account.url, key) else "",
            linkedPasswordId  = account.linkedPasswordId,
            recoveryCodesJson = if (account.recoveryCodes.isNotEmpty())
                crypto.encrypt(serializeRecoveryCodes(account.recoveryCodes), key) else "",
            digits            = account.digits,
            period            = account.period,
            algorithm         = account.algorithm.name,
            category          = account.category.name,
            tags              = account.tags.joinToString(","),
            isFavorite        = account.isFavorite,
            isTravelSafe      = account.isTravelSafe,
            isPinned     = account.isPinned,
            isHidden     = account.isHidden,
            isProtected = account.isProtected,
            createdAt         = account.createdAt,
            updatedAt         = System.currentTimeMillis(),
            isDeleted         = account.isDeleted,
            deletedAt         = account.deletedAt,
            vaultId          = account.vaultId,
            inheritedFrom    = account.inheritedFrom,
        )
    }

    private fun parseAlgorithm(name: String): TotpAlgorithm =
        runCatching { TotpAlgorithm.valueOf(name) }.getOrDefault(TotpAlgorithm.SHA1)

    private fun parseRecoveryCodes(json: String): List<RecoveryCode> = try {
        val arr = JSONArray(json)
        List(arr.length()) { i ->
            val obj = arr.getJSONObject(i)
            RecoveryCode(
                code = obj.optString("code", ""),
                used = obj.optBoolean("used", false)
            )
        }
    } catch (e: Exception) { emptyList() }

    private fun serializeRecoveryCodes(codes: List<RecoveryCode>): String {
        val arr = JSONArray()
        codes.forEach { c ->
            arr.put(JSONObject().apply {
                put("code", c.code)
                put("used", c.used)
            })
        }
        return arr.toString()
    }

    suspend fun setPin(id: String, isPinned: Boolean) = dao.setPin(id, isPinned)

    suspend fun countPinned(): Int = dao.countPinned(activeVaultPrefs.activeVaultId)
    fun observeArchived(): Flow<List<TwoFactorAccount>> =
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

    fun observeAllForSearch(): Flow<List<TwoFactorAccount>> =
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

    suspend fun getAllForBackup(): List<TwoFactorAccount> {
        val key = session.getSecretKey() ?: return emptyList()
        val entidades = dao.getAllEntities()
        return withContext(defaultDispatcher) {
            entidades.filter { it.vaultId !in VaultMetadata.COERCION_VAULT_IDS }
                .mapNotNull { entity ->
                    val item = decryptWith(entity, key)
                    if (item == null) {
                        Log.e(TAG_DECRYPT, "getAllForBackup: TwoFactorAccount id=${entity.id} ilegible, " +
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
            val original = getAccount(id) ?: return@forEach
            val copy = original.copy(
                id = java.util.UUID.randomUUID().toString(),
                vaultId       = targetVaultId,
                inheritedFrom = inheritedFrom,
            isPinned      = false,
            createdAt     = System.currentTimeMillis(),
            updatedAt     = System.currentTimeMillis()
            )
            saveAccount(copy)
        }
    }

    suspend fun removeInheritedFrom(id: String) = dao.clearInheritedFrom(id)
}
