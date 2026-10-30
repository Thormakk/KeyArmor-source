package com.thormakk.keyarmor.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_snapshots")
data class VaultSnapshotEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "trigger")
    val trigger: String,

    @ColumnInfo(name = "entry_count")
    val entryCount: Int,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "file_size_bytes")
    val fileSizeBytes: Long,

    @ColumnInfo(name = "synced_to_drive")
    val syncedToDrive: Boolean = false,

    @ColumnInfo(name = "includes_attachments", defaultValue = "1")
    val includesAttachments: Boolean = true
)
