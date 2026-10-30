package com.thormakk.keyarmor.data.prefs

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculatorDisguisePrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("calculator_disguise_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENABLED  = "enabled"
        private const val KEY_SEQ_HASH = "sequence_hash"
        private const val KEY_SEQ_SALT = "sequence_salt"

        private const val PBKDF2_ITERATIONS = 20_000
        private const val PBKDF2_KEY_LENGTH = 256

        private const val LAUNCHER_DEFAULT    = "com.thormakk.keyarmor.LauncherDefaultActivity"
        private const val LAUNCHER_CALCULATOR = "com.thormakk.keyarmor.LauncherCalculatorActivity"

        fun isDisguiseActive(context: Context): Boolean =
            context.getSharedPreferences("calculator_disguise_prefs", Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, false)
    }

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun hasSequenceConfigured(): Boolean = prefs.contains(KEY_SEQ_HASH)

    fun setSequenceAndEnable(sequence: String) {
        val salt = generateSalt()
        val hash = hashSequence(sequence, salt)
        prefs.edit {
            putString(KEY_SEQ_HASH, hash)
            putString(KEY_SEQ_SALT, salt)
            putBoolean(KEY_ENABLED, true)
        }
    }

    fun enable() {
        if (!hasSequenceConfigured()) return
        prefs.edit { putBoolean(KEY_ENABLED, true) }
    }

    fun disable() {
        prefs.edit { putBoolean(KEY_ENABLED, false) }
    }

    fun clearSequence() {
        prefs.edit {
            putBoolean(KEY_ENABLED, false)
            remove(KEY_SEQ_HASH)
            remove(KEY_SEQ_SALT)
        }
    }

    fun verifySequence(sequence: String): Boolean {
        if (!isEnabled()) return false
        if (sequence.isBlank()) return false
        val storedHash = prefs.getString(KEY_SEQ_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_SEQ_SALT, null) ?: return false
        return MessageDigest.isEqual(
            hashSequence(sequence, storedSalt).toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
    }

    fun applyLauncherIcon(enabled: Boolean = isEnabled()) {
        val pm = context.packageManager
        val newState = if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                       else          PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        val oldState = if (enabled) PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                       else          PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        pm.setComponentEnabledSetting(
            ComponentName(context.packageName, LAUNCHER_CALCULATOR),
            newState,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            ComponentName(context.packageName, LAUNCHER_DEFAULT),
            oldState,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hashSequence(sequence: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val spec = PBEKeySpec(sequence.toCharArray(), saltBytes, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } finally {
            spec.clearPassword()
        }
    }
}
