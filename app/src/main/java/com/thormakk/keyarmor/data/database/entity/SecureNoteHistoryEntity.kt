package com.thormakk.keyarmor.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "secure_note_history",
    indices   = [Index(value = ["note_id"], name = "idx_note_history_note_id")]
)
data class SecureNoteHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "encrypted_content")
    val encryptedContent: String,

    @ColumnInfo(name = "saved_at")
    val savedAt: Long
)
