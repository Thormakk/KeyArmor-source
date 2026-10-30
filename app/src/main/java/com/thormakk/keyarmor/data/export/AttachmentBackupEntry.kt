package com.thormakk.keyarmor.data.export

data class AttachmentBackupEntry(
    val id:        String,
    val entryId:   String,
    val fileName:  String,
    val mimeType:  String,
    val fileSize:  Long,
    val createdAt: Long,
    val content:   ByteArray
)
