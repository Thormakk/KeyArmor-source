package com.thormakk.keyarmor.presentation.screens.generator

import androidx.lifecycle.ViewModel
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.SecureRandom
import javax.inject.Inject

enum class GeneratorMode {
    STANDARD,
    PRONOUNCEABLE,
    PASSPHRASE
}

data class GeneratorUiState(
    val password:         String        = "",
    val mode:             GeneratorMode = GeneratorMode.STANDARD,
    val length:           Int           = 16,
    val useUpper:         Boolean       = true,
    val useLower:         Boolean       = true,
    val useDigits:        Boolean       = true,
    val useSymbols:       Boolean       = true,
    val excludeAmbiguous: Boolean       = false,
    val countUpper:       Int           = 1,
    val countDigits:      Int           = 1,
    val countSymbols:     Int           = 1,
    val upperCount:       Int           = 1,
    val wordCount:        Int           = 4,
    val separator:        String        = "-",
    val capitalize:       Boolean       = false
) {
    val strength: Int get() = PasswordStrengthCalculator.calculate(password).coerceAtLeast(0)

    fun maxCountUpper(): Int {
        val other = (if (useDigits) countDigits else 0) + (if (useSymbols) countSymbols else 0)
        return (length - other).coerceAtLeast(1)
    }
    fun maxCountDigits(): Int {
        val other = (if (useUpper) countUpper else 0) + (if (useSymbols) countSymbols else 0)
        return (length - other).coerceAtLeast(1)
    }
    fun maxCountSymbols(): Int {
        val other = (if (useUpper) countUpper else 0) + (if (useDigits) countDigits else 0)
        return (length - other).coerceAtLeast(1)
    }
}

@HiltViewModel
class PasswordGeneratorViewModel @Inject constructor() : ViewModel() {

    private val random = SecureRandom()

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState

    init { regenerate() }

    fun onModeChange(mode: GeneratorMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
        regenerate()
    }

    fun onLengthChange(v: Int) {
        val s        = _uiState.value
        val maxUpper = (v / 2).coerceAtLeast(1)
        _uiState.value = s.copy(
            length       = v,
            upperCount   = s.upperCount.coerceAtMost(maxUpper),
            countUpper   = s.countUpper.coerceIn(1, v / 2),
            countDigits  = s.countDigits.coerceIn(1, v / 2),
            countSymbols = s.countSymbols.coerceIn(1, v / 2)
        )
        regenerate()
    }

    fun onUpperCountChange(v: Int) {
        val s = _uiState.value
        val maxUpper = (s.length / 2).coerceAtLeast(1)
        _uiState.value = s.copy(upperCount = v.coerceIn(1, maxUpper))
        regenerate()
    }

    fun onUpperToggle(v: Boolean) {
        val s = _uiState.value
        if (v || s.useLower || s.useDigits || s.useSymbols)
            _uiState.value = s.copy(useUpper = v)
        regenerate()
    }

    fun onLowerToggle(v: Boolean) {
        val s = _uiState.value
        if (v || s.useUpper || s.useDigits || s.useSymbols)
            _uiState.value = s.copy(useLower = v)
        regenerate()
    }

    fun onDigitsToggle(v: Boolean) {
        val s = _uiState.value
        if (v || s.useUpper || s.useLower || s.useSymbols)
            _uiState.value = s.copy(useDigits = v)
        regenerate()
    }

    fun onSymbolsToggle(v: Boolean) {
        val s = _uiState.value
        if (v || s.useUpper || s.useLower || s.useDigits)
            _uiState.value = s.copy(useSymbols = v)
        regenerate()
    }

    fun onExcludeAmbiguousToggle(v: Boolean) {
        _uiState.value = _uiState.value.copy(excludeAmbiguous = v)
        regenerate()
    }

    fun onCountUpperChange(v: Int) {
        val s = _uiState.value
        _uiState.value = s.copy(countUpper = v.coerceIn(1, s.maxCountUpper()))
        regenerate()
    }

    fun onCountDigitsChange(v: Int) {
        val s = _uiState.value
        _uiState.value = s.copy(countDigits = v.coerceIn(1, s.maxCountDigits()))
        regenerate()
    }

    fun onCountSymbolsChange(v: Int) {
        val s = _uiState.value
        _uiState.value = s.copy(countSymbols = v.coerceIn(1, s.maxCountSymbols()))
        regenerate()
    }

