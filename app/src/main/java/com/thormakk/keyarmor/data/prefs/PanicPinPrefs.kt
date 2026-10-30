package com.thormakk.keyarmor.data.prefs

import android.content.Context
import android.os.SystemClock
import android.util.Base64
import androidx.core.content.edit
import com.thormakk.keyarmor.security.CoercionPepperManager
import com.thormakk.keyarmor.security.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

enum class PanicMode {
    WIPE,
    HIDE,
    DECOY
}

data class PanicKeyParams(
    val salt:        ByteArray,
    val memoryKb:     Int,
    val iterations:   Int,
    val parallelism:  Int
)

private const val HIDE_DURATION_MS = 48L * 60 * 60 * 1000

@Singleton
class PanicPinPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coercionPepper: CoercionPepperManager
) {
    private val prefs = context.getSharedPreferences("panic_pin_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENABLED           = "enabled"
        private const val KEY_PIN_HASH          = "pin_hash"
        private const val KEY_PIN_SALT          = "pin_salt"
        private const val KEY_HASH_VERSION      = "hash_version"
        private const val KEY_COERCION_MATERIAL_STALE = "coercion_material_stale"
        private const val KEY_MODE              = "panic_mode"
        private const val KEY_HIDE_ACTIVE       = "hide_active"
        private const val KEY_WIPE_COERCION_ACTIVE = "wipe_coercion_active"
        private const val KEY_DECOY_ACTIVE      = "decoy_active"
        private const val KEY_DECOY_SALT        = "decoy_key_salt"
        private const val KEY_DECOY_ARGON_MEMORY_KB  = "decoy_argon_memory_kb"
        private const val KEY_DECOY_ARGON_ITERATIONS = "decoy_argon_iterations"
        private const val KEY_DECOY_ARGON_PARALLELISM = "decoy_argon_parallelism"
        private const val KEY_HIDE_SALT        = "hide_key_salt"
        private const val KEY_HIDE_ARGON_MEMORY_KB  = "hide_argon_memory_kb"
        private const val KEY_HIDE_ARGON_ITERATIONS = "hide_argon_iterations"
        private const val KEY_HIDE_ARGON_PARALLELISM = "hide_argon_parallelism"
        private const val KEY_HIDE_ACTIVATED_AT = "hide_activated_at"
        private const val KEY_HIDE_ACTIVATED_ELAPSED = "hide_activated_elapsed"

        private const val CURRENT_HASH_VERSION  = 3

        private const val PBKDF2_ITERATIONS     = 300_000
        private const val PBKDF2_KEY_LENGTH     = 256
    }

    init {
        val storedVersion = prefs.getInt(KEY_HASH_VERSION, 1)
        if (prefs.getBoolean(KEY_ENABLED, false) && storedVersion < CURRENT_HASH_VERSION) {
            prefs.edit {
                putBoolean(KEY_ENABLED, false)
                remove(KEY_PIN_HASH)
                remove(KEY_PIN_SALT)
                remove(KEY_HASH_VERSION)
                putBoolean(KEY_COERCION_MATERIAL_STALE, true)
            }
        }
    }

    private val _panicModeFlow = MutableStateFlow(computeIsHideActive())
    val panicModeFlow: StateFlow<Boolean> = _panicModeFlow.asStateFlow()

    private val _wipeCoercionActiveFlow = MutableStateFlow(prefs.getBoolean(KEY_WIPE_COERCION_ACTIVE, false))

    private val _decoyActiveFlow = MutableStateFlow(prefs.getBoolean(KEY_DECOY_ACTIVE, false))
    val decoyActiveFlow: StateFlow<Boolean> = _decoyActiveFlow.asStateFlow()

    private val _coercionActiveFlow =
        MutableStateFlow(_wipeCoercionActiveFlow.value || _panicModeFlow.value || _decoyActiveFlow.value)
    val coercionActiveFlow: StateFlow<Boolean> = _coercionActiveFlow.asStateFlow()

    private fun recomputeCoercion() {
        _coercionActiveFlow.value =
            _wipeCoercionActiveFlow.value || _panicModeFlow.value || _decoyActiveFlow.value
    }

    fun isPanicPinEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setPanicPin(pin: String) {
        val salt = generateSalt()
        val hash = hashPin(coercionPepper.pepperedOrCreate(pin), salt)
        prefs.edit {
            putBoolean(KEY_ENABLED, true)
            putString(KEY_PIN_HASH, hash)
            putString(KEY_PIN_SALT, salt)
            putInt(KEY_HASH_VERSION, CURRENT_HASH_VERSION)
        }
    }

    fun clearPanicPin() {
        prefs.edit {
            putBoolean(KEY_ENABLED, false)
            remove(KEY_PIN_HASH)
            remove(KEY_PIN_SALT)
        }
        deactivateHideMode()
        coercionPepper.clear()
    }

    fun verifyPin(input: String): Boolean {
        if (!isPanicPinEnabled()) return false
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val peppered = coercionPepper.pepperedOrNull(input) ?: return false
        return MessageDigest.isEqual(
            hashPin(peppered, storedSalt).toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
    }

    fun getPanicMode(): PanicMode = when (prefs.getString(KEY_MODE, PanicMode.HIDE.name)) {
        PanicMode.WIPE.name  -> PanicMode.WIPE
        PanicMode.DECOY.name -> PanicMode.DECOY
        else                 -> PanicMode.HIDE
    }

    fun setPanicMode(mode: PanicMode) {
        prefs.edit { putString(KEY_MODE, mode.name) }
    }

    fun activateHideMode() {
        prefs.edit {
            putBoolean(KEY_HIDE_ACTIVE, true)
            putLong(KEY_HIDE_ACTIVATED_AT, System.currentTimeMillis())
            putLong(KEY_HIDE_ACTIVATED_ELAPSED, SystemClock.elapsedRealtime())
        }
        _panicModeFlow.value = true
        recomputeCoercion()
    }

    fun activateWipeCoercion() {
        prefs.edit { putBoolean(KEY_WIPE_COERCION_ACTIVE, true) }
        _wipeCoercionActiveFlow.value = true
        recomputeCoercion()
    }

    fun deactivateHideMode() {
        prefs.edit {
            putBoolean(KEY_HIDE_ACTIVE, false)
            remove(KEY_HIDE_ACTIVATED_AT)
            remove(KEY_HIDE_ACTIVATED_ELAPSED)
        }
        _panicModeFlow.value = false
        recomputeCoercion()
    }

    private fun clearWipeCoercion() {
        prefs.edit { remove(KEY_WIPE_COERCION_ACTIVE) }
        _wipeCoercionActiveFlow.value = false
        recomputeCoercion()
    }

    fun activateDecoyMode() {
        prefs.edit { putBoolean(KEY_DECOY_ACTIVE, true) }
        _decoyActiveFlow.value = true
        recomputeCoercion()
    }

    private fun deactivateDecoyMode() {
        prefs.edit { putBoolean(KEY_DECOY_ACTIVE, false) }
        _decoyActiveFlow.value = false
        recomputeCoercion()
    }

    fun hasDecoyKeyMaterial(): Boolean = prefs.contains(KEY_DECOY_SALT)

    fun clearDecoyKeyMaterial() {
        prefs.edit {
            remove(KEY_DECOY_SALT)
            remove(KEY_DECOY_ARGON_MEMORY_KB)
            remove(KEY_DECOY_ARGON_ITERATIONS)
            remove(KEY_DECOY_ARGON_PARALLELISM)
        }
    }

    fun getDecoyKeyParams(): PanicKeyParams? {
        val saltB64 = prefs.getString(KEY_DECOY_SALT, null) ?: return null
        return PanicKeyParams(
            salt        = Base64.decode(saltB64, Base64.NO_WRAP),
            memoryKb    = prefs.getInt(KEY_DECOY_ARGON_MEMORY_KB, CryptoManager.ARGON2_DEFAULT_MEMORY_KB),
            iterations  = prefs.getInt(KEY_DECOY_ARGON_ITERATIONS, CryptoManager.ARGON2_DEFAULT_ITERATIONS),
            parallelism = prefs.getInt(KEY_DECOY_ARGON_PARALLELISM, CryptoManager.ARGON2_DEFAULT_PARALLELISM)
        )
    }

    fun generateDecoyKeyMaterial(): PanicKeyParams {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        val params = PanicKeyParams(
            salt        = salt,
            memoryKb    = CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
            iterations  = CryptoManager.ARGON2_DEFAULT_ITERATIONS,
            parallelism = CryptoManager.ARGON2_DEFAULT_PARALLELISM
        )
        prefs.edit {
            putString(KEY_DECOY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            putInt(KEY_DECOY_ARGON_MEMORY_KB, params.memoryKb)
            putInt(KEY_DECOY_ARGON_ITERATIONS, params.iterations)
            putInt(KEY_DECOY_ARGON_PARALLELISM, params.parallelism)
        }
        return params
    }

    fun hasHideKeyMaterial(): Boolean = prefs.contains(KEY_HIDE_SALT)

    fun getHideKeyParams(): PanicKeyParams? {
        val saltB64 = prefs.getString(KEY_HIDE_SALT, null) ?: return null
        return PanicKeyParams(
            salt        = Base64.decode(saltB64, Base64.NO_WRAP),
            memoryKb    = prefs.getInt(KEY_HIDE_ARGON_MEMORY_KB, CryptoManager.ARGON2_DEFAULT_MEMORY_KB),
            iterations  = prefs.getInt(KEY_HIDE_ARGON_ITERATIONS, CryptoManager.ARGON2_DEFAULT_ITERATIONS),
            parallelism = prefs.getInt(KEY_HIDE_ARGON_PARALLELISM, CryptoManager.ARGON2_DEFAULT_PARALLELISM)
        )
    }

    fun generateHideKeyMaterial(): PanicKeyParams {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        val params = PanicKeyParams(
            salt        = salt,
            memoryKb    = CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
            iterations  = CryptoManager.ARGON2_DEFAULT_ITERATIONS,
            parallelism = CryptoManager.ARGON2_DEFAULT_PARALLELISM
        )
        prefs.edit {
            putString(KEY_HIDE_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            putInt(KEY_HIDE_ARGON_MEMORY_KB, params.memoryKb)
            putInt(KEY_HIDE_ARGON_ITERATIONS, params.iterations)
            putInt(KEY_HIDE_ARGON_PARALLELISM, params.parallelism)
        }
        return params
    }

    fun clearHideKeyMaterial() {
        prefs.edit {
            remove(KEY_HIDE_SALT)
            remove(KEY_HIDE_ARGON_MEMORY_KB)
            remove(KEY_HIDE_ARGON_ITERATIONS)
            remove(KEY_HIDE_ARGON_PARALLELISM)
        }
    }

    fun isCoercionMaterialStale(): Boolean = prefs.getBoolean(KEY_COERCION_MATERIAL_STALE, false)

    fun consumeCoercionMaterialStale() {
        prefs.edit { remove(KEY_COERCION_MATERIAL_STALE) }
    }

    fun clearAllCoercion() {
        deactivateHideMode()
        clearWipeCoercion()
        deactivateDecoyMode()
    }

    fun isHideModeExpired(): Boolean {
        if (!prefs.getBoolean(KEY_HIDE_ACTIVE, false)) return false
        val activatedElapsed = prefs.getLong(KEY_HIDE_ACTIVATED_ELAPSED, Long.MAX_VALUE)
        return SystemClock.elapsedRealtime() - activatedElapsed >= HIDE_DURATION_MS
    }

    private fun computeIsHideActive(): Boolean {
        if (!prefs.getBoolean(KEY_HIDE_ACTIVE, false)) return false
        val activatedElapsed = prefs.getLong(KEY_HIDE_ACTIVATED_ELAPSED, Long.MAX_VALUE)
        if (SystemClock.elapsedRealtime() - activatedElapsed >= HIDE_DURATION_MS) {
            prefs.edit {
                putBoolean(KEY_HIDE_ACTIVE, false)
                remove(KEY_HIDE_ACTIVATED_AT)
                remove(KEY_HIDE_ACTIVATED_ELAPSED)
            }
            return false
        }
        return true
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hashPin(pepperedSecret: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val spec      = PBEKeySpec(pepperedSecret.toCharArray(), saltBytes, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash    = factory.generateSecret(spec).encoded
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } finally {
            spec.clearPassword()
        }
    }
}
