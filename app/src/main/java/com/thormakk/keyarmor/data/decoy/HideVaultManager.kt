package com.thormakk.keyarmor.data.decoy

import android.content.Context
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.security.CoercionPepperManager
import com.thormakk.keyarmor.security.CoercionPepperUnavailableException
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HideVaultManager @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val panicPinPrefs:           PanicPinPrefs,
    private val coercionPepper:          CoercionPepperManager,
    private val crypto:                  CryptoManager,
    private val vaultRepository:         VaultRepository,
    private val bankAccountRepository:   BankAccountRepository,
    private val creditCardRepository:    CreditCardRepository,
    private val vaultMetadataRepository: VaultMetadataRepository
) {

    private val context: Context get() = contextoDelIdioma(contextInyectado)
    suspend fun deriveOrCreateHideKey(pin: String): SecretKeySpec = withContext(Dispatchers.Default) {
        val pepperedPin = coercionPepper.pepperedOrNull(pin)
            ?: throw CoercionPepperUnavailableException("pepper ausente al derivar la clave Hide")
        val isFirstTime = !panicPinPrefs.hasHideKeyMaterial()
        val params = panicPinPrefs.getHideKeyParams() ?: panicPinPrefs.generateHideKeyMaterial()
        val masterSecret = crypto.deriveKeyArgon2id(
            pepperedPin, params.salt, params.memoryKb, params.iterations, params.parallelism
        )
        val key = crypto.deriveEncryptionKey(masterSecret)

        if (isFirstTime) {
            vaultMetadataRepository.saveWithKey(
                VaultMetadata(
                    id        = VaultMetadata.HIDE_MAIN_VAULT_ID,
                    name      = context.getString(R.string.vault_name_primary),
                    isPrivate = false,
                    sortOrder = 0
                ),
                key
            )
            vaultMetadataRepository.saveWithKey(
                VaultMetadata(
                    id        = VaultMetadata.HIDE_PRIVATE_VAULT_ID,
                    name      = context.getString(R.string.vault_name_private),
                    isPrivate = true,
                    sortOrder = VaultMetadata.PRIVATE_SORT_ORDER
                ),
                key
            )
        }

        key
    }

    suspend fun purgeHideContent() {
        if (!panicPinPrefs.hasHideKeyMaterial()) return
        vaultRepository.deleteAllByVault(VaultMetadata.HIDE_MAIN_VAULT_ID)
        vaultRepository.deleteAllByVault(VaultMetadata.HIDE_PRIVATE_VAULT_ID)
        bankAccountRepository.deleteAllByVault(VaultMetadata.HIDE_MAIN_VAULT_ID)
        creditCardRepository.deleteAllByVault(VaultMetadata.HIDE_MAIN_VAULT_ID)
        vaultMetadataRepository.delete(VaultMetadata.HIDE_MAIN_VAULT_ID)
        vaultMetadataRepository.delete(VaultMetadata.HIDE_PRIVATE_VAULT_ID)
        panicPinPrefs.clearHideKeyMaterial()
    }
}
