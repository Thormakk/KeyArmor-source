package com.thormakk.keyarmor.domain.model

data class VaultSnapshot(
    val id:            String,
    val timestamp:     Long,
    val trigger:       SnapshotTrigger,
    val entryCount:    Int,
    val filePath:      String,
    val fileSizeBytes: Long,
    val syncedToDrive: Boolean,
    val includesAttachments: Boolean = true
)
