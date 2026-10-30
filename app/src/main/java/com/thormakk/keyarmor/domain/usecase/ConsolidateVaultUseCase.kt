package com.thormakk.keyarmor.domain.usecase

import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.domain.model.VaultMetadata
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsolidateVaultUseCase @Inject constructor(
    private val vaultRepo:      VaultRepository,
    private val bankRepo:       BankAccountRepository,
    private val cardRepo:       CreditCardRepository,
    private val cryptoRepo:     CryptoWalletRepository,
    private val noteRepo:       SecureNoteRepository,
    private val customRepo:     CustomEntryRepository,
    private val twoFactorRepo:  TwoFactorRepository,
    private val passkeyRepo:    PasskeyRepository,
    private val identityRepo:   IdentityDocumentRepository,
    private val vehicleRepo:    VehicleRepository,
    private val pinnedHubPrefs: PinnedHubPrefs,
) {
    suspend operator fun invoke(
        sourceVaultId: String,
        inheritedFrom: String? = null,
        sendToTrash: Boolean = false,
    ): Int {
        if (sourceVaultId == VaultMetadata.PRIMARY_VAULT_ID) return 0
        val target = VaultMetadata.PRIMARY_VAULT_ID

        vaultRepo.unpinAllByVault(sourceVaultId)
        bankRepo.unpinAllByVault(sourceVaultId)
        cardRepo.unpinAllByVault(sourceVaultId)
        cryptoRepo.unpinAllByVault(sourceVaultId)
        noteRepo.unpinAllByVault(sourceVaultId)
        customRepo.unpinAllByVault(sourceVaultId)
        twoFactorRepo.unpinAllByVault(sourceVaultId)
        passkeyRepo.unpinAllByVault(sourceVaultId)
        identityRepo.unpinAllByVault(sourceVaultId)
        vehicleRepo.unpinAllByVault(sourceVaultId)
        pinnedHubPrefs.unpinAllByVault(sourceVaultId)

        if (inheritedFrom != null) {
            vaultRepo.stampInheritedFrom(sourceVaultId, inheritedFrom)
            bankRepo.stampInheritedFrom(sourceVaultId, inheritedFrom)
            cardRepo.stampInheritedFrom(sourceVaultId, inheritedFrom)
            cryptoRepo.stampInheritedFrom(sourceVaultId, inheritedFrom)
            noteRepo.stampInheritedFrom(sourceVaultId, inheritedFrom)
            customRepo.stampInheritedFrom(sourceVaultId, inheritedFrom)
            twoFactorRepo.stampInheritedFrom(sourceVaultId, inheritedFrom)
            passkeyRepo.stampInheritedFrom(sourceVaultId, inheritedFrom)
            identityRepo.stampInheritedFrom(sourceVaultId, inheritedFrom)
            vehicleRepo.stampInheritedFrom(sourceVaultId, inheritedFrom)
        }

        var protegidas = 0
        if (sendToTrash) {
            protegidas += vaultRepo.trashAllByVault(sourceVaultId)
            protegidas += bankRepo.trashAllByVault(sourceVaultId)
            protegidas += cardRepo.trashAllByVault(sourceVaultId)
            protegidas += cryptoRepo.trashAllByVault(sourceVaultId)
            protegidas += noteRepo.trashAllByVault(sourceVaultId)
            protegidas += customRepo.trashAllByVault(sourceVaultId)
            protegidas += twoFactorRepo.trashAllByVault(sourceVaultId)
            protegidas += passkeyRepo.trashAllByVault(sourceVaultId)
            protegidas += identityRepo.trashAllByVault(sourceVaultId)
            protegidas += vehicleRepo.trashAllByVault(sourceVaultId)
        }

        vaultRepo.moveAllToVault(sourceVaultId, target)
        bankRepo.moveAllToVault(sourceVaultId, target)
        cardRepo.moveAllToVault(sourceVaultId, target)
        cryptoRepo.moveAllToVault(sourceVaultId, target)
        noteRepo.moveAllToVault(sourceVaultId, target)
        customRepo.moveAllToVault(sourceVaultId, target)
        twoFactorRepo.moveAllToVault(sourceVaultId, target)
        passkeyRepo.moveAllToVault(sourceVaultId, target)
        identityRepo.moveAllToVault(sourceVaultId, target)
        vehicleRepo.moveAllToVault(sourceVaultId, target)
        return protegidas
    }
}
