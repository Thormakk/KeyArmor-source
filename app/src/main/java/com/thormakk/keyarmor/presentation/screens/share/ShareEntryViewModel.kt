package com.thormakk.keyarmor.presentation.screens.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.repository.AttachmentRepository
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.data.share.ShareSerializer
import com.thormakk.keyarmor.data.share.ShareSerializer.EntryType
import com.thormakk.keyarmor.security.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ShareExpiry(val durationMillis: Long) {
    ONE_HOUR     (60L * 60 * 1000),
    ONE_DAY      (24L * 60 * 60 * 1000),
    SEVEN_DAYS   (7L  * 24 * 60 * 60 * 1000),
    FOURTEEN_DAYS(14L * 24 * 60 * 60 * 1000),
    THIRTY_DAYS  (30L * 24 * 60 * 60 * 1000)
}

data class ShareEntryUiState(
    val entryName:      String     = "",
    val hasAttachments: Boolean    = false,
    val password:       String     = "",
    val passwordConfirm: String    = "",
    val isPasswordVisible:  Boolean = false,
    val isConfirmVisible:   Boolean = false,
    val encryptedText:  String     = "",
    val qrBitmap:       Bitmap?    = null,
    val isGenerating:   Boolean    = false,
    val isGenerated:    Boolean    = false,
    val errorMessage:   String?    = null,
    val selectedExpiry: ShareExpiry = ShareExpiry.ONE_DAY
) {
    val passwordsMatch: Boolean get() =
        password.isNotEmpty() && password == passwordConfirm

    val canGenerate: Boolean get() =
        password.length >= 8 && passwordsMatch && !isGenerating
}

sealed class ShareEntryEvent {
    data class Error(val message: String) : ShareEntryEvent()
}

