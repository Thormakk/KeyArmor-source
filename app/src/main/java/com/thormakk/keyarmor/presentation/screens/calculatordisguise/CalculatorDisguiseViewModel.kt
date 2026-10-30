package com.thormakk.keyarmor.presentation.screens.calculatordisguise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.presentation.UiText
import com.thormakk.keyarmor.presentation.screens.calculator.CalcOperator
import com.thormakk.keyarmor.presentation.screens.calculator.CalculatorEngine
import com.thormakk.keyarmor.presentation.screens.calculator.CalculatorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class CalculatorDisguiseStep {
    IDLE,
    RECORD_SEQUENCE,
    CONFIRM_SEQUENCE
}

data class CalculatorDisguiseUiState(
    val isEnabled            : Boolean               = false,
    val hasSequenceConfigured: Boolean                = false,
    val step                 : CalculatorDisguiseStep = CalculatorDisguiseStep.IDLE,
    val calcState            : CalculatorState        = CalculatorState(),
    val recordError          : UiText?                = null,
    val savedSuccess         : Boolean                = false
)

@HiltViewModel
class CalculatorDisguiseViewModel @Inject constructor(
    private val prefs: CalculatorDisguisePrefs,
    private val authRepository: AuthRepository,
    planPrefs: PlanPrefs
) : ViewModel() {

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    private val engine = CalculatorEngine()

    private var pendingSequence: String? = null

    private val _uiState = MutableStateFlow(
        CalculatorDisguiseUiState(
            isEnabled             = prefs.isEnabled(),
            hasSequenceConfigured = prefs.hasSequenceConfigured()
        )
    )
    val uiState: StateFlow<CalculatorDisguiseUiState> = _uiState

    fun onToggle(checked: Boolean) {
        if (checked) {
            if (prefs.hasSequenceConfigured()) {
                prefs.enable()
                _uiState.value = _uiState.value.copy(isEnabled = true)
            } else {
                onStartRecording()
            }
        } else {
            prefs.disable()
            _uiState.value = _uiState.value.copy(isEnabled = false)
        }
    }

    fun onChangeSequence() = onStartRecording()

    fun onStartRecording() {
        engine.clearAll()
        pendingSequence = null
        _uiState.value = _uiState.value.copy(
            step        = CalculatorDisguiseStep.RECORD_SEQUENCE,
            calcState   = engine.snapshot(),
            recordError = null
        )
    }

    fun onDigit(digit: Char) { engine.pressDigit(digit); refreshCalc() }
    fun onDecimal()          { engine.pressDecimal();    refreshCalc() }
    fun onOperator(op: CalcOperator) { engine.pressOperator(op); refreshCalc() }
    fun onPercent()          { engine.pressPercent();     refreshCalc() }
    fun onNegate()           { engine.pressNegate();      refreshCalc() }
    fun onClearEntry()       { engine.clearAll();         refreshCalc() }
    fun onBackspace()        { engine.pressBackspace();   refreshCalc() }

    private fun refreshCalc() {
        _uiState.value = _uiState.value.copy(calcState = engine.snapshot())
    }

    fun onConfirmStep() {
        val entered = engine.transcript
        if (entered.isBlank()) return

        when (_uiState.value.step) {
            CalculatorDisguiseStep.RECORD_SEQUENCE -> {
                pendingSequence = entered
                engine.clearAll()
                _uiState.value = _uiState.value.copy(
                    step        = CalculatorDisguiseStep.CONFIRM_SEQUENCE,
                    calcState   = engine.snapshot(),
                    recordError = null
                )
            }
            CalculatorDisguiseStep.CONFIRM_SEQUENCE -> {
                if (entered == pendingSequence) {
                    prefs.setSequenceAndEnable(entered)
                    pendingSequence = null
                    engine.clearAll()
                    _uiState.value = _uiState.value.copy(
                        step                  = CalculatorDisguiseStep.IDLE,
                        isEnabled             = true,
                        hasSequenceConfigured = true,
                        calcState             = engine.snapshot(),
                        recordError           = null,
                        savedSuccess          = true
                    )
                } else {
                    engine.clearAll()
                    _uiState.value = _uiState.value.copy(
                        calcState   = engine.snapshot(),
                        recordError = UiText.StringResource(R.string.calculator_disguise_mismatch)
                    )
                }
            }
            CalculatorDisguiseStep.IDLE -> Unit
        }
    }

    fun onCancelStep() {
        pendingSequence = null
        engine.clearAll()
        _uiState.value = _uiState.value.copy(
            step        = CalculatorDisguiseStep.IDLE,
            calcState   = engine.snapshot(),
            recordError = null
        )
    }

    fun onSavedSuccessConsumed() {
        _uiState.value = _uiState.value.copy(savedSuccess = false)
    }

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)
}
