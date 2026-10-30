package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.SecureNoteHistoryEntity

@Dao
interface SecureNoteHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SecureNoteHistoryEntity)

    @Query("SELECT * FROM secure_note_history WHERE note_id = :noteId ORDER BY saved_at DESC")
    suspend fun getHistoryForNote(noteId: String): List<SecureNoteHistoryEntity>

    @Query("""
        DELETE FROM secure_note_history
        WHERE note_id = :noteId
          AND id NOT IN (
              SELECT id FROM secure_note_history
              WHERE note_id = :noteId
              ORDER BY saved_at DESC
              LIMIT :keepCount
          )
    """)
    suspend fun trimHistory(noteId: String, keepCount: Int = 3)

    @Query("DELETE FROM secure_note_history WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("DELETE FROM secure_note_history WHERE note_id = :noteId")
    suspend fun deleteForNote(noteId: String)

    @Query("DELETE FROM secure_note_history")
    suspend fun deleteAll()

    @Query("SELECT * FROM secure_note_history")
    suspend fun getAllEntities(): List<SecureNoteHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<SecureNoteHistoryEntity>)
}
