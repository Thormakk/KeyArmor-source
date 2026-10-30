package com.thormakk.keyarmor.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(

    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "note")
    val note: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_trashed")
    val isTrashed: Boolean = false,

    @ColumnInfo(name = "trashed_at")
    val trashedAt: Long? = null,

    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "is_archived", defaultValue = "0")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "is_protected", defaultValue = "0")
    val isProtected: Boolean = false,

    @ColumnInfo(name = "tags", defaultValue = "NULL")
    val tags: String? = null,

    @ColumnInfo(name = "category", defaultValue = "NULL")
    val category: String? = null,

    @ColumnInfo(name = "alert_schedules", defaultValue = "NULL")
    val alertSchedulesJson: String? = null
)
