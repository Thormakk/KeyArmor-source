package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.BankAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankAccountDao {

    @Query("SELECT * FROM bank_accounts WHERE isDeleted = 0 AND isArchived = 0 AND vault_id = :vaultId ORDER BY updatedAt DESC")
    fun observeActive(vaultId: String): Flow<List<BankAccountEntity>>

    @Query("SELECT * FROM bank_accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BankAccountEntity?

    @Query("SELECT * FROM bank_accounts WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<BankAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: BankAccountEntity)

    @Query("UPDATE bank_accounts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE bank_accounts SET isTravelSafe = :isTravelSafe WHERE id = :id")
    suspend fun setTravelSafe(id: String, isTravelSafe: Boolean)

    @Query("UPDATE bank_accounts SET isHidden = :isHidden WHERE id = :id")
    suspend fun setHidden(id: String, isHidden: Boolean)

    @Query("SELECT COUNT(*) FROM bank_accounts WHERE isDeleted = 0 AND isArchived = 0 AND vault_id = :vaultId")
    suspend fun countActive(vaultId: String): Int
    @Query("SELECT COUNT(*) FROM bank_accounts WHERE vault_id = :vaultId")
    suspend fun countAllByVault(vaultId: String): Int


    @Query("SELECT COUNT(*) FROM bank_accounts")
    suspend fun countAll(): Int

    @Query("UPDATE bank_accounts SET isDeleted = 1, deletedAt = :now WHERE id = :id AND is_protected = 0")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE bank_accounts SET isDeleted = 1, deletedAt = :now WHERE id IN (:ids) AND is_protected = 0")
    suspend fun softDeleteBatch(ids: List<String>, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM bank_accounts WHERE isDeleted = 1 AND vault_id = :vaultId ORDER BY deletedAt DESC")
    fun observeDeleted(vaultId: String): Flow<List<BankAccountEntity>>

    @Query("UPDATE bank_accounts SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM bank_accounts WHERE id = :id AND is_protected = 0")
    suspend fun deletePermanently(id: String)

    @Query("DELETE FROM bank_accounts WHERE vault_id = :vaultId")
    suspend fun deleteAllByVaultId(vaultId: String)

    @Query("DELETE FROM bank_accounts WHERE isDeleted = 1 AND vault_id = :vaultId AND is_protected = 0")
    suspend fun emptyTrash(vaultId: String)

    @Query("SELECT id FROM bank_accounts WHERE isDeleted = 1 AND vault_id = :vaultId")
    suspend fun getTrashedIds(vaultId: String): List<String>

    @Query("SELECT id FROM bank_accounts WHERE isDeleted = 1 AND deletedAt IS NOT NULL AND deletedAt <= :cutoff")
    suspend fun getExpiredTrashedIds(cutoff: Long): List<String>

    @Query("DELETE FROM bank_accounts")
    suspend fun deleteAll()

    @Query("UPDATE bank_accounts SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPin(id: String, isPinned: Boolean)

    @Query("SELECT COUNT(*) FROM bank_accounts WHERE isPinned = 1 AND isDeleted = 0 AND vault_id = :vaultId")
    suspend fun countPinned(vaultId: String): Int

    @Query("SELECT id, tags FROM bank_accounts WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%'")
    suspend fun getEntriesWithTag(tag: String): List<TagsRow>

    @Query("UPDATE bank_accounts SET tags = :tags WHERE id = :id")
    suspend fun updateTags(id: String, tags: String)

    @Query("SELECT tags FROM bank_accounts WHERE tags != '' AND isDeleted = 0")
    suspend fun getTagStrings(): List<String>

    @Query("SELECT COUNT(*) FROM bank_accounts WHERE isDeleted = 0 AND ',' || tags || ',' LIKE '%,' || :tag || ',%'")
    suspend fun countEntriesWithTag(tag: String): Int

    @Query("""
        UPDATE bank_accounts
        SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :tag || ',', ','), ',')
        WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%'
    """)
    suspend fun removeTagFromEntries(tag: String)

    @Query("UPDATE bank_accounts SET isArchived = 1, archivedAt = :now WHERE id IN (:ids)")
    suspend fun archiveBatch(ids: List<String>, now: Long = System.currentTimeMillis())

    @Query("UPDATE bank_accounts SET isArchived = 1, archivedAt = :now WHERE id = :id")
    suspend fun archive(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE bank_accounts SET isArchived = 1, archivedAt = :now WHERE isDeleted = 0 AND isArchived = 0 AND isHidden = 0 AND is_protected = 0 AND vault_id = :vaultId")
    suspend fun archiveAll(vaultId: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE bank_accounts SET isArchived = 0, archivedAt = NULL WHERE id = :id")
    suspend fun unarchive(id: String)

    @Query("SELECT * FROM bank_accounts WHERE isArchived = 1 AND isDeleted = 0 AND vault_id = :vaultId ORDER BY archivedAt DESC")
    fun observeArchived(vaultId: String): Flow<List<BankAccountEntity>>

    @Query("SELECT * FROM bank_accounts ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<BankAccountEntity>>

    @Query("SELECT COUNT(*) FROM bank_accounts WHERE isArchived = 1 AND isDeleted = 0 AND vault_id = :vaultId")
    suspend fun countArchived(vaultId: String): Int

    @Query("SELECT * FROM bank_accounts")
    suspend fun getAllEntities(): List<BankAccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<BankAccountEntity>)
    @Query("SELECT id FROM bank_accounts")
    suspend fun getAllIds(): List<String>
    @Query("UPDATE bank_accounts SET vault_id = :targetVaultId WHERE vault_id = :sourceVaultId")
    suspend fun moveToVault(sourceVaultId: String, targetVaultId: String)

    @Query("UPDATE bank_accounts SET vault_id = :targetVaultId WHERE vault_id NOT IN (:validVaultIds)")
    suspend fun reassignOrphansToVault(validVaultIds: List<String>, targetVaultId: String): Int

    @Query("UPDATE bank_accounts SET isPinned = 0 WHERE vault_id = :vaultId")
    suspend fun unpinAllByVault(vaultId: String)

    @Query("UPDATE `bank_accounts` SET inherited_from = :inheritedFrom WHERE vault_id = :sourceVaultId")
    suspend fun stampInheritedFrom(sourceVaultId: String, inheritedFrom: String)

    @Query("UPDATE `bank_accounts` SET isDeleted = 1, deletedAt = :now WHERE vault_id = :vaultId AND is_protected = 0")
    suspend fun softDeleteAllByVault(vaultId: String, now: Long)

    @Query("UPDATE `bank_accounts` SET vault_id = :targetVaultId WHERE id = :entryId")
    suspend fun moveEntryToVault(entryId: String, targetVaultId: String)

    @Query("UPDATE `bank_accounts` SET inherited_from = NULL WHERE id = :entryId")
    suspend fun clearInheritedFrom(entryId: String)

    @Query("UPDATE bank_accounts SET linkedPasswordId = '' WHERE linkedPasswordId = :entryId")
    suspend fun clearLinkForPasswordId(entryId: String)

    @Query("UPDATE bank_accounts SET is_protected = :isProtected WHERE id = :id")
    suspend fun setProtected(id: String, isProtected: Boolean)

    @Query("SELECT id FROM bank_accounts WHERE is_protected = 1 AND id IN (:ids)")
    suspend fun getProtectedIds(ids: List<String>): List<String>

    @Query("SELECT COUNT(*) FROM bank_accounts WHERE vault_id = :vaultId AND is_protected = 1 AND isDeleted = 0")
    suspend fun countProtectedByVault(vaultId: String): Int

    @Query("UPDATE bank_accounts SET isHidden = 0 WHERE isHidden = 1")
    suspend fun unhideAll()
}
