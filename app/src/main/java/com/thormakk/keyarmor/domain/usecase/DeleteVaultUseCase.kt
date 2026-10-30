package com.thormakk.keyarmor.domain.usecase

import androidx.room.withTransaction
import com.thormakk.keyarmor.data.database.KeyArmorDatabase
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteVaultUseCase @Inject constructor(
    private val db:                      KeyArmorDatabase,
    private val consolidateVaultUseCase: ConsolidateVaultUseCase,
    private val vaultMetadataRepository: VaultMetadataRepository,
) {
    suspend operator fun invoke(
        vaultId: String,
        inheritedFrom: String?,
        sendToTrash: Boolean,
    ) = db.withTransaction {
        val protegidas = consolidateVaultUseCase(vaultId, inheritedFrom, sendToTrash)
        vaultMetadataRepository.delete(vaultId)
        protegidas
    }
}
