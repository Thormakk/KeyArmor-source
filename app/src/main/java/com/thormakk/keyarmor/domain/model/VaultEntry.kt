package com.thormakk.keyarmor.domain.model

import androidx.annotation.StringRes
import com.thormakk.keyarmor.R

data class VaultEntry(
    val id: String,
    val title: String,
    val username: String,
    val email: String,
    val password: String,
    val urls: List<String> = emptyList(),
    val notes: String,
    val category: EntryCategory,
    val isFavorite:   Boolean,
    val isTravelSafe: Boolean = false,
    val isPinned:     Boolean = false,
    val isHidden:     Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean,
    val deletedAt: Long?,
    val archivedAt: Long?   = null,
    val tags: List<String> = emptyList(),
    val expiryDate: Long? = null,
    val vaultId: String = VaultMetadata.PRIMARY_VAULT_ID,
    val inheritedFrom: String? = null,
    val passwordChangedAt: Long? = null,
    val fieldLabel: String = "",
    val isProtected: Boolean = false,
) {
    val url: String get() = urls.firstOrNull() ?: ""
}

enum class EntryCategory(
    val label: String,
    @StringRes val labelRes: Int
) {
    SIN_CATEGORIZAR   ("Sin categorizar",     R.string.category_uncategorized),
    BANCOS_BILLETERAS ("Bancos y Billeteras", R.string.category_banks        ),
    COMPRAS_WEB       ("Compras Web",         R.string.category_shopping     ),
    REDES_SOCIALES    ("Redes sociales",      R.string.category_social       );

    companion object {
        fun fromString(value: String): EntryCategory =
            entries.firstOrNull { it.name == value } ?: SIN_CATEGORIZAR
    }
}
