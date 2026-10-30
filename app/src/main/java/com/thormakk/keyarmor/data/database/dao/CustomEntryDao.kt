package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.CustomEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomEntryDao {

    @Query("SELECT * FROM custom_entries WHERE isDeleted = 0 AND isArchived = 0 AND vault_id = :vaultId ORDER BY updatedAt DESC")
    fun observeActive(vaultId: String): Flow<List<CustomEntryEntity>>

    @Query("SELECT * FROM custom_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CustomEntryEntity?

    @Query("SELECT * FROM custom_entries WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<CustomEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: CustomEntryEntity)

    @Query("UPDATE custom_entries SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE custom_entries SET isTravelSafe = :isTravelSafe WHERE id = :id")
    suspend fun setTravelSafe(id: String, isTravelSafe: Boolean)

    @Query("UPDATE custom_entries SET isHidden = :isHidden WHERE id = :id")
    suspend fun setHidden(id: String, isHidden: Boolean)

    @Query("SELECT COUNT(*) FROM custom_entries WHERE isDeleted = 0 AND isArchived = 0 AND vault_id = :vaultId")
    suspend fun countActive(vaultId: String): Int
    @Query("SELECT COUNT(*) FROM custom_entries WHERE vault_id = :vaultId")
    suspend fun countAllByVault(vaultId: String): Int


    @Query("SELECT COUNT(*) FROM custom_entries")
    suspend fun countAll(): Int

    @Query("UPDATE custom_entries SET isDeleted = 1, deletedAt = :now WHERE id = :id AND is_protected = 0")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE custom_entries SET isDeleted = 1, deletedAt = :now WHERE id IN (:ids) AND is_protected = 0")
    suspend fun softDeleteBatch(ids: List<String>, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM custom_entries WHERE isDeleted = 1 AND vault_id = :vaultId ORDER BY deletedAt DESC")
    fun observeDeleted(vaultId: String): Flow<List<CustomEntryEntity>>

    @Query("UPDATE custom_entries SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM custom_entries WHERE id = :id AND is_protected = 0")
    suspend fun deletePermanently(id: String)

    @Query("DELETE FROM custom_entries WHERE vault_id = :vaultId")
    suspend fun deleteAllByVaultId(vaultId: String)

    @Query("DELETE FROM custom_entries WHERE isDeleted = 1 AND vault_id = :vaultId AND is_protected = 0")
    suspend fun emptyTrash(vaultId: String)

    @Query("SELECT id FROM custom_entries WHERE isDeleted = 1 AND vault_id = :vaultId")
    suspend fun getTrashedIds(vaultId: String): List<String>

    @Query("SELECT id FROM custom_entries WHERE isDeleted = 1 AND deletedAt IS NOT NULL AND deletedAt <= :cutoff")
    suspend fun getExpiredTrashedIds(cutoff: Long): List<String>

    @Query("DELETE FROM custom_entries")
    suspend fun deleteAll()

    @Query("UPDATE custom_entries SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPin(id: String, isPinned: Boolean)

    @Query("SELECT COUNT(*) FROM custom_entries WHERE isPinned = 1 AND isDeleted = 0 AND vault_id = :vaultId")
    suspend fun countPinned(vaultId: String): Int

    @Query("SELECT id, tags FROM custom_entries WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%'")
    suspend fun getEntriesWithTag(tag: String): List<TagsRow>

    @Query("UPDATE custom_entries SET tags = :tags WHERE id = :id")
    suspend fun updateTags(id: String, tags: String)

    @Query("SELECT tags FROM custom_entries WHERE tags != '' AND isDeleted = 0")
    suspend fun getTagStrings(): List<String>

    @Query("SELECT COUNT(*) FROM custom_entries WHERE isDeleted = 0 AND ',' || tags || ',' LIKE '%,' || :tag || ',%'")
    suspend fun countEntriesWithTag(tag: String): Int

    @Query("""
        UPDATE custom_entries
        SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :tag || ',', ','), ',')
        WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%'
    """)
    suspend fun removeTagFromEntries(tag: String)

    @Query("UPDATE custom_entries SET isArchived = 1, archivedAt = :now WHERE id IN (:ids)")
    suspend fun archiveBatch(ids: List<String>, now: Long = System.currentTimeMillis())

    @Query("UPDATE custom_entries SET isArchived = 1, archivedAt = :now WHERE id = :id")
    suspend fun archive(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE custom_entries SET isArchived = 1, archivedAt = :now WHERE isDeleted = 0 AND isArchived = 0 AND isHidden = 0 AND is_protected = 0 AND vault_id = :vaultId")
    suspend fun archiveAll(vaultId: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE custom_entries SET isArchived = 0, archivedAt = NULL WHERE id = :id")
    suspend fun unarchive(id: String)

    @Query("SELECT * FROM custom_entries WHERE isArchived = 1 AND isDeleted = 0 AND vault_id = :vaultId ORDER BY archivedAt DESC")
    fun observeArchived(vaultId: String): Flow<List<CustomEntryEntity>>

    @Query("SELECT * FROM custom_entries ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CustomEntryEntity>>

    @Query("SELECT COUNT(*) FROM custom_entries WHERE isArchived = 1 AND isDeleted = 0 AND vault_id = :vaultId")
    suspend fun countArchived(vaultId: String): Int

    @Query("SELECT * FROM custom_entries")
    suspend fun getAllEntities(): List<CustomEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<CustomEntryEntity>)
    @Query("SELECT id FROM custom_entries")
    suspend fun getAllIds(): List<String>
    @Query("UPDATE custom_entries SET vault_id = :targetVaultId WHERE vault_id = :sourceVaultId")
    suspend fun moveToVault(sourceVaultId: String, targetVaultId: String)

    @Query("UPDATE custom_entries SET vault_id = :targetVaultId WHERE vault_id NOT IN (:validVaultIds)")
    suspend fun reassignOrphansToVault(validVaultIds: List<String>, targetVaultId: String): Int

    @Query("UPDATE custom_entries SET isPinned = 0 WHERE vault_id = :vaultId")
    suspend fun unpinAllByVault(vaultId: String)

    @Query("UPDATE `custom_entries` SET inherited_from = :inheritedFrom WHERE vault_id = :sourceVaultId")
    suspend fun stampInheritedFrom(sourceVaultId: String, inheritedFrom: String)

    @Query("UPDATE `custom_entries` SET isDeleted = 1, deletedAt = :now WHERE vault_id = :vaultId AND is_protected = 0")
    suspend fun softDeleteAllByVault(vaultId: String, now: Long)

    @Query("UPDATE `custom_entries` SET vault_id = :targetVaultId WHERE id = :entryId")
    suspend fun moveEntryToVault(entryId: String, targetVaultId: String)

    @Query("UPDATE `custom_entries` SET inherited_from = NULL WHERE id = :entryId")
    suspend fun clearInheritedFrom(entryId: String)

    @Query("UPDATE custom_entries SET is_protected = :isProtected WHERE id = :id")
    suspend fun setProtected(id: String, isProtected: Boolean)

    @Query("SELECT id FROM custom_entries WHERE is_protected = 1 AND id IN (:ids)")
    suspend fun getProtectedIds(ids: List<String>): List<String>

    @Query("SELECT COUNT(*) FROM custom_entries WHERE vault_id = :vaultId AND is_protected = 1 AND isDeleted = 0")
    suspend fun countProtectedByVault(vaultId: String): Int

    @Query("UPDATE custom_entries SET isHidden = 0 WHERE isHidden = 1")
    suspend fun unhideAll()
}
