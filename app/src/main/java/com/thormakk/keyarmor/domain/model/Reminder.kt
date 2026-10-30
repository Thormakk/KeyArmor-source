package com.thormakk.keyarmor.domain.model

data class Reminder(
    val id: String,
    val name: String,
    val date: Long,
    val note: String,
    val createdAt: Long,
    val isTrashed: Boolean = false,
    val trashedAt: Long? = null,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isProtected: Boolean = false,
    val tags: List<String> = emptyList(),
    val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
    val schedules: List<ReminderSchedule> = emptyList(),
    val hasAttachments: Boolean = false
)
