package com.thormakk.keyarmor.domain.model

import com.thormakk.keyarmor.domain.model.VaultMetadata.Companion.PRIMARY_VAULT_ID


data class VaultMetadata(
    val id:            String,
    val name:          String,
    val isInherited:   Boolean = false,
    val isPrivate:     Boolean = false,
    val ownerEmail:    String  = "",
    val ownerSub:      String  = "",
    val sortOrder:     Int     = 0,
    val createdAt:     Long    = System.currentTimeMillis()
) {
    companion object {
        const val PRIMARY_VAULT_ID = "main"

        const val PRIVATE_VAULT_ID = "private"

        const val PRIVATE_SORT_ORDER = Int.MAX_VALUE

        const val INHERITED_PREFIX = "inherited_"

        fun inheritedId(ownerEmail: String) = "$INHERITED_PREFIX$ownerEmail"

        const val DECOY_MAIN_VAULT_ID    = "decoy_main"
        const val DECOY_PRIVATE_VAULT_ID = "decoy_private"

        val DECOY_VAULT_IDS = setOf(DECOY_MAIN_VAULT_ID, DECOY_PRIVATE_VAULT_ID)

        const val HIDE_MAIN_VAULT_ID    = "hide_main"
        const val HIDE_PRIVATE_VAULT_ID = "hide_private"

        val HIDE_VAULT_IDS = setOf(HIDE_MAIN_VAULT_ID, HIDE_PRIVATE_VAULT_ID)

        val COERCION_VAULT_IDS = DECOY_VAULT_IDS + HIDE_VAULT_IDS
    }
}
