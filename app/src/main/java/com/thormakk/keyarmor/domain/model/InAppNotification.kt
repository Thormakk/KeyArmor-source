package com.thormakk.keyarmor.domain.model

enum class NotificationType {
    EXPIRING, EXPIRED, BREACH, HEIR_ACCEPTED, HEIR_DECLINED, REMINDER,
    HEIR_REMOVED,
    HEIR_RESIGNED,
    VAULT_INHERITED,
    INHERITANCE_SYNC,
    INHERITANCE_ERROR,

    FAMILY_INVITE_SENT,
    FAMILY_ERROR,
    FAMILY_MEMBER_LEFT,
    FAMILY_ACCESS_REVOKED,
    FAMILY_PREMIUM_REVOKED,
    FAMILY_MEMBER_DECLINED,

    TRAVEL_SUGGESTION
}

data class InAppNotification(
    val id:         Long,
    val type:       NotificationType,
    val title:      String,
    val body:       String,
    val createdAt:  Long,
    val isRead:     Boolean,
    val trashedAt:  Long? = null,
    val isArchived: Boolean = false,
    val archivedAt: Long?   = null,
    val targetId:   String? = null
)
