package com.thormakk.keyarmor.autofill

import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.VaultEntry
import java.util.UUID

object AutofillEntryFactory {

    fun create(
        title:      String,
        url:        String,
        username:   String,
        email:      String,
        password:   String,
        fieldLabel: String,
    ): VaultEntry {
        val now = System.currentTimeMillis()
        return VaultEntry(
            id           = UUID.randomUUID().toString(),
            title        = title,
            username     = username,
            email        = email,
            password     = password,
            urls         = if (url.isNotEmpty()) listOf(url) else emptyList(),
            notes        = "",
            category     = EntryCategory.SIN_CATEGORIZAR,
            isFavorite   = false,
            isTravelSafe = false,
            isPinned     = false,
            createdAt    = now,
            updatedAt    = now,
            isDeleted    = false,
            deletedAt    = null,
            archivedAt   = null,
            fieldLabel   = fieldLabel,
        )
    }
}