@HiltViewModel
class ShareEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vaultRepo:        VaultRepository,
    private val twoFactorRepo:    TwoFactorRepository,
    private val creditCardRepo:   CreditCardRepository,
    private val bankAccountRepo:  BankAccountRepository,
    private val cryptoRepo:       CryptoWalletRepository,
    private val secureNoteRepo:   SecureNoteRepository,
    private val customEntryRepo:  CustomEntryRepository,
    private val vehicleRepo:      VehicleRepository,
    private val attachmentRepo:   AttachmentRepository,
    private val crypto:           CryptoManager,
    @ApplicationContext private val appContextInyectado: Context
) : ViewModel() {

    private val appContext: Context get() = contextoDelIdioma(appContextInyectado)

    private val typeArg: String = savedStateHandle["type"] ?: ""
    private val idArg:   String = savedStateHandle["id"]   ?: ""

    private val _uiState = MutableStateFlow(ShareEntryUiState())
    val uiState: StateFlow<ShareEntryUiState> = _uiState.asStateFlow()

    private val _events = Channel<ShareEntryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadedShared: ShareSerializer.SharedEntry? = null

    init {
        loadEntry()
        val generated = generateStrongPassword()
        _uiState.update { it.copy(password = generated, passwordConfirm = generated) }
    }

    fun regeneratePassword() {
        val generated = generateStrongPassword()
        _uiState.update {
            it.copy(
                password = generated, passwordConfirm = generated,
                isGenerated = false, encryptedText = "", qrBitmap = null, errorMessage = null
            )
        }
    }

    private fun generateStrongPassword(length: Int = 20): String {
        val upper   = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        val lower   = "abcdefghijkmnpqrstuvwxyz"
        val digits  = "23456789"
        val symbols = "!@#\$%^&*-_=+?"
        val all = upper + lower + digits + symbols
        val rnd = java.security.SecureRandom()
        val chars = mutableListOf(
            upper[rnd.nextInt(upper.length)],
            lower[rnd.nextInt(lower.length)],
            digits[rnd.nextInt(digits.length)],
            symbols[rnd.nextInt(symbols.length)]
        )
        repeat((length - chars.size).coerceAtLeast(0)) { chars.add(all[rnd.nextInt(all.length)]) }
        for (i in chars.indices.reversed()) {
            val j = rnd.nextInt(i + 1)
            val tmp = chars[i]; chars[i] = chars[j]; chars[j] = tmp
        }
        return chars.joinToString("")
    }

    private fun loadEntry() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val type = EntryType.valueOf(typeArg)
                val (shared, name) = when (type) {
                    EntryType.PASSWORD -> {
                        val e = vaultRepo.getEntry(idArg) ?: error("Password entry $idArg not found")
                        ShareSerializer.SharedEntry(type, e) to e.title
                    }
                    EntryType.TWO_FACTOR -> {
                        val e = twoFactorRepo.getAccount(idArg) ?: error("2FA account $idArg not found")
                        ShareSerializer.SharedEntry(type, e) to e.name
                    }
                    EntryType.CREDIT_CARD -> {
                        val e = creditCardRepo.getCard(idArg) ?: error("Credit card $idArg not found")
                        ShareSerializer.SharedEntry(type, e) to e.issuer.ifEmpty { e.brand }
                    }
                    EntryType.BANK_ACCOUNT -> {
                        val e = bankAccountRepo.getAccount(idArg) ?: error("Bank account $idArg not found")
                        ShareSerializer.SharedEntry(type, e) to e.bankName
                    }
                    EntryType.CRYPTO_WALLET -> {
                        val e = cryptoRepo.getWallet(idArg) ?: error("Crypto wallet $idArg not found")
                        ShareSerializer.SharedEntry(type, e) to e.asset.ifEmpty { e.network }
                    }
                    EntryType.SECURE_NOTE -> {
                        val e = secureNoteRepo.getNote(idArg) ?: error("Secure note $idArg not found")
                        ShareSerializer.SharedEntry(type, e) to e.title
                    }
                    EntryType.CUSTOM_ENTRY -> {
                        val e = customEntryRepo.getEntry(idArg) ?: error("Custom entry $idArg not found")
                        ShareSerializer.SharedEntry(type, e) to e.title
                    }
                    EntryType.VEHICLE -> {
                        val e = vehicleRepo.getVehicle(idArg) ?: error("Vehicle $idArg not found")
                        ShareSerializer.SharedEntry(type, e) to listOf(e.make, e.model).filter { it.isNotEmpty() }.joinToString(" ")
                    }
                }
                loadedShared = shared
                val hasAttachments = attachmentRepo.countForEntry(idArg) > 0
                _uiState.update { it.copy(entryName = name, hasAttachments = hasAttachments) }
            }.onFailure { err ->
                _events.send(ShareEntryEvent.Error(appContext.getString(R.string.share_error_loading_entry)))
            }
        }
    }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, isGenerated = false, encryptedText = "", qrBitmap = null, errorMessage = null) }

    fun onPasswordConfirmChange(value: String) =
        _uiState.update { it.copy(passwordConfirm = value, isGenerated = false, encryptedText = "", qrBitmap = null, errorMessage = null) }

    fun togglePasswordVisible()        = _uiState.update { it.copy(isPasswordVisible  = !it.isPasswordVisible) }
    fun toggleConfirmVisible()         = _uiState.update { it.copy(isConfirmVisible   = !it.isConfirmVisible) }

    fun onExpiryChange(expiry: ShareExpiry) =
        _uiState.update { it.copy(selectedExpiry = expiry, isGenerated = false, encryptedText = "", qrBitmap = null, errorMessage = null) }

    fun onGenerate() {
        val shared = loadedShared ?: return
        val password = _uiState.value.password
        if (password.length < 8) return

        _uiState.update { it.copy(isGenerating = true, errorMessage = null) }

        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                val expiresAt = System.currentTimeMillis() + _uiState.value.selectedExpiry.durationMillis
                val token = ShareSerializer.encrypt(shared, password, crypto, expiresAt)
                val bitmap = generateQr(token)
                _uiState.update {
                    it.copy(
                        isGenerating  = false,
                        isGenerated   = true,
                        encryptedText = token,
                        qrBitmap      = bitmap,
                        errorMessage  = null
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(isGenerating = false, errorMessage = err.message ?: appContext.getString(R.string.error_unknown)) }
            }
        }
    }

    private fun generateQr(content: String): Bitmap {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val writer = QRCodeWriter()
        val matrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)
        val w = matrix.width
        val h = matrix.height
        val bmp = createBitmap(w, h, Bitmap.Config.RGB_565)
        for (x in 0 until w) {
            for (y in 0 until h) {
                bmp[x, y] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return bmp
    }
}
