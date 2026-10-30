package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.PasswordHistoryEntity

@Dao
interface PasswordHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: PasswordHistoryEntity)

    @Query("SELECT password_hash FROM password_history WHERE entry_id = :entryId ORDER BY saved_at DESC")
    suspend fun getHashesForEntry(entryId: String): List<String>

    @Query("SELECT * FROM password_history WHERE entry_id = :entryId ORDER BY saved_at DESC")
    suspend fun getHistoryForEntry(entryId: String): List<PasswordHistoryEntity>

    @Query("""
        DELETE FROM password_history
        WHERE entry_id = :entryId
          AND id NOT IN (
              SELECT id FROM password_history
              WHERE entry_id = :entryId
              ORDER BY saved_at DESC
              LIMIT :keepCount
          )
    """)
    suspend fun trimHistory(entryId: String, keepCount: Int = 10)

    @Query("DELETE FROM password_history WHERE entry_id = :entryId")
    suspend fun deleteForEntry(entryId: String)

    @Query("DELETE FROM password_history")
    suspend fun deleteAll()

    @Query("SELECT * FROM password_history")
    suspend fun getAllEntities(): List<PasswordHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<PasswordHistoryEntity>)
}
