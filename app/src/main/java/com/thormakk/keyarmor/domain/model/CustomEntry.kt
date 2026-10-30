package com.thormakk.keyarmor.domain.model

import androidx.compose.ui.text.input.KeyboardType
import java.util.UUID

enum class CustomFieldType(
    val keyboardType: KeyboardType,
    val isSecret: Boolean,
    val hasCopy: Boolean,
    val isMultiline: Boolean
) {
    TEXT(
        keyboardType = KeyboardType.Uri,
        isSecret     = false,
        hasCopy      = false,
        isMultiline  = false
    ),
    NUMBER(
        keyboardType = KeyboardType.Number,
        isSecret     = false,
        hasCopy      = false,
        isMultiline  = false
    ),
    PASSWORD(
        keyboardType = KeyboardType.Password,
        isSecret     = true,
        hasCopy      = true,
        isMultiline  = false
    ),
    DATE(
        keyboardType = KeyboardType.Number,
        isSecret     = false,
        hasCopy      = false,
        isMultiline  = false
    ),
    EMAIL(
        keyboardType = KeyboardType.Email,
        isSecret     = false,
        hasCopy      = true,
        isMultiline  = false
    ),
    URL(
        keyboardType = KeyboardType.Uri,
        isSecret     = false,
        hasCopy      = true,
        isMultiline  = false
    ),
    WALLET(
        keyboardType = KeyboardType.Uri,
        isSecret     = false,
        hasCopy      = true,
        isMultiline  = false
    ),
    PHONE(
        keyboardType = KeyboardType.Phone,
        isSecret     = false,
        hasCopy      = true,
        isMultiline  = false
    ),
    PIN(
        keyboardType = KeyboardType.NumberPassword,
        isSecret     = true,
        hasCopy      = true,
        isMultiline  = false
    ),
    CODE(
        keyboardType = KeyboardType.Uri,
        isSecret     = false,
        hasCopy      = true,
        isMultiline  = false
    ),
    NOTE(
        keyboardType = KeyboardType.Text,
        isSecret     = false,
        hasCopy      = false,
        isMultiline  = true
    );

    companion object {
        fun fromName(name: String): CustomFieldType =
            entries.firstOrNull { it.name == name } ?: TEXT
    }
}

fun CustomFieldType.validate(value: String): Boolean {
    if (value.isEmpty()) return true
    return when (this) {
        CustomFieldType.EMAIL  -> android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()
        CustomFieldType.URL    -> android.util.Patterns.WEB_URL.matcher(value).matches()
        CustomFieldType.PHONE  -> value.filter { it.isDigit() }.length in 7..15
        CustomFieldType.DATE   -> {
            val digits = value.filter { it.isDigit() }
            digits.length == 8
        }
        CustomFieldType.PIN,
        CustomFieldType.NUMBER -> value.all { it.isDigit() }
        else                   -> true
    }
}

data class CustomField(
    val id:    String           = UUID.randomUUID().toString(),
    val label: String           = "",
    val type:  CustomFieldType  = CustomFieldType.TEXT,
    val value: String           = ""
)

data class CustomEntry(
    val id:         String            = UUID.randomUUID().toString(),
    val title:      String            = "",
    val fields:     List<CustomField> = emptyList(),
    val category:   EntryCategory     = EntryCategory.SIN_CATEGORIZAR,
    val tags:       List<String>      = emptyList(),
    val isFavorite:   Boolean           = false,
    val isTravelSafe: Boolean           = false,
    val isPinned:      Boolean       = false,
    val isHidden:      Boolean       = false,
    val isProtected: Boolean = false,
    val createdAt:  Long              = System.currentTimeMillis(),
    val updatedAt:  Long              = System.currentTimeMillis(),
    val isDeleted:  Boolean           = false,
    val deletedAt:  Long?             = null,
    val archivedAt: Long?             = null,
    val vaultId:       String        = VaultMetadata.PRIMARY_VAULT_ID,
    val inheritedFrom: String? = null,
)
