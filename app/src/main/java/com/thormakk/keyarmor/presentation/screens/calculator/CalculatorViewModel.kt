package com.thormakk.keyarmor.presentation.screens.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val calculatorDisguisePrefs: CalculatorDisguisePrefs
) : ViewModel() {

    private val engine = CalculatorEngine()

    private val _uiState = MutableStateFlow(engine.snapshot())
    val uiState: StateFlow<CalculatorState> = _uiState

    private val _unlockEvent = Channel<Unit>(Channel.BUFFERED)
    val unlockEvent: Flow<Unit> = _unlockEvent.receiveAsFlow()

    fun onDigit(digit: Char) {
        engine.pressDigit(digit)
        refresh()
    }

    fun onDecimal() {
        engine.pressDecimal()
        refresh()
    }

    fun onOperator(op: CalcOperator) {
        engine.pressOperator(op)
        refresh()
    }

    fun onPercent() {
        engine.pressPercent()
        refresh()
    }

    fun onNegate() {
        engine.pressNegate()
        refresh()
    }

    fun onClear() {
        engine.clearAll()
        refresh()
    }

    fun onBackspace() {
        engine.pressBackspace()
        refresh()
    }

    fun onEquals() {
        val entered = engine.transcript
        if (calculatorDisguisePrefs.verifySequence(entered)) {
            engine.clearAll()
            _uiState.value = engine.snapshot()
            viewModelScope.launch { _unlockEvent.send(Unit) }
            return
        }
        _uiState.value = engine.pressEquals()
    }

    private fun refresh() {
        _uiState.value = engine.snapshot()
    }
}
