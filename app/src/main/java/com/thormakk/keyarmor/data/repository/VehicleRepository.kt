@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.content.Context
import android.util.Log
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.database.dao.VehicleDao
import com.thormakk.keyarmor.data.database.entity.VehicleEntity
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.prefs.TravelModePrefs
import com.thormakk.keyarmor.di.DefaultDispatcher
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Vehicle
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

private const val TAG_DECRYPT = "VehicleRepository"

@Singleton
class VehicleRepository @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val dao:             VehicleDao,
    private val crypto:          CryptoManager,
    private val session:         SessionManager,
    private val panicPinPrefs:   PanicPinPrefs,
    private val travelModePrefs: TravelModePrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    private val attachmentRepository: AttachmentRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    fun observeVehicles(): Flow<List<Vehicle>> =
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

    fun observeTrash(): Flow<List<Vehicle>> =
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

    fun observeArchived(): Flow<List<Vehicle>> =
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

    fun observeAllForSearch(): Flow<List<Vehicle>> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(emptyList())
            else combine(dao.observeAll(), hiddenEntriesPrefs.revealActiveFlow, travelModePrefs.travelModeFlow) { list, reveal, isTravel ->
                val items = list.mapNotNull { decryptWith(it, key) }.filter { it.isHidden == reveal }
                if (isTravel) items.filter { it.isTravelSafe } else items
            }
        }
            .flowOn(defaultDispatcher)


    suspend fun getVehicle(id: String): Vehicle? {
        val key = session.getSecretKey() ?: return null
        val v = dao.getById(id)?.let { decryptWith(it, key) } ?: return null
        if (panicPinPrefs.panicModeFlow.value && v.vaultId !in VaultMetadata.HIDE_VAULT_IDS) return null
        return if (travelModePrefs.travelModeFlow.value && !v.isTravelSafe) null else v
    }

    fun observeVehicleById(id: String): Flow<Vehicle?> =
        combine(panicPinPrefs.panicModeFlow, travelModePrefs.travelModeFlow) { p, t -> p to t }
            .flatMapLatest { (isPanic, isTravel) ->
                session.isUnlockedFlow.flatMapLatest { unlocked ->
                    val key = if (unlocked) session.getSecretKey() else null
                    if (key == null) flowOf(null)
                    else dao.observeById(id).map { entity ->
                        val v = entity?.let { decryptWith(it, key) }
                        when {
                            v == null -> null
                            isPanic && v.vaultId !in VaultMetadata.HIDE_VAULT_IDS -> null
                            isTravel && !v.isTravelSafe -> null
                            else -> v
                        }
                    }
                }
            }
            .flowOn(defaultDispatcher)

    @Throws(SessionExpiredException::class)
    suspend fun saveVehicle(vehicle: Vehicle, soloMetadatos: Boolean = false): Boolean {
        val key = session.getSecretKey() ?: throw SessionExpiredException()
        val protegida = isProtected(vehicle.id)
        if (protegida && !soloMetadatos) return false
        dao.upsert(encrypt(if (soloMetadatos) vehicle.copy(isProtected = protegida) else vehicle, key))
        return true
    }

    @Throws(SessionExpiredException::class)
    suspend fun saveAll(items: List<Vehicle>): Int {
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

    suspend fun countAllByVault(vaultId: String): Int = dao.countAllByVault(vaultId)
    suspend fun countAllVehicles(): Int = dao.countAll()
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
                val v = getVehicle(id) ?: return@mapNotNull null
                val label = listOf(v.make, v.model).filter { it.isNotEmpty() }.joinToString(" ")
                    .ifEmpty { context.getString(R.string.vehicle_fallback_title) }
                id to label
            }
    }
    suspend fun archiveAll(vaultId: String)                   = dao.archiveAll(vaultId)
    suspend fun getAllIds(): Set<String>        = dao.getAllIds().toSet()
    suspend fun getAllForBackup(): List<Vehicle> {
        val key = session.getSecretKey() ?: return emptyList()
        val entidades = dao.getAllEntities()
        return withContext(defaultDispatcher) {
            entidades.filter { it.vaultId !in VaultMetadata.COERCION_VAULT_IDS }
                .mapNotNull { entity ->
                    val item = decryptWith(entity, key)
                    if (item == null) {
                        Log.e(TAG_DECRYPT, "getAllForBackup: Vehicle id=${entity.id} ilegible, " +
                            "queda FUERA del backup")
                    }
                    item
                }
        }
    }

    private fun decryptWith(entity: VehicleEntity, key: SecretKeySpec): Vehicle? =
        try {
            fun dec(v: String) = if (v.isNotEmpty()) crypto.decrypt(v, key) else ""
            Vehicle(
                id                           = entity.id,
                make                         = dec(entity.make),
                model                        = dec(entity.model),
                year                         = dec(entity.year),
                licensePlate                 = dec(entity.licensePlate),
                vin                          = dec(entity.vin),
                inspectionExpiryDate         = dec(entity.inspectionExpiryDate),
                insuranceExpiryDate          = dec(entity.insuranceExpiryDate),
                insuranceCompany             = dec(entity.insuranceCompany),
                policyNumber                 = dec(entity.policyNumber),
                driverLicenseExpiryDate      = dec(entity.driverLicenseExpiryDate),
                notes                        = dec(entity.notes),
                inspectionExpiryTimestamp    = entity.inspectionExpiryTimestamp,
                inspectionAlertDays          = entity.inspectionAlertDays,
                insuranceExpiryTimestamp     = entity.insuranceExpiryTimestamp,
                insuranceAlertDays           = entity.insuranceAlertDays,
                driverLicenseExpiryTimestamp = entity.driverLicenseExpiryTimestamp,
                driverLicenseAlertDays       = entity.driverLicenseAlertDays,
                category                     = EntryCategory.entries.firstOrNull { it.name == entity.category }
                                                   ?: EntryCategory.SIN_CATEGORIZAR,
                tags                         = entity.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                isFavorite                   = entity.isFavorite,
                isTravelSafe                 = entity.isTravelSafe,
                isPinned                     = entity.isPinned,
                isHidden                     = entity.isHidden,
                isProtected = entity.isProtected,
                createdAt                    = entity.createdAt,
                updatedAt                    = entity.updatedAt,
                isDeleted                    = entity.isDeleted,
                deletedAt                    = entity.deletedAt,
                archivedAt                   = entity.archivedAt,
                vaultId                      = entity.vaultId,
                inheritedFrom                = entity.inheritedFrom,
            )
        } catch (e: Exception) {
            Log.e(TAG_DECRYPT, "decrypt falló para id=${entity.id}", e)
            null
        }

    private fun encrypt(vehicle: Vehicle, key: SecretKeySpec): VehicleEntity {
        fun enc(v: String) = if (v.isNotEmpty()) crypto.encrypt(v, key) else ""
        return VehicleEntity(
            id                           = vehicle.id,
            make                         = enc(vehicle.make),
            model                        = enc(vehicle.model),
            year                         = enc(vehicle.year),
            licensePlate                 = enc(vehicle.licensePlate),
            vin                          = enc(vehicle.vin),
            inspectionExpiryDate         = enc(vehicle.inspectionExpiryDate),
            insuranceExpiryDate          = enc(vehicle.insuranceExpiryDate),
            insuranceCompany             = enc(vehicle.insuranceCompany),
            policyNumber                 = enc(vehicle.policyNumber),
            driverLicenseExpiryDate      = enc(vehicle.driverLicenseExpiryDate),
            notes                        = enc(vehicle.notes),
            inspectionExpiryTimestamp    = vehicle.inspectionExpiryTimestamp,
            inspectionAlertDays          = vehicle.inspectionAlertDays,
            insuranceExpiryTimestamp     = vehicle.insuranceExpiryTimestamp,
            insuranceAlertDays           = vehicle.insuranceAlertDays,
            driverLicenseExpiryTimestamp = vehicle.driverLicenseExpiryTimestamp,
            driverLicenseAlertDays       = vehicle.driverLicenseAlertDays,
            category                     = vehicle.category.name,
            tags                         = vehicle.tags.joinToString(","),
            isFavorite                   = vehicle.isFavorite,
            isTravelSafe                 = vehicle.isTravelSafe,
            isPinned                     = vehicle.isPinned,
            isHidden                     = vehicle.isHidden,
            isProtected = vehicle.isProtected,
            createdAt                    = vehicle.createdAt,
            updatedAt                    = vehicle.updatedAt,
            isDeleted                    = vehicle.isDeleted,
            deletedAt                    = vehicle.deletedAt,
            archivedAt                   = vehicle.archivedAt,
            vaultId          = vehicle.vaultId,
            inheritedFrom    = vehicle.inheritedFrom,
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
            val original = getVehicle(id) ?: return@forEach
            val copy = original.copy(
                id = java.util.UUID.randomUUID().toString(),
                vaultId       = targetVaultId,
                inheritedFrom = inheritedFrom,
            isPinned      = false,
            createdAt     = System.currentTimeMillis(),
            updatedAt     = System.currentTimeMillis()
            )
            saveVehicle(copy)
        }
    }

    suspend fun removeInheritedFrom(id: String) = dao.clearInheritedFrom(id)
}
