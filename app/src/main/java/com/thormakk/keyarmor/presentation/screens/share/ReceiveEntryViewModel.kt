package com.thormakk.keyarmor.presentation.screens.share

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.data.share.ShareExpiredException
import com.thormakk.keyarmor.data.share.ShareSerializer
import com.thormakk.keyarmor.data.share.ShareSerializer.EntryType
import com.thormakk.keyarmor.domain.model.BankAccount
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.CustomEntry
import com.thormakk.keyarmor.domain.model.SecureNote
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.Vehicle
import com.thormakk.keyarmor.security.AutoLockManager
import com.thormakk.keyarmor.security.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ReceiveEntryUiState(
    val pastedText:        String    = "",
    val password:          String    = "",
    val isPasswordVisible: Boolean   = false,
    val isDecrypting:      Boolean   = false,
    val previewType:       EntryType? = null,
    val previewName:       String     = "",
    val showPreview:       Boolean    = false,
    val isSaving:          Boolean    = false,
    val errorMessage:      String?    = null,
) {
    val canDecrypt: Boolean get() =
        pastedText.isNotBlank() && password.isNotEmpty() && !isDecrypting
}

sealed class ReceiveEntryEvent {
    object SavedSuccessfully       : ReceiveEntryEvent()
    data class Error(val msg: String) : ReceiveEntryEvent()
}

@HiltViewModel
class ReceiveEntryViewModel @Inject constructor(
    private val vaultRepo:       VaultRepository,
    private val twoFactorRepo:   TwoFactorRepository,
    private val creditCardRepo:  CreditCardRepository,
    private val bankAccountRepo: BankAccountRepository,
    private val cryptoRepo:      CryptoWalletRepository,
    private val secureNoteRepo:  SecureNoteRepository,
    private val customEntryRepo: CustomEntryRepository,
    private val vehicleRepo:     VehicleRepository,
    private val crypto:          CryptoManager,
    private val autoLockManager: AutoLockManager,
    @ApplicationContext private val appContextInyectado: Context
) : ViewModel() {

    private val appContext: Context get() = contextoDelIdioma(appContextInyectado)

    private val _uiState = MutableStateFlow(ReceiveEntryUiState())
    val uiState: StateFlow<ReceiveEntryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReceiveEntryEvent>()
    val events: SharedFlow<ReceiveEntryEvent> = _events.asSharedFlow()

    private var pending: ShareSerializer.SharedEntry? = null

    fun onPastedTextChange(value: String) {
        _uiState.update { it.copy(pastedText = value, showPreview = false, errorMessage = null) }
        pending = null
    }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, showPreview = false, errorMessage = null) }

    fun togglePasswordVisible() =
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun prepareForQrScan() = autoLockManager.skipLockOnNextForeground()

    fun onQrScanned(raw: String) {
        _uiState.update {
            it.copy(pastedText = raw, showPreview = false, errorMessage = null)
        }
        pending = null
    }

    fun onDecrypt() {
        val state = _uiState.value
        if (!state.canDecrypt) return

        _uiState.update { it.copy(isDecrypting = true, errorMessage = null) }

        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                ShareSerializer.decrypt(state.pastedText.trim(), state.password, crypto)
            }.onSuccess { shared ->
                pending = shared
                val name = entryName(shared)
                _uiState.update {
                    it.copy(
                        isDecrypting = false,
                        previewType  = shared.type,
                        previewName  = name,
                        showPreview  = true,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                val msg = if (err is ShareExpiredException)
                    appContext.getString(R.string.share_error_expired)
                else
                    err.message ?: appContext.getString(R.string.error_unknown)
                _uiState.update {
                    it.copy(
                        isDecrypting = false,
                        errorMessage = msg
                    )
                }
            }
        }
    }

    fun onSave() {
        val shared = pending ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                saveWithNewId(shared)
            }.onSuccess {
                _events.emit(ReceiveEntryEvent.SavedSuccessfully)
            }.onFailure { err ->
                _uiState.update { it.copy(isSaving = false, errorMessage = err.message ?: appContext.getString(R.string.error_unknown)) }
                _events.emit(ReceiveEntryEvent.Error(err.message ?: appContext.getString(R.string.share_error_saving)))
            }
        }
    }


    private suspend fun saveWithNewId(shared: ShareSerializer.SharedEntry) {
        val newId = UUID.randomUUID().toString()
        when (shared.type) {
            EntryType.PASSWORD ->
                vaultRepo.saveEntry((shared.entry as VaultEntry).copy(id = newId, isProtected = false))
            EntryType.TWO_FACTOR ->
                twoFactorRepo.saveAccount((shared.entry as TwoFactorAccount).copy(id = newId, isProtected = false))
            EntryType.CREDIT_CARD ->
                creditCardRepo.saveCard((shared.entry as CreditCard).copy(id = newId, isProtected = false))
            EntryType.BANK_ACCOUNT ->
                bankAccountRepo.saveAccount((shared.entry as BankAccount).copy(id = newId, isProtected = false))
            EntryType.CRYPTO_WALLET ->
                cryptoRepo.saveWallet((shared.entry as CryptoWallet).copy(id = newId, isProtected = false))
            EntryType.SECURE_NOTE ->
                secureNoteRepo.saveNote((shared.entry as SecureNote).copy(id = newId, isProtected = false))
            EntryType.CUSTOM_ENTRY ->
                customEntryRepo.saveEntry((shared.entry as CustomEntry).copy(id = newId, isProtected = false))
            EntryType.VEHICLE ->
                vehicleRepo.saveVehicle((shared.entry as Vehicle).copy(id = newId, isProtected = false))
        }
    }

    private fun entryName(shared: ShareSerializer.SharedEntry): String = when (shared.type) {
        EntryType.PASSWORD      -> (shared.entry as VaultEntry).title
        EntryType.TWO_FACTOR    -> (shared.entry as TwoFactorAccount).name
        EntryType.CREDIT_CARD   -> (shared.entry as CreditCard).issuer.ifEmpty { shared.entry.brand }
        EntryType.BANK_ACCOUNT  -> (shared.entry as BankAccount).bankName
        EntryType.CRYPTO_WALLET -> (shared.entry as CryptoWallet).asset.ifEmpty { shared.entry.network }
        EntryType.SECURE_NOTE   -> (shared.entry as SecureNote).title
        EntryType.CUSTOM_ENTRY  -> (shared.entry as CustomEntry).title
        EntryType.VEHICLE       -> (shared.entry as Vehicle).let { listOf(it.make, it.model).filter { s -> s.isNotEmpty() }.joinToString(" ") }
    }
}
