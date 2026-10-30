package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.PasskeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PasskeyDao {

    @Query("SELECT * FROM passkeys WHERE is_deleted = 0 AND is_archived = 0 AND vault_id = :vaultId ORDER BY created_at DESC")
    fun observeActive(vaultId: String): Flow<List<PasskeyEntity>>

    @Query("SELECT * FROM passkeys WHERE is_archived = 1 AND is_deleted = 0 AND vault_id = :vaultId ORDER BY archived_at DESC")
    fun observeArchived(vaultId: String): Flow<List<PasskeyEntity>>

    @Query("UPDATE passkeys SET is_archived = 1, archived_at = :now WHERE credential_id = :credentialId")
    suspend fun archive(credentialId: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE passkeys SET is_archived = 0, archived_at = NULL WHERE credential_id = :credentialId")
    suspend fun unarchive(credentialId: String)

    @Query("SELECT * FROM passkeys WHERE credential_id = :credentialId LIMIT 1")
    suspend fun getById(credentialId: String): PasskeyEntity?

    @Query("SELECT * FROM passkeys WHERE credential_id = :credentialId LIMIT 1")
    fun observeById(credentialId: String): Flow<PasskeyEntity?>

    @Query("SELECT * FROM passkeys WHERE rp_id = :rpId AND is_deleted = 0")
    suspend fun getByRpId(rpId: String): List<PasskeyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PasskeyEntity)

    @Query("""
        UPDATE passkeys SET
            notes               = :notes,
            urls                = :urls,
            linked_password_id  = :linkedPasswordId,
            category            = :category,
            tags                = :tags,
            custom_account_label = :customAccountLabel
        WHERE credential_id = :credentialId
    """)
    suspend fun updateMetadata(
        credentialId:       String,
        notes:              String,
        urls:               String,
        linkedPasswordId:   String,
        category:           String,
        tags:               String,
        customAccountLabel: String
    )

    @Query("UPDATE passkeys SET is_travel_safe = :value WHERE credential_id = :credentialId")
    suspend fun updateTravelSafe(credentialId: String, value: Boolean)

    @Query("UPDATE passkeys SET is_hidden = :value WHERE credential_id = :credentialId")
    suspend fun updateHidden(credentialId: String, value: Boolean)

    @Query("UPDATE passkeys SET is_pinned = :value WHERE credential_id = :credentialId")
    suspend fun updatePinned(credentialId: String, value: Boolean)

    @Query("UPDATE passkeys SET sign_count = :signCount, last_used_at = :now WHERE credential_id = :credentialId")
    suspend fun incrementSignCount(
        credentialId: String,
        signCount: Long,
        now: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM passkeys WHERE is_deleted = 1 AND vault_id = :vaultId ORDER BY deleted_at DESC")
    fun observeDeleted(vaultId: String): Flow<List<PasskeyEntity>>

    @Query("SELECT * FROM passkeys ORDER BY created_at DESC")
    fun observeAll(): Flow<List<PasskeyEntity>>

    @Query("SELECT * FROM passkeys WHERE is_deleted = 0 AND is_archived = 0 AND linked_password_id = :entryId LIMIT 1")
    fun observeByLinkedPasswordId(entryId: String): Flow<PasskeyEntity?>

    @Query("SELECT DISTINCT linked_password_id FROM passkeys WHERE is_deleted = 0 AND is_archived = 0 AND linked_password_id != ''")
    fun observeLinkedPasswordIds(): Flow<List<String>>

    @Query("UPDATE passkeys SET linked_password_id = '' WHERE linked_password_id = :entryId")
    suspend fun clearLinkForPasswordId(entryId: String)

    @Query("UPDATE passkeys SET is_deleted = 0, deleted_at = NULL WHERE credential_id = :credentialId")
    suspend fun restore(credentialId: String)

    @Query("UPDATE passkeys SET is_deleted = 1, deleted_at = :now WHERE credential_id = :credentialId AND is_protected = 0")
    suspend fun softDelete(credentialId: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE passkeys SET is_deleted = 1, deleted_at = :now WHERE vault_id = :vaultId AND is_protected = 0")
    suspend fun softDeleteAllByVault(vaultId: String, now: Long)

    @Query("DELETE FROM passkeys WHERE credential_id = :credentialId AND is_protected = 0")
    suspend fun deletePermanently(credentialId: String)

    @Query("DELETE FROM passkeys WHERE vault_id = :vaultId")
    suspend fun deleteAllByVaultId(vaultId: String)

    @Query("DELETE FROM passkeys WHERE is_deleted = 1 AND vault_id = :vaultId AND is_protected = 0")
    suspend fun emptyTrash(vaultId: String)

    @Query("SELECT credential_id FROM passkeys WHERE is_deleted = 1 AND deleted_at IS NOT NULL AND deleted_at <= :cutoff")
    suspend fun getExpiredTrashedIds(cutoff: Long): List<String>

    @Query("DELETE FROM passkeys")
    suspend fun deleteAll()

    @Query("DELETE FROM passkeys WHERE is_hidden = 0")
    suspend fun deleteAllExceptHidden()

    @Query("DELETE FROM passkeys WHERE is_hidden = 0 AND vault_id = :vaultId AND is_protected = 0")
    suspend fun deleteAllExceptHiddenInVault(vaultId: String)

    @Query("SELECT COUNT(*) FROM passkeys WHERE is_deleted = 0 AND is_archived = 0 AND vault_id = :vaultId")
    suspend fun countActive(vaultId: String): Int
    @Query("SELECT COUNT(*) FROM passkeys WHERE vault_id = :vaultId")
    suspend fun countAllByVault(vaultId: String): Int


    @Query("SELECT COUNT(*) FROM passkeys WHERE is_pinned = 1 AND is_deleted = 0 AND vault_id = :vaultId")
    suspend fun countPinned(vaultId: String): Int

    @Query("SELECT COUNT(*) FROM passkeys WHERE is_deleted = 0")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT * FROM passkeys")
    suspend fun getAllEntities(): List<PasskeyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PasskeyEntity>)
    @Query("UPDATE passkeys SET vault_id = :targetVaultId WHERE vault_id = :sourceVaultId")
    suspend fun moveToVault(sourceVaultId: String, targetVaultId: String)

    @Query("UPDATE passkeys SET vault_id = :targetVaultId WHERE vault_id NOT IN (:validVaultIds)")
    suspend fun reassignOrphansToVault(validVaultIds: List<String>, targetVaultId: String): Int

    @Query("UPDATE passkeys SET is_pinned = 0 WHERE vault_id = :vaultId")
    suspend fun unpinAllByVault(vaultId: String)

    @Query("UPDATE `passkeys` SET inherited_from = :inheritedFrom WHERE vault_id = :sourceVaultId")
    suspend fun stampInheritedFrom(sourceVaultId: String, inheritedFrom: String)


    @Query("UPDATE `passkeys` SET vault_id = :targetVaultId WHERE credential_id = :entryId")
    suspend fun moveEntryToVault(entryId: String, targetVaultId: String)

    @Query("UPDATE `passkeys` SET inherited_from = NULL WHERE credential_id = :entryId")
    suspend fun clearInheritedFrom(entryId: String)

    @Query("UPDATE passkeys SET is_protected = :isProtected WHERE credential_id = :credentialId")
    suspend fun setProtected(credentialId: String, isProtected: Boolean)

    @Query("SELECT credential_id FROM passkeys WHERE is_protected = 1 AND credential_id IN (:ids)")
    suspend fun getProtectedIds(ids: List<String>): List<String>

    @Query("SELECT COUNT(*) FROM passkeys WHERE vault_id = :vaultId AND is_protected = 1 AND is_deleted = 0")
    suspend fun countProtectedByVault(vaultId: String): Int

    @Query("UPDATE passkeys SET is_hidden = 0 WHERE is_hidden = 1")
    suspend fun unhideAll()
}
