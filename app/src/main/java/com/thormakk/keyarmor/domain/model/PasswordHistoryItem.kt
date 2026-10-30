package com.thormakk.keyarmor.domain.model

data class PasswordHistoryItem(
    val id: Int,
    val password: String,
    val savedAt: Long
)
