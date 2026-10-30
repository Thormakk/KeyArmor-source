package com.thormakk.keyarmor.domain.usecase

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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetVaultEntryCountUseCase @Inject constructor(
    private val vaultRepo:     VaultRepository,
    private val bankRepo:      BankAccountRepository,
    private val cardRepo:      CreditCardRepository,
    private val cryptoRepo:    CryptoWalletRepository,
    private val noteRepo:      SecureNoteRepository,
    private val customRepo:    CustomEntryRepository,
    private val twoFactorRepo: TwoFactorRepository,
    private val passkeyRepo:   PasskeyRepository,
    private val identityRepo:  IdentityDocumentRepository,
    private val vehicleRepo:   VehicleRepository,
) {
    suspend operator fun invoke(vaultId: String): Int =
        vaultRepo.countAllByVault(vaultId) +
        bankRepo.countAllByVault(vaultId) +
        cardRepo.countAllByVault(vaultId) +
        cryptoRepo.countAllByVault(vaultId) +
        noteRepo.countAllByVault(vaultId) +
        customRepo.countAllByVault(vaultId) +
        twoFactorRepo.countAllByVault(vaultId) +
        passkeyRepo.countAllByVault(vaultId) +
        identityRepo.countAllByVault(vaultId) +
        vehicleRepo.countAllByVault(vaultId)
}
