package com.thormakk.keyarmor.presentation.screens.panicpin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.decoy.DecoyVaultManager
import com.thormakk.keyarmor.data.decoy.HideVaultManager
import com.thormakk.keyarmor.data.prefs.PanicMode
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PanicPinStep {
    IDLE,
    ENTER_PIN,
    CONFIRM_PIN
}

data class PanicPinUiState(
    val isEnabled        : Boolean      = false,
    val step             : PanicPinStep = PanicPinStep.IDLE,
    val panicMode        : PanicMode    = PanicMode.HIDE,
    val pin              : String       = "",
    val confirmPin       : String       = "",
    val pinError         : String?      = null,
    val showDisableDialog: Boolean      = false,
    val savedSuccess     : Boolean      = false,
    val isGeneratingDecoyContent: Boolean = false
)

const val PANIC_PIN_LENGTH = 6

@HiltViewModel
class PanicPinViewModel @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val panicPinPrefs: PanicPinPrefs,
    private val decoyVaultManager: DecoyVaultManager,
    private val hideVaultManager: HideVaultManager,
    planPrefs: PlanPrefs
) : ViewModel() {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    private val _uiState = MutableStateFlow(
        PanicPinUiState(
            isEnabled = panicPinPrefs.isPanicPinEnabled(),
            panicMode = panicPinPrefs.getPanicMode()
        )
    )
    val uiState: StateFlow<PanicPinUiState> = _uiState

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    fun onModeChange(mode: PanicMode) {
        panicPinPrefs.setPanicMode(mode)
        _uiState.value = _uiState.value.copy(panicMode = mode)
    }

    fun onStartSetup() {
        _uiState.value = _uiState.value.copy(
            step     = PanicPinStep.ENTER_PIN,
            pin      = "",
            pinError = null
        )
    }

    fun onDigitEnter(digit: Char) {
        val state = _uiState.value
        when (state.step) {
            PanicPinStep.ENTER_PIN -> {
                if (state.pin.length >= PANIC_PIN_LENGTH) return
                val newPin = state.pin + digit
                _uiState.value = state.copy(pin = newPin, pinError = null)
                if (newPin.length == PANIC_PIN_LENGTH) {
                    _uiState.value = _uiState.value.copy(
                        step       = PanicPinStep.CONFIRM_PIN,
                        confirmPin = "",
                        pinError   = null
                    )
                }
            }
            PanicPinStep.CONFIRM_PIN -> {
                if (state.confirmPin.length >= PANIC_PIN_LENGTH) return
                val newConfirm = state.confirmPin + digit
                _uiState.value = state.copy(confirmPin = newConfirm, pinError = null)
                if (newConfirm.length == PANIC_PIN_LENGTH) {
                    if (state.pin == newConfirm) {
                        val confirmedPin = state.pin
                        _uiState.value = _uiState.value.copy(isGeneratingDecoyContent = true)
                        viewModelScope.launch {
                            panicPinPrefs.setPanicPin(confirmedPin)
                            _uiState.value = _uiState.value.copy(
                                isEnabled    = true,
                                step         = PanicPinStep.IDLE,
                                pin          = "",
                                confirmPin   = "",
                                pinError     = null,
                                savedSuccess = true
                            )
                            try {
                                decoyVaultManager.regenerateDecoyContent(confirmedPin)
                            } finally {
                                _uiState.value = _uiState.value.copy(isGeneratingDecoyContent = false)
                            }
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            confirmPin = "",
                            pinError   = context.getString(R.string.panic_pin_mismatch)
                        )
                    }
                }
            }
            PanicPinStep.IDLE -> Unit
        }
    }

    fun onBackspace() {
        val state = _uiState.value
        when (state.step) {
            PanicPinStep.ENTER_PIN   ->
                _uiState.value = state.copy(pin        = state.pin.dropLast(1),        pinError = null)
            PanicPinStep.CONFIRM_PIN ->
                _uiState.value = state.copy(confirmPin = state.confirmPin.dropLast(1), pinError = null)
            PanicPinStep.IDLE -> Unit
        }
    }

    fun onCancelSetup() {
        _uiState.value = _uiState.value.copy(
            step       = PanicPinStep.IDLE,
            pin        = "",
            confirmPin = "",
            pinError   = null
        )
    }

    fun onRestartSetup() {
        _uiState.value = _uiState.value.copy(
            step       = PanicPinStep.ENTER_PIN,
            pin        = "",
            confirmPin = "",
            pinError   = null
        )
    }

    fun onRequestDisable() {
        _uiState.value = _uiState.value.copy(showDisableDialog = true)
    }

    fun onConfirmDisable() {
        panicPinPrefs.clearPanicPin()
        _uiState.value = _uiState.value.copy(
            isEnabled         = false,
            showDisableDialog = false,
            step              = PanicPinStep.IDLE
        )
        viewModelScope.launch {
            decoyVaultManager.purgeDecoyContent()
            hideVaultManager.purgeHideContent()
        }
    }

    fun onDismissDisable() {
        _uiState.value = _uiState.value.copy(showDisableDialog = false)
    }

    fun onSavedSuccessConsumed() {
        _uiState.value = _uiState.value.copy(savedSuccess = false)
    }
}
