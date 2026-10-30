package com.thormakk.keyarmor.domain.usecase

import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetTotalActiveEntryCountUseCase @Inject constructor(
    private val vaultRepo:    VaultRepository,
    private val bankRepo:     BankAccountRepository,
    private val cardRepo:     CreditCardRepository,
    private val cryptoRepo:   CryptoWalletRepository,
    private val customRepo:   CustomEntryRepository,
    private val noteRepo:     SecureNoteRepository,
    private val twoFaRepo:    TwoFactorRepository,
    private val identityRepo: IdentityDocumentRepository,
    private val vehicleRepo:  VehicleRepository
) {
    suspend operator fun invoke(): Int =
        vaultRepo.countAllEntries() +
        bankRepo.countAllAccounts() +
        cardRepo.countAllCards() +
        cryptoRepo.countAllWallets() +
        customRepo.countAllEntries() +
        noteRepo.countAllNotes() +
        twoFaRepo.countAll() +
        identityRepo.countAllDocuments() +
        vehicleRepo.countAllVehicles()

}
