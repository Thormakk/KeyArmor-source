package com.thormakk.keyarmor.domain.model

import java.util.UUID

enum class TotpAlgorithm { SHA1, SHA256, SHA512 }

data class RecoveryCode(
    val code: String,
    val used: Boolean = false
)

data class TwoFactorAccount(
    val id: String                    = UUID.randomUUID().toString(),
    val name: String                  = "",
    val issuer: String                = "",
    val accountLabel: String          = "",
    val secret: String                = "",
    val digits: Int                   = 6,
    val period: Int                   = 30,
    val algorithm: TotpAlgorithm      = TotpAlgorithm.SHA1,
    val recoveryCodes: List<RecoveryCode> = emptyList(),
    val url: String                   = "",
    val linkedPasswordId: String      = "",
    val notes: String                 = "",
    val category: EntryCategory       = EntryCategory.SIN_CATEGORIZAR,
    val tags: List<String>            = emptyList(),
    val isFavorite:   Boolean           = false,
    val isTravelSafe: Boolean           = false,
    val isPinned:     Boolean           = false,
    val isHidden:     Boolean           = false,
    val isProtected: Boolean = false,
    val createdAt: Long               = System.currentTimeMillis(),
    val updatedAt: Long               = System.currentTimeMillis(),
    val isDeleted: Boolean            = false,
    val deletedAt: Long?              = null,
    val archivedAt: Long?             = null,
    val vaultId:       String        = VaultMetadata.PRIMARY_VAULT_ID,
    val inheritedFrom: String? = null,
) {
    val hasTotp: Boolean get() = secret.isNotBlank()
}