    fun onWordCountChange(v: Int) {
        _uiState.value = _uiState.value.copy(wordCount = v.coerceIn(2, 8))
        regenerate()
    }

    fun onSeparatorChange(v: String) {
        _uiState.value = _uiState.value.copy(separator = v)
        regenerate()
    }

    fun onCapitalizeToggle(v: Boolean) {
        _uiState.value = _uiState.value.copy(capitalize = v)
        regenerate()
    }

    fun regenerate() {
        val s = _uiState.value
        _uiState.value = s.copy(password = when (s.mode) {
            GeneratorMode.STANDARD      -> generateStandard(s)
            GeneratorMode.PRONOUNCEABLE -> generatePronounceable(s)
            GeneratorMode.PASSPHRASE    -> generatePassphrase(s)
        })
    }

    private fun generateStandard(s: GeneratorUiState): String {
        val upperPool  = if (s.excludeAmbiguous) "ABCDEFGHJKLMNPQRSTUVWXYZ" else "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerPool  = if (s.excludeAmbiguous) "abcdefghjkmnpqrstuvwxyz"  else "abcdefghijklmnopqrstuvwxyz"
        val digitPool  = if (s.excludeAmbiguous) "23456789" else "0123456789"
        val symbolPool = "!@#$%^&*-_=+?"

        if (!s.useUpper && !s.useLower && !s.useDigits && !s.useSymbols) return ""

        val result = mutableListOf<Char>()
        if (s.useUpper)   repeat(s.countUpper)   { result.add(upperPool[random.nextInt(upperPool.length)]) }
        if (s.useDigits)  repeat(s.countDigits)  { result.add(digitPool[random.nextInt(digitPool.length)]) }
        if (s.useSymbols) repeat(s.countSymbols) { result.add(symbolPool[random.nextInt(symbolPool.length)]) }

        val fillerPool = if (s.useLower) lowerPool else buildString {
            if (s.useUpper)   append(upperPool)
            if (s.useDigits)  append(digitPool)
            if (s.useSymbols) append(symbolPool)
        }
        val remaining = (s.length - result.size).coerceAtLeast(0)
        repeat(remaining) { result.add(fillerPool[random.nextInt(fillerPool.length)]) }

        for (i in result.indices.reversed()) {
            val j = random.nextInt(i + 1)
            val tmp = result[i]; result[i] = result[j]; result[j] = tmp
        }
        return result.take(s.length).joinToString("")
    }

    private fun generatePronounceable(s: GeneratorUiState): String {
        val vowels     = "aeiou"
        val consonants = "bcdfghjklmnpqrstvwxyz"
        val sb = StringBuilder()
        var useConsonant = random.nextBoolean()
        while (sb.length < s.length) {
            val pool = if (useConsonant) consonants else vowels
            sb.append(pool[random.nextInt(pool.length)])
            useConsonant = !useConsonant
        }
        val chars = sb.toString().take(s.length).toCharArray()
        if (s.useUpper && chars.isNotEmpty()) {
            val count   = s.upperCount.coerceIn(1, chars.size)
            val indices = mutableSetOf<Int>()
            while (indices.size < count) indices.add(random.nextInt(chars.size))
            indices.forEach { i -> chars[i] = chars[i].uppercaseChar() }
        }
        return String(chars)
    }

    private fun generatePassphrase(s: GeneratorUiState): String {
        val words = WORD_LIST
        return (1..s.wordCount)
            .map { word ->
                val w = words[random.nextInt(words.size)]
                if (s.capitalize) w.replaceFirstChar { c -> c.uppercaseChar() } else w
            }
            .joinToString(s.separator)
    }

    companion object {
        private val WORD_LIST = listOf(
            "cielo", "tierra", "agua", "fuego", "viento", "luna", "sol", "mar",
            "noche", "dia", "bosque", "roca", "rio", "flor", "arbol", "nieve",
            "lluvia", "trueno", "rayo", "niebla", "arena", "lago", "monte", "valle",
            "lobo", "aguila", "puma", "zorro", "tigre", "leon", "oso", "gato",
            "perro", "caballo", "pajaro", "pez", "dragon", "sirena", "ninja", "pirata",
            "mesa", "silla", "puerta", "ventana", "techo", "suelo", "muro", "torre",
            "libro", "lapiz", "papel", "mapa", "llave", "espejo", "reloj", "bruja",
            "magia", "estrella", "cometa", "planeta", "robot", "codigo", "clave", "juego"
        )
    }
}
