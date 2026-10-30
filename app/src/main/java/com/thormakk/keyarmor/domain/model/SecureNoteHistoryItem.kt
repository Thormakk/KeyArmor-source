package com.thormakk.keyarmor.domain.model

data class SecureNoteHistoryItem(
    val id:      Int,
    val noteId:  String,
    val content: String,
    val savedAt: Long
)
