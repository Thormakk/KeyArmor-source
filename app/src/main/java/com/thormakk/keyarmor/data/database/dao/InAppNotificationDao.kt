package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.InAppNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InAppNotificationDao {

    @Query("SELECT * FROM in_app_notifications WHERE is_trashed = 0 AND is_archived = 0 ORDER BY created_at DESC")
    fun observeAll(): Flow<List<InAppNotificationEntity>>

    @Query("SELECT * FROM in_app_notifications WHERE is_trashed = 1 ORDER BY trashed_at DESC")
    fun observeTrash(): Flow<List<InAppNotificationEntity>>

    @Query("SELECT COUNT(*) FROM in_app_notifications WHERE is_read = 0 AND is_trashed = 0")
    fun observeUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InAppNotificationEntity)

    @Query("UPDATE in_app_notifications SET is_read = 1 WHERE is_trashed = 0")
    suspend fun markAllRead()

    @Query("UPDATE in_app_notifications SET is_trashed = 1, trashed_at = :trashedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, trashedAt: Long = System.currentTimeMillis())

    @Query("UPDATE in_app_notifications SET is_trashed = 1, trashed_at = :trashedAt WHERE is_trashed = 0")
    suspend fun softDeleteAll(trashedAt: Long = System.currentTimeMillis())

    @Query("UPDATE in_app_notifications SET is_trashed = 0, trashed_at = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("DELETE FROM in_app_notifications WHERE id = :id")
    suspend fun permanentlyDeleteById(id: Long)

    @Query("DELETE FROM in_app_notifications WHERE is_trashed = 1")
    suspend fun permanentlyDeleteAllTrashed()

    @Query("DELETE FROM in_app_notifications WHERE is_trashed = 0 AND created_at < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)

    @Query("UPDATE in_app_notifications SET is_read = CASE WHEN is_read = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleRead(id: Long)

    @Query("UPDATE in_app_notifications SET is_archived = 1, archived_at = :archivedAt WHERE id = :id")
    suspend fun archiveById(id: Long, archivedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM in_app_notifications")
    suspend fun deleteAll()

    @Query("DELETE FROM in_app_notifications WHERE type = 'EXPIRING' AND target_id IS NULL AND is_trashed = 0 AND is_archived = 0")
    suspend fun deleteActiveExpiringWithNullTarget()
}
