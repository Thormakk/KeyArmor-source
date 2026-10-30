@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.content.Context
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.database.dao.VaultMetadataDao
import com.thormakk.keyarmor.data.database.entity.VaultMetadataEntity
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.SessionExpiredException
import com.thormakk.keyarmor.security.SessionManager
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultMetadataRepository @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val dao: VaultMetadataDao,
    private val crypto: CryptoManager,
    private val session: SessionManager,
    private val panicPinPrefs: PanicPinPrefs
) {

    private val context: Context get() = contextoDelIdioma(contextInyectado)
    private fun filterByCoercionSession(
        vaults:      List<VaultMetadata>,
        decoyActive: Boolean,
        hideActive:  Boolean
    ): List<VaultMetadata> = when {
        decoyActive -> vaults.filter { it.id in VaultMetadata.DECOY_VAULT_IDS }
        hideActive  -> vaults.filter { it.id in VaultMetadata.HIDE_VAULT_IDS }
        else        -> vaults.filter { it.id !in VaultMetadata.DECOY_VAULT_IDS && it.id !in VaultMetadata.HIDE_VAULT_IDS }
    }

    private fun encField(value: String, key: SecretKeySpec): String =
        if (value.isEmpty()) "" else crypto.encrypt(value, key)

    private fun decField(value: String, key: SecretKeySpec): String =
        if (value.isEmpty()) "" else runCatching { crypto.decrypt(value, key) }.getOrDefault(value)

    private fun VaultMetadataEntity.toDomain(key: SecretKeySpec) = VaultMetadata(
        id             = id,
        name           = decField(name, key),
        isInherited    = isInherited,
        isPrivate      = isPrivate,
        ownerEmail     = decField(ownerEmail, key),
        ownerSub       = decField(ownerSub, key),
        sortOrder      = sortOrder,
        createdAt      = createdAt
    )

    private fun VaultMetadata.toEntity(key: SecretKeySpec) = VaultMetadataEntity(
        id             = id,
        name           = encField(name, key),
        isInherited    = isInherited,
        isPrivate      = isPrivate,
        ownerEmail     = encField(ownerEmail, key),
        ownerSub       = encField(ownerSub, key),
        sortOrder      = sortOrder,
        createdAt      = createdAt
    )

    fun observeAll(): Flow<List<VaultMetadata>> =
        combine(session.isUnlockedFlow, panicPinPrefs.decoyActiveFlow, panicPinPrefs.panicModeFlow) { unlocked, decoyActive, hideActive -> Triple(unlocked, decoyActive, hideActive) }
            .flatMapLatest { (unlocked, decoyActive, hideActive) ->
                val key = if (unlocked) session.getSecretKey() else null
                if (key == null) flowOf(emptyList())
                else dao.observeAll().map { list ->
                    filterByCoercionSession(list.map { it.toDomain(key) }, decoyActive, hideActive)
                }
            }

    suspend fun getAll(): List<VaultMetadata> {
        val key = session.getSecretKey() ?: return emptyList()
        val all = dao.getAll().map { it.toDomain(key) }
        return filterByCoercionSession(all, panicPinPrefs.decoyActiveFlow.value, panicPinPrefs.panicModeFlow.value)
    }

    suspend fun saveWithKey(vault: VaultMetadata, key: SecretKeySpec) {
        dao.upsert(vault.toEntity(key))
    }

    suspend fun getAllIdsRaw(): List<String> = dao.getAll().map { it.id }

    suspend fun getById(id: String): VaultMetadata? {
        val key = session.getSecretKey() ?: return null
        return dao.getById(id)?.toDomain(key)
    }

    @Throws(SessionExpiredException::class)
    suspend fun save(vault: VaultMetadata) {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        dao.upsert(vault.toEntity(key))
    }

    suspend fun delete(id: String) =
        dao.deleteById(id)

    @Throws(SessionExpiredException::class)
    suspend fun ensurePrimaryVaultExists() {
        if (dao.getById(VaultMetadata.PRIMARY_VAULT_ID) == null) {
            val key = session.getSecretKey() ?: throw SessionExpiredException()
            dao.upsert(
                VaultMetadataEntity(
                    id          = VaultMetadata.PRIMARY_VAULT_ID,
                    name        = encField(context.getString(R.string.vault_name_primary), key),
                    isInherited = false,
                    isPrivate   = false,
                    sortOrder   = 0,
                    createdAt   = System.currentTimeMillis()
                )
            )
        }
    }

    @Throws(SessionExpiredException::class)
    suspend fun ensurePrivateVaultExists() {
        if (dao.getById(VaultMetadata.PRIVATE_VAULT_ID) == null) {
            val key = session.getSecretKey() ?: throw SessionExpiredException()
            dao.upsert(
                VaultMetadataEntity(
                    id          = VaultMetadata.PRIVATE_VAULT_ID,
                    name        = encField(context.getString(R.string.vault_name_private), key),
                    isInherited = false,
                    isPrivate   = true,
                    sortOrder   = VaultMetadata.PRIVATE_SORT_ORDER,
                    createdAt   = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun reasignarEntradasHuerfanas(): Int {
        val destino = VaultMetadata.PRIMARY_VAULT_ID
        return (
            dao.reasignarHuerfanas0(destino) +
            dao.reasignarHuerfanas1(destino) +
            dao.reasignarHuerfanas2(destino) +
            dao.reasignarHuerfanas3(destino) +
            dao.reasignarHuerfanas4(destino) +
            dao.reasignarHuerfanas5(destino) +
            dao.reasignarHuerfanas6(destino) +
            dao.reasignarHuerfanas7(destino) +
            dao.reasignarHuerfanas8(destino) +
            dao.reasignarHuerfanas9(destino)
        )
    }

    suspend fun healSystemVaultFlags() {
        val all = getAll()
        all.find { it.id == VaultMetadata.PRIVATE_VAULT_ID && !it.isPrivate }
            ?.let { save(it.copy(isPrivate = true)) }

        all.filter { it.isPrivate && it.id != VaultMetadata.PRIVATE_VAULT_ID }
            .forEach { save(it.copy(isPrivate = false)) }
    }

    suspend fun stripLegacyPanelPrefix() {
        val legacyPrefix = "Panel de "
        getAll()
            .filter { vault ->
                !vault.isInherited && !vault.isPrivate &&
                vault.id != VaultMetadata.PRIMARY_VAULT_ID &&
                vault.name.startsWith(legacyPrefix)
            }
            .forEach { vault ->
                save(vault.copy(name = vault.name.removePrefix(legacyPrefix)))
            }
    }

}
