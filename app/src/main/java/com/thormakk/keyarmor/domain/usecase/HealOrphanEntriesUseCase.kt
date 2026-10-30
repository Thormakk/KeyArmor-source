package com.thormakk.keyarmor.domain.usecase

import androidx.room.withTransaction
import com.thormakk.keyarmor.data.database.KeyArmorDatabase
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.domain.model.VaultMetadata
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealOrphanEntriesUseCase @Inject constructor(
    private val db:                      KeyArmorDatabase,
    private val vaultMetadataRepository: VaultMetadataRepository,
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
) {
    suspend operator fun invoke(): Int {
        val valid = vaultMetadataRepository.getAllIdsRaw().toSet() + VaultMetadata.COERCION_VAULT_IDS
        if (VaultMetadata.PRIMARY_VAULT_ID !in valid) return 0

        val validList = valid.toList()
        val target    = VaultMetadata.PRIMARY_VAULT_ID

        return db.withTransaction {
            vaultRepo.reassignOrphansToVault(validList, target) +
            bankRepo.reassignOrphansToVault(validList, target) +
            cardRepo.reassignOrphansToVault(validList, target) +
            cryptoRepo.reassignOrphansToVault(validList, target) +
            noteRepo.reassignOrphansToVault(validList, target) +
            customRepo.reassignOrphansToVault(validList, target) +
            twoFactorRepo.reassignOrphansToVault(validList, target) +
            passkeyRepo.reassignOrphansToVault(validList, target) +
            identityRepo.reassignOrphansToVault(validList, target) +
            vehicleRepo.reassignOrphansToVault(validList, target)
        }
    }
}
