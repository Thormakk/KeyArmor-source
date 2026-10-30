package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicles WHERE isDeleted = 0 AND isArchived = 0 AND vault_id = :vaultId ORDER BY updatedAt DESC")
    fun observeActive(vaultId: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<VehicleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VehicleEntity)

    @Query("UPDATE vehicles SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE vehicles SET isTravelSafe = :isTravelSafe WHERE id = :id")
    suspend fun setTravelSafe(id: String, isTravelSafe: Boolean)

    @Query("UPDATE vehicles SET isHidden = :isHidden WHERE id = :id")
    suspend fun setHidden(id: String, isHidden: Boolean)

    @Query("SELECT COUNT(*) FROM vehicles WHERE isDeleted = 0 AND isArchived = 0 AND vault_id = :vaultId")
    suspend fun countActive(vaultId: String): Int
    @Query("SELECT COUNT(*) FROM vehicles WHERE vault_id = :vaultId")
    suspend fun countAllByVault(vaultId: String): Int


    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun countAll(): Int

    @Query("UPDATE vehicles SET isDeleted = 1, deletedAt = :now WHERE id = :id AND is_protected = 0")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE vehicles SET isDeleted = 1, deletedAt = :now WHERE id IN (:ids) AND is_protected = 0")
    suspend fun softDeleteBatch(ids: List<String>, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM vehicles WHERE isDeleted = 1 AND vault_id = :vaultId ORDER BY deletedAt DESC")
    fun observeDeleted(vaultId: String): Flow<List<VehicleEntity>>

    @Query("UPDATE vehicles SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM vehicles WHERE id = :id AND is_protected = 0")
    suspend fun deletePermanently(id: String)

    @Query("DELETE FROM vehicles WHERE vault_id = :vaultId")
    suspend fun deleteAllByVaultId(vaultId: String)

    @Query("DELETE FROM vehicles WHERE isDeleted = 1 AND vault_id = :vaultId AND is_protected = 0")
    suspend fun emptyTrash(vaultId: String)

    @Query("SELECT id FROM vehicles WHERE isDeleted = 1 AND vault_id = :vaultId")
    suspend fun getTrashedIds(vaultId: String): List<String>

    @Query("SELECT id FROM vehicles WHERE isDeleted = 1 AND deletedAt IS NOT NULL AND deletedAt <= :cutoff")
    suspend fun getExpiredTrashedIds(cutoff: Long): List<String>

    @Query("DELETE FROM vehicles")
    suspend fun deleteAll()

    @Query("UPDATE vehicles SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPin(id: String, isPinned: Boolean)

    @Query("SELECT COUNT(*) FROM vehicles WHERE isPinned = 1 AND isDeleted = 0 AND vault_id = :vaultId")
    suspend fun countPinned(vaultId: String): Int

    @Query("SELECT id, tags FROM vehicles WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%'")
    suspend fun getEntriesWithTag(tag: String): List<TagsRow>

    @Query("UPDATE vehicles SET tags = :tags WHERE id = :id")
    suspend fun updateTags(id: String, tags: String)

    @Query("SELECT tags FROM vehicles WHERE tags != '' AND isDeleted = 0")
    suspend fun getTagStrings(): List<String>

    @Query("SELECT COUNT(*) FROM vehicles WHERE isDeleted = 0 AND ',' || tags || ',' LIKE '%,' || :tag || ',%'")
    suspend fun countEntriesWithTag(tag: String): Int

    @Query("""
        UPDATE vehicles
        SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :tag || ',', ','), ',')
        WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%'
    """)
    suspend fun removeTagFromEntries(tag: String)

    @Query("UPDATE vehicles SET isArchived = 1, archivedAt = :now WHERE id IN (:ids)")
    suspend fun archiveBatch(ids: List<String>, now: Long = System.currentTimeMillis())

    @Query("UPDATE vehicles SET isArchived = 1, archivedAt = :now WHERE id = :id")
    suspend fun archive(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE vehicles SET isArchived = 1, archivedAt = :now WHERE isDeleted = 0 AND isArchived = 0 AND isHidden = 0 AND is_protected = 0 AND vault_id = :vaultId")
    suspend fun archiveAll(vaultId: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE vehicles SET isArchived = 0, archivedAt = NULL WHERE id = :id")
    suspend fun unarchive(id: String)

    @Query("SELECT * FROM vehicles WHERE isArchived = 1 AND isDeleted = 0 AND vault_id = :vaultId ORDER BY archivedAt DESC")
    fun observeArchived(vaultId: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<VehicleEntity>>

    @Query("SELECT COUNT(*) FROM vehicles WHERE isArchived = 1 AND isDeleted = 0 AND vault_id = :vaultId")
    suspend fun countArchived(vaultId: String): Int

    @Query("SELECT * FROM vehicles")
    suspend fun getAllEntities(): List<VehicleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<VehicleEntity>)

    @Query("SELECT id FROM vehicles")
    suspend fun getAllIds(): List<String>

    @Query("""
        SELECT inspectionExpiryTimestamp FROM vehicles WHERE isDeleted = 0 AND inspectionExpiryTimestamp IS NOT NULL
        UNION ALL
        SELECT insuranceExpiryTimestamp FROM vehicles WHERE isDeleted = 0 AND insuranceExpiryTimestamp IS NOT NULL
        UNION ALL
        SELECT driverLicenseExpiryTimestamp FROM vehicles WHERE isDeleted = 0 AND driverLicenseExpiryTimestamp IS NOT NULL
    """)
    suspend fun getExpiryTimestamps(): List<Long>

    @Query("""
        SELECT DISTINCT id FROM vehicles WHERE isDeleted = 0 AND (
            (inspectionExpiryTimestamp IS NOT NULL AND inspectionExpiryTimestamp > :now AND inspectionExpiryTimestamp <= :until) OR
            (insuranceExpiryTimestamp IS NOT NULL AND insuranceExpiryTimestamp > :now AND insuranceExpiryTimestamp <= :until) OR
            (driverLicenseExpiryTimestamp IS NOT NULL AND driverLicenseExpiryTimestamp > :now AND driverLicenseExpiryTimestamp <= :until)
        )
    """)
    suspend fun getExpiringIds(now: Long, until: Long): List<String>
    @Query("UPDATE vehicles SET vault_id = :targetVaultId WHERE vault_id = :sourceVaultId")
    suspend fun moveToVault(sourceVaultId: String, targetVaultId: String)

    @Query("UPDATE vehicles SET vault_id = :targetVaultId WHERE vault_id NOT IN (:validVaultIds)")
    suspend fun reassignOrphansToVault(validVaultIds: List<String>, targetVaultId: String): Int

    @Query("UPDATE vehicles SET isPinned = 0 WHERE vault_id = :vaultId")
    suspend fun unpinAllByVault(vaultId: String)

    @Query("UPDATE `vehicles` SET inherited_from = :inheritedFrom WHERE vault_id = :sourceVaultId")
    suspend fun stampInheritedFrom(sourceVaultId: String, inheritedFrom: String)

    @Query("UPDATE `vehicles` SET isDeleted = 1, deletedAt = :now WHERE vault_id = :vaultId AND is_protected = 0")
    suspend fun softDeleteAllByVault(vaultId: String, now: Long)

    @Query("UPDATE `vehicles` SET vault_id = :targetVaultId WHERE id = :entryId")
    suspend fun moveEntryToVault(entryId: String, targetVaultId: String)

    @Query("UPDATE `vehicles` SET inherited_from = NULL WHERE id = :entryId")
    suspend fun clearInheritedFrom(entryId: String)

    @Query("UPDATE vehicles SET is_protected = :isProtected WHERE id = :id")
    suspend fun setProtected(id: String, isProtected: Boolean)

    @Query("SELECT id FROM vehicles WHERE is_protected = 1 AND id IN (:ids)")
    suspend fun getProtectedIds(ids: List<String>): List<String>

    @Query("SELECT COUNT(*) FROM vehicles WHERE vault_id = :vaultId AND is_protected = 1 AND isDeleted = 0")
    suspend fun countProtectedByVault(vaultId: String): Int

    @Query("UPDATE vehicles SET isHidden = 0 WHERE isHidden = 1")
    suspend fun unhideAll()
}
