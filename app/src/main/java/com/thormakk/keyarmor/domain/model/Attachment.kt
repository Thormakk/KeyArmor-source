package com.thormakk.keyarmor.domain.model

data class Attachment(
    val id:         String,
    val entryId:    String,
    val fileName:   String,
    val mimeType:   String,
    val fileSize:   Long,
    val storedPath: String,
    val createdAt:  Long
)
