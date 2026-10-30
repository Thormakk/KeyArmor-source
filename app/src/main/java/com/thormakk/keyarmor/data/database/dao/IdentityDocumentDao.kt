package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.IdentityDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdentityDocumentDao {

    @Query("SELECT * FROM identity_documents WHERE isDeleted = 0 AND isArchived = 0 AND vault_id = :vaultId ORDER BY updatedAt DESC")
    fun observeActive(vaultId: String): Flow<List<IdentityDocumentEntity>>

    @Query("SELECT * FROM identity_documents WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): IdentityDocumentEntity?

    @Query("SELECT * FROM identity_documents WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<IdentityDocumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: IdentityDocumentEntity)

    @Query("UPDATE identity_documents SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE identity_documents SET isTravelSafe = :isTravelSafe WHERE id = :id")
    suspend fun setTravelSafe(id: String, isTravelSafe: Boolean)

    @Query("UPDATE identity_documents SET isHidden = :isHidden WHERE id = :id")
    suspend fun setHidden(id: String, isHidden: Boolean)

    @Query("UPDATE identity_documents SET front_photo_path = :path WHERE id = :id")
    suspend fun setFrontPhotoPath(id: String, path: String?)

    @Query("UPDATE identity_documents SET back_photo_path = :path WHERE id = :id")
    suspend fun setBackPhotoPath(id: String, path: String?)

    @Query("SELECT COUNT(*) FROM identity_documents WHERE isDeleted = 0 AND isArchived = 0 AND vault_id = :vaultId")
    suspend fun countActive(vaultId: String): Int
    @Query("SELECT COUNT(*) FROM identity_documents WHERE vault_id = :vaultId")
    suspend fun countAllByVault(vaultId: String): Int


    @Query("SELECT COUNT(*) FROM identity_documents")
    suspend fun countAll(): Int

    @Query("UPDATE identity_documents SET isDeleted = 1, deletedAt = :now WHERE id = :id AND is_protected = 0")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE identity_documents SET isDeleted = 1, deletedAt = :now WHERE id IN (:ids) AND is_protected = 0")
    suspend fun softDeleteBatch(ids: List<String>, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM identity_documents WHERE isDeleted = 1 AND vault_id = :vaultId ORDER BY deletedAt DESC")
    fun observeDeleted(vaultId: String): Flow<List<IdentityDocumentEntity>>

    @Query("UPDATE identity_documents SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM identity_documents WHERE id = :id AND is_protected = 0")
    suspend fun deletePermanently(id: String)

    @Query("DELETE FROM identity_documents WHERE vault_id = :vaultId")
    suspend fun deleteAllByVaultId(vaultId: String)

    @Query("DELETE FROM identity_documents WHERE isDeleted = 1 AND vault_id = :vaultId AND is_protected = 0")
    suspend fun emptyTrash(vaultId: String)

    @Query("SELECT id FROM identity_documents WHERE isDeleted = 1 AND vault_id = :vaultId")
    suspend fun getTrashedIds(vaultId: String): List<String>

    @Query("SELECT id FROM identity_documents WHERE isDeleted = 1 AND deletedAt IS NOT NULL AND deletedAt <= :cutoff")
    suspend fun getExpiredTrashedIds(cutoff: Long): List<String>

    @Query("DELETE FROM identity_documents")
    suspend fun deleteAll()

    @Query("UPDATE identity_documents SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPin(id: String, isPinned: Boolean)

    @Query("SELECT COUNT(*) FROM identity_documents WHERE isPinned = 1 AND isDeleted = 0 AND vault_id = :vaultId")
    suspend fun countPinned(vaultId: String): Int

    @Query("SELECT id, tags FROM identity_documents WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%'")
    suspend fun getEntriesWithTag(tag: String): List<TagsRow>

    @Query("UPDATE identity_documents SET tags = :tags WHERE id = :id")
    suspend fun updateTags(id: String, tags: String)

    @Query("SELECT tags FROM identity_documents WHERE tags != '' AND isDeleted = 0")
    suspend fun getTagStrings(): List<String>

    @Query("SELECT COUNT(*) FROM identity_documents WHERE isDeleted = 0 AND ',' || tags || ',' LIKE '%,' || :tag || ',%'")
    suspend fun countEntriesWithTag(tag: String): Int

    @Query("""
        UPDATE identity_documents
        SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :tag || ',', ','), ',')
        WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%'
    """)
    suspend fun removeTagFromEntries(tag: String)

    @Query("UPDATE identity_documents SET isArchived = 1, archivedAt = :now WHERE id IN (:ids)")
    suspend fun archiveBatch(ids: List<String>, now: Long = System.currentTimeMillis())

    @Query("UPDATE identity_documents SET isArchived = 1, archivedAt = :now WHERE id = :id")
    suspend fun archive(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE identity_documents SET isArchived = 1, archivedAt = :now WHERE isDeleted = 0 AND isArchived = 0 AND isHidden = 0 AND is_protected = 0 AND vault_id = :vaultId")
    suspend fun archiveAll(vaultId: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE identity_documents SET isArchived = 0, archivedAt = NULL WHERE id = :id")
    suspend fun unarchive(id: String)

    @Query("SELECT * FROM identity_documents WHERE isArchived = 1 AND isDeleted = 0 AND vault_id = :vaultId ORDER BY archivedAt DESC")
    fun observeArchived(vaultId: String): Flow<List<IdentityDocumentEntity>>

    @Query("SELECT * FROM identity_documents ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<IdentityDocumentEntity>>

    @Query("SELECT COUNT(*) FROM identity_documents WHERE isArchived = 1 AND isDeleted = 0 AND vault_id = :vaultId")
    suspend fun countArchived(vaultId: String): Int

    @Query("SELECT * FROM identity_documents")
    suspend fun getAllEntities(): List<IdentityDocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<IdentityDocumentEntity>)

    @Query("SELECT id FROM identity_documents")
    suspend fun getAllIds(): List<String>

    @Query("SELECT expiryTimestamp FROM identity_documents WHERE isDeleted = 0 AND expiryTimestamp IS NOT NULL")
    suspend fun getExpiryTimestamps(): List<Long>

    @Query("SELECT id FROM identity_documents WHERE isDeleted = 0 AND expiryTimestamp IS NOT NULL AND expiryTimestamp > :now AND expiryTimestamp <= :until")
    suspend fun getExpiringIds(now: Long, until: Long): List<String>
    @Query("UPDATE identity_documents SET vault_id = :targetVaultId WHERE vault_id = :sourceVaultId")
    suspend fun moveToVault(sourceVaultId: String, targetVaultId: String)

    @Query("UPDATE identity_documents SET vault_id = :targetVaultId WHERE vault_id NOT IN (:validVaultIds)")
    suspend fun reassignOrphansToVault(validVaultIds: List<String>, targetVaultId: String): Int

    @Query("UPDATE identity_documents SET isPinned = 0 WHERE vault_id = :vaultId")
    suspend fun unpinAllByVault(vaultId: String)

    @Query("UPDATE `identity_documents` SET inherited_from = :inheritedFrom WHERE vault_id = :sourceVaultId")
    suspend fun stampInheritedFrom(sourceVaultId: String, inheritedFrom: String)

    @Query("UPDATE `identity_documents` SET isDeleted = 1, deletedAt = :now WHERE vault_id = :vaultId AND is_protected = 0")
    suspend fun softDeleteAllByVault(vaultId: String, now: Long)

    @Query("UPDATE `identity_documents` SET vault_id = :targetVaultId WHERE id = :entryId")
    suspend fun moveEntryToVault(entryId: String, targetVaultId: String)

    @Query("UPDATE `identity_documents` SET inherited_from = NULL WHERE id = :entryId")
    suspend fun clearInheritedFrom(entryId: String)

    @Query("UPDATE identity_documents SET is_protected = :isProtected WHERE id = :id")
    suspend fun setProtected(id: String, isProtected: Boolean)

    @Query("SELECT id FROM identity_documents WHERE is_protected = 1 AND id IN (:ids)")
    suspend fun getProtectedIds(ids: List<String>): List<String>

    @Query("SELECT COUNT(*) FROM identity_documents WHERE vault_id = :vaultId AND is_protected = 1 AND isDeleted = 0")
    suspend fun countProtectedByVault(vaultId: String): Int

    @Query("UPDATE identity_documents SET isHidden = 0 WHERE isHidden = 1")
    suspend fun unhideAll()
}
