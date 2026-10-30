package com.thormakk.keyarmor.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "password_history",
    indices = [Index(value = ["entry_id"], name = "idx_password_history_entry_id")]
)
data class PasswordHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "entry_id")
    val entryId: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "encrypted_password")
    val encryptedPassword: String = "",

    @ColumnInfo(name = "saved_at")
    val savedAt: Long
)
