package com.thormakk.keyarmor.presentation.screens.calculator

import java.util.Locale

enum class CalcOperator(val symbol: Char) {
    ADD('+'), SUBTRACT('−'), MULTIPLY('×'), DIVIDE('÷')
}

data class CalculatorState(
    val display: String = "0",
    val expressionPreview: String = "",
    val isError: Boolean = false
)

class CalculatorEngine {

    private var accumulator: Double?     = null
    private var pendingOp:   CalcOperator? = null
    private var currentInput: String     = "0"

    private var startFresh: Boolean = true

    private var currentInputEditable: Boolean = true

    private val transcriptBuilder = StringBuilder()

    val transcript: String get() = transcriptBuilder.toString()

    fun snapshot(): CalculatorState {
        val preview = if (pendingOp != null) {
            val opPart = "${formatNumber(accumulator ?: 0.0)} ${pendingOp!!.symbol}"
            if (!startFresh) "$opPart $currentInput" else opPart
        } else {
            ""
        }
        return CalculatorState(display = currentInput, expressionPreview = preview)
    }

    fun pressDigit(digit: Char) {
        if (startFresh) {
            currentInput = if (digit == '0') "0" else digit.toString()
            startFresh = false
        } else if (currentInput == "0") {
            currentInput = digit.toString()
        } else if (currentInput.count { it.isDigit() } < MAX_DIGITS) {
            currentInput += digit
        }
        currentInputEditable = true
        transcriptBuilder.append(digit)
    }

    fun pressDecimal() {
        if (startFresh) {
            currentInput = "0."
            startFresh = false
        } else if (!currentInput.contains('.')) {
            currentInput += "."
        } else {
            return
        }
        currentInputEditable = true
        transcriptBuilder.append('.')
    }

    fun pressOperator(op: CalcOperator) {
        val value = currentInput.toDoubleOrNull() ?: 0.0
        if (pendingOp != null && !startFresh) {
            accumulator = applyOp(pendingOp!!, accumulator ?: 0.0, value)
            currentInput = formatNumber(accumulator!!)
        } else if (accumulator == null) {
            accumulator = value
        }
        pendingOp = op
        startFresh = true
        currentInputEditable = false
        transcriptBuilder.append(op.symbol)
    }

    fun pressPercent() {
        val value = (currentInput.toDoubleOrNull() ?: 0.0) / 100.0
        currentInput = formatNumber(value)
        currentInputEditable = false
        transcriptBuilder.append('%')
    }

    fun pressNegate() {
        if (currentInput == "0") return
        currentInput = if (currentInput.startsWith("-")) currentInput.drop(1) else "-$currentInput"
        currentInputEditable = false
        transcriptBuilder.append('±')
    }

    fun pressBackspace() {
        if (!currentInputEditable) return
        currentInput = if (currentInput.length <= 1 || (currentInput.length == 2 && currentInput.startsWith("-"))) {
            "0"
        } else {
            currentInput.dropLast(1)
        }
        if (transcriptBuilder.isNotEmpty()) transcriptBuilder.deleteCharAt(transcriptBuilder.length - 1)
        if (currentInput == "0") startFresh = true
    }

    fun pressEquals(): CalculatorState {
        val value  = currentInput.toDoubleOrNull() ?: 0.0
        val op     = pendingOp
        val isDivByZero = op == CalcOperator.DIVIDE && value == 0.0
        val result = if (op != null) applyOp(op, accumulator ?: 0.0, value) else value
        val equation = if (op != null) {
            "${formatNumber(accumulator ?: 0.0)} ${op.symbol} ${formatNumber(value)} ="
        } else {
            ""
        }
        clearAll()
        return if (isDivByZero) {
            CalculatorState(display = ERROR_TEXT, isError = true)
        } else {
            accumulator  = result
            currentInput = formatNumber(result)
            startFresh   = true
            CalculatorState(display = currentInput, expressionPreview = equation)
        }
    }

    fun clearAll() {
        accumulator          = null
        pendingOp            = null
        currentInput         = "0"
        startFresh           = true
        currentInputEditable = true
        transcriptBuilder.clear()
    }

    private fun applyOp(op: CalcOperator, a: Double, b: Double): Double = when (op) {
        CalcOperator.ADD      -> a + b
        CalcOperator.SUBTRACT -> a - b
        CalcOperator.MULTIPLY -> a * b
        CalcOperator.DIVIDE   -> if (b == 0.0) 0.0 else a / b
    }

    companion object {
        private const val MAX_DIGITS = 15
        const val ERROR_TEXT = "Error"

        fun formatNumber(value: Double): String {
            if (value.isNaN() || value.isInfinite()) return ERROR_TEXT
            return if (value == value.toLong().toDouble() && kotlin.math.abs(value) < 1e15) {
                value.toLong().toString()
            } else {
                String.format(Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')
            }
        }
    }
}
