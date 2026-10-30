package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.thormakk.keyarmor.data.database.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE entry_id = :entryId ORDER BY created_at ASC")
    fun observeForEntry(entryId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE entry_id = :entryId ORDER BY created_at ASC")
    suspend fun getForEntry(entryId: String): List<AttachmentEntity>

    @Query("SELECT COUNT(*) FROM attachments WHERE entry_id = :entryId")
    suspend fun countForEntry(entryId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttachmentEntity)

    @Transaction
    suspend fun insertIfUnderLimit(entity: AttachmentEntity, maxPerEntry: Int): Boolean {
        if (countForEntry(entity.entryId) >= maxPerEntry) return false
        insert(entity)
        return true
    }

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM attachments WHERE entry_id = :entryId")
    suspend fun deleteAllForEntry(entryId: String)

    @Query("SELECT * FROM attachments")
    suspend fun getAll(): List<AttachmentEntity>

    @Query("SELECT DISTINCT entry_id FROM attachments")
    fun observeEntryIdsWithAttachments(): Flow<List<String>>

    @Query("DELETE FROM attachments")
    suspend fun deleteAll()
}