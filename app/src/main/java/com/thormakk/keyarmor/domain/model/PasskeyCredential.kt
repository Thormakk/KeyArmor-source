package com.thormakk.keyarmor.domain.model

@Suppress("ArrayInDataClass")
data class PasskeyCredential(
    val credentialId:    String,

    val rpId:            String,

    val rpName:          String,

    val userId:          String,

    val userName:        String,

    val userDisplayName: String,

    val privateKeyPkcs8: ByteArray,

    val publicKeyCose:   ByteArray,

    val signCount:       Long,

    val createdAt:  Long,
    val lastUsedAt: Long? = null,

    val notes: String = "",

    val urls: String = "",

    val linkedPasswordId: String = "",

    val category: String = "SIN_CATEGORIZAR",

    val tags: String = "",

    val customAccountLabel: String = "",

    val isTravelSafe: Boolean = false,

    val isPinned: Boolean = false,

    val isHidden: Boolean = false,

    val isProtected: Boolean = false,

    val isArchived: Boolean = false,
    val archivedAt: Long?   = null,

    val isDeleted: Boolean = false,
    val deletedAt: Long?   = null,
    val vaultId:    String  = VaultMetadata.PRIMARY_VAULT_ID,
    val inheritedFrom: String? = null
) {
    val displayName: String get() = rpName.ifBlank { rpId }

    val accountLabel: String get() = userDisplayName.ifBlank { userName }.ifBlank { customAccountLabel }

    val isAccountLabelEditable: Boolean get() = userDisplayName.isBlank() && userName.isBlank()

    val tagList: List<String> get() = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}