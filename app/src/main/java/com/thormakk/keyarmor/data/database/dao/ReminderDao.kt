package com.thormakk.keyarmor.data.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

data class ExpiringReminderInfo(
    val id: String,
    @ColumnInfo(name = "date") val date: Long
)

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE is_trashed = 0 ORDER BY date ASC")
    fun observeActive(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE is_trashed = 1 ORDER BY trashed_at DESC")
    fun observeTrash(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReminderEntity)

    @Query("SELECT * FROM reminders")
    suspend fun getAllEntities(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ReminderEntity>)

    @Query("SELECT date FROM reminders WHERE is_trashed = 0")
    suspend fun getExpiryDates(): List<Long>

    @Query("SELECT id, date FROM reminders WHERE is_trashed = 0")
    suspend fun getExpiringReminderInfos(): List<ExpiringReminderInfo>

    @Query("UPDATE reminders SET is_trashed = 1, trashed_at = :trashedAt WHERE id = :id AND is_protected = 0")
    suspend fun softDelete(id: String, trashedAt: Long = System.currentTimeMillis())

    @Query("UPDATE reminders SET is_trashed = 1, trashed_at = :trashedAt WHERE id IN (:ids) AND is_protected = 0")
    suspend fun softDeleteBatch(ids: List<String>, trashedAt: Long = System.currentTimeMillis())

    @Query("UPDATE reminders SET is_protected = :isProtected WHERE id = :id")
    suspend fun setProtected(id: String, isProtected: Boolean)

    @Query("SELECT id FROM reminders WHERE is_protected = 1 AND id IN (:ids)")
    suspend fun getProtectedIds(ids: List<String>): List<String>

    @Query("UPDATE reminders SET is_pinned = :pinned WHERE id = :id")
    suspend fun setPin(id: String, pinned: Boolean)

    @Query("UPDATE reminders SET is_favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("UPDATE reminders SET is_archived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("UPDATE reminders SET is_archived = :archived WHERE id IN (:ids)")
    suspend fun setArchivedBatch(ids: List<String>, archived: Boolean)

    @Query("UPDATE reminders SET tags = :tagsJson WHERE id = :id")
    suspend fun setTags(id: String, tagsJson: String?)

    @Query("SELECT id, tags FROM reminders WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%'")
    suspend fun getEntriesWithTag(tag: String): List<TagsRow>

    @Query("UPDATE reminders SET tags = :tags WHERE id = :id")
    suspend fun updateTags(id: String, tags: String)

    @Query("SELECT tags FROM reminders WHERE tags IS NOT NULL AND tags != '' AND is_trashed = 0")
    suspend fun getTagStrings(): List<String>

    @Query("""
        UPDATE reminders
        SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :tag || ',', ','), ',')
        WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%'
    """)
    suspend fun removeTagFromEntries(tag: String)

    @Query("UPDATE reminders SET category = :category WHERE id = :id")
    suspend fun setCategory(id: String, category: String?)

    @Query("UPDATE reminders SET is_trashed = 0, trashed_at = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: String)

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()

    @Query("DELETE FROM reminders WHERE id = :id AND is_protected = 0")
    suspend fun permanentlyDelete(id: String)

    @Query("SELECT * FROM reminders WHERE is_trashed = 0 AND alert_schedules IS NOT NULL")
    suspend fun getActiveWithSchedules(): List<ReminderEntity>
}
