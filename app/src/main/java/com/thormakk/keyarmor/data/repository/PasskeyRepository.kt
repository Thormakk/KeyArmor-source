@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.util.Log
import android.util.Base64
import com.thormakk.keyarmor.data.database.dao.PasskeyDao
import com.thormakk.keyarmor.data.database.entity.PasskeyEntity
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.TravelModePrefs
import com.thormakk.keyarmor.di.DefaultDispatcher
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.model.PasskeyCredential
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
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_DECRYPT = "PasskeyRepository"

@Singleton
class PasskeyRepository @Inject constructor(
    private val dao:              PasskeyDao,
    private val crypto:           CryptoManager,
    private val session:          SessionManager,
    private val activeVaultPrefs: ActiveVaultPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    private val travelModePrefs:  TravelModePrefs,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    fun observePasskeys(): Flow<List<PasskeyCredential>> =
        combine(session.isUnlockedFlow, activeVaultPrefs.activeVaultIdFlow, travelModePrefs.travelModeFlow) { unlocked, vaultId, isTravel -> Triple(unlocked, vaultId, isTravel) }
            .flatMapLatest { (unlocked, vaultId, isTravel) ->
                val key = if (unlocked) session.getSecretKey() else null
                if (key == null) flowOf(emptyList())
                else dao.observeActive(vaultId).map { list ->
                    val items = list.mapNotNull { decrypt(it, key) }.filterNot { it.isHidden }
                    if (isTravel) items.filter { it.isTravelSafe } else items
                }
            }
            .flowOn(defaultDispatcher)

    fun observeArchived(): Flow<List<PasskeyCredential>> =
        combine(session.isUnlockedFlow, activeVaultPrefs.activeVaultIdFlow, travelModePrefs.travelModeFlow) { unlocked, vaultId, isTravel -> Triple(unlocked, vaultId, isTravel) }
            .flatMapLatest { (unlocked, vaultId, isTravel) ->
                val key = if (unlocked) session.getSecretKey() else null
                if (key == null) flowOf(emptyList())
                else dao.observeArchived(vaultId).map { list ->
                    val items = list.mapNotNull { decrypt(it, key) }.filterNot { it.isHidden }
                    if (isTravel) items.filter { it.isTravelSafe } else items
                }
            }
            .flowOn(defaultDispatcher)

    suspend fun archive(credentialId: String) {
        if (dao.getById(credentialId)?.isHidden == true) return
        dao.archive(credentialId)
    }
    suspend fun unarchive(credentialId: String) = dao.unarchive(credentialId)

    fun observeTrash(): Flow<List<PasskeyCredential>> =
        combine(session.isUnlockedFlow, activeVaultPrefs.activeVaultIdFlow, travelModePrefs.travelModeFlow) { unlocked, vaultId, isTravel -> Triple(unlocked, vaultId, isTravel) }
            .flatMapLatest { (unlocked, vaultId, isTravel) ->
                val key = if (unlocked) session.getSecretKey() else null
                if (key == null) flowOf(emptyList())
                else dao.observeDeleted(vaultId).map { list ->
                    val items = list.mapNotNull { decrypt(it, key) }.filterNot { it.isHidden }
                    if (isTravel) items.filter { it.isTravelSafe } else items
                }
            }
            .flowOn(defaultDispatcher)

    fun observeAllForSearch(): Flow<List<PasskeyCredential>> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(emptyList())
            else combine(dao.observeAll(), hiddenEntriesPrefs.revealActiveFlow, travelModePrefs.travelModeFlow) { list, reveal, isTravel ->
                val items = list.mapNotNull { decrypt(it, key) }.filter { it.isHidden == reveal }
                if (isTravel) items.filter { it.isTravelSafe } else items
            }
        }
            .flowOn(defaultDispatcher)

    @Throws(SessionExpiredException::class)
    suspend fun linkToPassword(credentialId: String, vaultEntryId: String) {
        dao.clearLinkForPasswordId(vaultEntryId)
        val entity = dao.getById(credentialId) ?: return
        dao.upsert(entity.copy(linkedPasswordId = vaultEntryId))
    }

    suspend fun unlinkFromPassword(vaultEntryId: String) {
        dao.clearLinkForPasswordId(vaultEntryId)
    }

    fun observeLinkedToPassword(vaultEntryId: String): Flow<PasskeyCredential?> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(null)
            else dao.observeByLinkedPasswordId(vaultEntryId).map { entity ->
                entity?.let { decrypt(it, key) }
            }
        }
            .flowOn(defaultDispatcher)

    fun observeLinkedPasswordIds(): Flow<Set<String>> =
        dao.observeLinkedPasswordIds().map { it.toSet() }

    suspend fun getPasskey(credentialId: String): PasskeyCredential? {
        val key = session.getSecretKey() ?: return null
        val passkey = dao.getById(credentialId)?.let { decrypt(it, key) } ?: return null
        return if (travelModePrefs.travelModeFlow.value && !passkey.isTravelSafe) null else passkey
    }

    fun observeById(credentialId: String): Flow<PasskeyCredential?> =
        combine(session.isUnlockedFlow, travelModePrefs.travelModeFlow) { unlocked, isTravel -> Pair(unlocked, isTravel) }
            .flatMapLatest { (unlocked, isTravel) ->
                val key = if (unlocked) session.getSecretKey() else null
                if (key == null) flowOf(null)
                else dao.observeById(credentialId).map { entity ->
                    entity?.let { decrypt(it, key) }?.let { pk -> if (isTravel && !pk.isTravelSafe) null else pk }
                }
            }
            .flowOn(defaultDispatcher)

    suspend fun getPasskeysForRp(rpId: String): List<PasskeyCredential> {
        val key = session.getSecretKey() ?: return emptyList()
        val items = dao.getByRpId(rpId).mapNotNull { decrypt(it, key) }.filterNot { it.isHidden }
        return if (travelModePrefs.travelModeFlow.value) items.filter { it.isTravelSafe } else items
    }

    suspend fun getPasskeyEntityListForRp(rpId: String): List<PasskeyEntity> {
        val isTravel = travelModePrefs.travelModeFlow.value
        return dao.getByRpId(rpId).filter {
            !it.isDeleted && !it.isHidden && (!isTravel || it.isTravelSafe)
        }
    }

    @Throws(SessionExpiredException::class)
    suspend fun savePasskey(passkey: PasskeyCredential): Boolean {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        if (isProtected(passkey.credentialId)) return false
        dao.upsert(encrypt(passkey, key))
        return true
    }

    @Throws(SessionExpiredException::class)
    suspend fun saveAll(items: List<PasskeyCredential>): Int {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        val protegidas = protectedAmong(items.map { it.credentialId })
        val aGuardar   = if (protegidas.isEmpty()) items else items.filterNot { it.credentialId in protegidas }
        val entidades = withContext(defaultDispatcher) { aGuardar.map { encrypt(it, key) } }
        dao.upsertAll(entidades)
        return protegidas.size
    }

    @Throws(SessionExpiredException::class)
    suspend fun saveNewPasskey(
        credentialId:          String,
        rpId:                  String,
        rpName:                String,
        userId:                String,
        userName:              String,
        userDisplayName:       String,
        privateKeyPkcs8:       ByteArray,
        publicKeyCoseBytes:    ByteArray
    ) {
        val key = session.getSecretKey() ?: throw SessionExpiredException()

        val privateKeyB64  = Base64.encodeToString(privateKeyPkcs8, Base64.NO_WRAP)
        val encryptedPriv  = crypto.encrypt(privateKeyB64, key)

        val publicKeyCoseB64 = Base64.encodeToString(publicKeyCoseBytes, Base64.NO_WRAP)

        dao.upsert(
            PasskeyEntity(
                credentialId      = credentialId,
                rpId              = rpId,
                rpName            = if (rpName.isNotEmpty()) crypto.encrypt(rpName, key) else "",
                userId            = userId,
                userName          = if (userName.isNotEmpty()) crypto.encrypt(userName, key) else "",
                userDisplayName   = if (userDisplayName.isNotEmpty()) crypto.encrypt(userDisplayName, key) else "",
                privateKeyEncrypted = encryptedPriv,
                publicKeyCose     = publicKeyCoseB64,
                signCount         = 0L,
                createdAt         = System.currentTimeMillis()
            )
        )
    }

    @Throws(SessionExpiredException::class)
    suspend fun saveMetadata(passkey: PasskeyCredential, soloMetadatos: Boolean = false): Boolean {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        if (!soloMetadatos && isProtected(passkey.credentialId)) return false
        dao.updateMetadata(
            credentialId       = passkey.credentialId,
            notes              = if (passkey.notes.isNotEmpty()) crypto.encrypt(passkey.notes, key) else "",
            urls               = passkey.urls,
            linkedPasswordId   = passkey.linkedPasswordId,
            category           = passkey.category,
            tags               = passkey.tags,
            customAccountLabel = if (passkey.customAccountLabel.isNotEmpty())
                                     crypto.encrypt(passkey.customAccountLabel, key)
                                 else ""
        )
        return true
    }

    suspend fun saveTravelSafeOnly(credentialId: String, value: Boolean) =
        dao.updateTravelSafe(credentialId, value)

    suspend fun saveHiddenOnly(credentialId: String, value: Boolean) =
        dao.updateHidden(credentialId, value)

    suspend fun togglePin(credentialId: String, value: Boolean) =
        dao.updatePinned(credentialId, value)

    suspend fun incrementSignCount(credentialId: String, newSignCount: Long) =
        dao.incrementSignCount(credentialId, newSignCount)

    suspend fun getPrivateKeyBytes(credentialId: String): ByteArray? {
        val key    = session.getSecretKey() ?: return null
        val entity = dao.getById(credentialId) ?: return null
        return try {
            val b64 = crypto.decrypt(entity.privateKeyEncrypted, key)
            Base64.decode(b64, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG_DECRYPT, "getPrivateKeyBytes: passkey id=$credentialId ilegible", e)
            null
        }
    }

    suspend fun delete(credentialId: String): Boolean {
        if (isProtected(credentialId)) return false
        if (dao.getById(credentialId)?.isHidden == true) deletePermanently(credentialId)
        else dao.softDelete(credentialId)
        return true
    }
    suspend fun restore(credentialId: String)       = dao.restore(credentialId)
    suspend fun unhideAll() = dao.unhideAll()

    suspend fun deletePermanently(credentialId: String): Boolean {
        if (isProtected(credentialId)) return false
        dao.deletePermanently(credentialId)
        return true
    }

    suspend fun isProtected(credentialId: String): Boolean = dao.getProtectedIds(listOf(credentialId)).isNotEmpty()
    suspend fun setProtected(credentialId: String, isProtected: Boolean) = dao.setProtected(credentialId, isProtected)
    suspend fun protectedAmong(ids: List<String>): Set<String> =
        ids.chunked(500).flatMap { dao.getProtectedIds(it) }.toSet()
    suspend fun emptyTrash()                        = dao.emptyTrash(activeVaultPrefs.activeVaultId)
    suspend fun getExpiredTrashedIds(cutoff: Long): List<String> = dao.getExpiredTrashedIds(cutoff)

    suspend fun deleteAll()                         = dao.deleteAll()
    suspend fun deleteAllExceptHidden()             = dao.deleteAllExceptHidden()

    suspend fun deleteAllExceptHiddenInVault(vaultId: String) = dao.deleteAllExceptHiddenInVault(vaultId)
    suspend fun countActive(): Int                  = dao.countActive(activeVaultPrefs.activeVaultId)
    suspend fun countPinned(): Int                  = dao.countPinned(activeVaultPrefs.activeVaultId)
    suspend fun countAllByVault(vaultId: String): Int  = dao.countAllByVault(vaultId)

    suspend fun getAllForBackup(): List<PasskeyCredential> {
        val key = session.getSecretKey() ?: return emptyList()
        val entidades = dao.getAllEntities()
        return withContext(defaultDispatcher) {
            entidades.filter { it.vaultId !in VaultMetadata.COERCION_VAULT_IDS }
                .mapNotNull { entity ->
                    val item = decrypt(entity, key)
                    if (item == null) {
                        Log.e(TAG_DECRYPT, "getAllForBackup: PasskeyCredential id=${entity.credentialId} ilegible, " +
                            "queda FUERA del backup")
                    }
                    item
                }
        }
    }

    suspend fun getAllEntities(): List<PasskeyEntity>            = dao.getAllEntities()
    suspend fun upsertAll(entities: List<PasskeyEntity>)        = dao.upsertAll(entities)

    private fun decrypt(
        entity: PasskeyEntity,
        key: javax.crypto.spec.SecretKeySpec
    ): PasskeyCredential? = try {
        val privateKeyB64 = crypto.decrypt(entity.privateKeyEncrypted, key)
        PasskeyCredential(
            credentialId       = entity.credentialId,
            rpId               = entity.rpId,
            rpName             = if (entity.rpName.isNotEmpty()) crypto.decrypt(entity.rpName, key) else "",
            userId             = entity.userId,
            userName           = if (entity.userName.isNotEmpty()) crypto.decrypt(entity.userName, key) else "",
            userDisplayName    = if (entity.userDisplayName.isNotEmpty()) crypto.decrypt(entity.userDisplayName, key) else "",
            privateKeyPkcs8    = Base64.decode(privateKeyB64, Base64.NO_WRAP),
            publicKeyCose      = Base64.decode(entity.publicKeyCose, Base64.NO_WRAP),
            signCount          = entity.signCount,
            createdAt          = entity.createdAt,
            lastUsedAt         = entity.lastUsedAt,
            notes              = if (entity.notes.isNotEmpty()) crypto.decrypt(entity.notes, key) else "",
            urls               = entity.urls,
            linkedPasswordId   = entity.linkedPasswordId,
            category           = entity.category,
            tags               = entity.tags,
            customAccountLabel = if (entity.customAccountLabel.isNotEmpty())
                                     crypto.decrypt(entity.customAccountLabel, key)
                                 else "",
            isTravelSafe       = entity.isTravelSafe,
            isPinned           = entity.isPinned,
            isHidden           = entity.isHidden,
            isProtected        = entity.isProtected,
            isArchived         = entity.isArchived,
            archivedAt         = entity.archivedAt,
            isDeleted          = entity.isDeleted,
            deletedAt          = entity.deletedAt,
            vaultId            = entity.vaultId,
            inheritedFrom      = entity.inheritedFrom,
        )
    } catch (e: Exception) {
        Log.e(TAG_DECRYPT, "decrypt falló para passkey id=${entity.credentialId}", e)
        null
    }

    private fun encrypt(passkey: PasskeyCredential, key: javax.crypto.spec.SecretKeySpec): PasskeyEntity {
        val privateKeyB64    = Base64.encodeToString(passkey.privateKeyPkcs8, Base64.NO_WRAP)
        val publicKeyCoseB64 = Base64.encodeToString(passkey.publicKeyCose, Base64.NO_WRAP)
        return PasskeyEntity(
            credentialId        = passkey.credentialId,
            rpId                = passkey.rpId,
            rpName              = if (passkey.rpName.isNotEmpty()) crypto.encrypt(passkey.rpName, key) else "",
            userId              = passkey.userId,
            userName            = if (passkey.userName.isNotEmpty()) crypto.encrypt(passkey.userName, key) else "",
            userDisplayName     = if (passkey.userDisplayName.isNotEmpty()) crypto.encrypt(passkey.userDisplayName, key) else "",
            privateKeyEncrypted = crypto.encrypt(privateKeyB64, key),
            publicKeyCose       = publicKeyCoseB64,
            signCount           = passkey.signCount,
            createdAt           = passkey.createdAt,
            lastUsedAt          = passkey.lastUsedAt,
            notes               = if (passkey.notes.isNotEmpty()) crypto.encrypt(passkey.notes, key) else "",
            urls                = passkey.urls,
            linkedPasswordId    = passkey.linkedPasswordId,
            category            = passkey.category,
            tags                = passkey.tags,
            customAccountLabel  = if (passkey.customAccountLabel.isNotEmpty())
                                      crypto.encrypt(passkey.customAccountLabel, key)
                                  else "",
            isTravelSafe        = passkey.isTravelSafe,
            isPinned            = passkey.isPinned,
            isHidden            = passkey.isHidden,
            isProtected         = passkey.isProtected,
            isDeleted           = passkey.isDeleted,
            deletedAt           = passkey.deletedAt,
            vaultId             = passkey.vaultId,
            inheritedFrom       = passkey.inheritedFrom,
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
            val original = getPasskey(id) ?: return@forEach
            val copy = original.copy(
                credentialId = java.util.UUID.randomUUID().toString(),
                vaultId       = targetVaultId,
                inheritedFrom = inheritedFrom,
            isPinned      = false,
            createdAt     = System.currentTimeMillis()
            )
            savePasskey(copy)
        }
    }

    suspend fun removeInheritedFrom(id: String) = dao.clearInheritedFrom(id)
}
