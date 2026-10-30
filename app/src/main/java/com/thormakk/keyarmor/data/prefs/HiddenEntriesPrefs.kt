package com.thormakk.keyarmor.data.prefs

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
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

@Singleton
class HiddenEntriesPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
    private val planPrefs: PlanPrefs
) {
    private val prefs = context.getSharedPreferences("hidden_entries_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENABLED     = "enabled"
        private const val KEY_PHRASE_HASH = "phrase_hash"
        private const val KEY_PHRASE_SALT = "phrase_salt"

        private const val PBKDF2_ITERATIONS = 300_000
        private const val PBKDF2_KEY_LENGTH = 256
    }

    private val _revealActiveFlow = MutableStateFlow(false)
    val revealActiveFlow: StateFlow<Boolean> = _revealActiveFlow.asStateFlow()

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun canHideNewEntries(): Boolean = isEnabled() && planPrefs.effectivePlan() != UserPlan.FREE

    fun setPhrase(phrase: String) {
        val salt = generateSalt()
        val hash = hashPhrase(phrase, salt)
        prefs.edit {
            putBoolean(KEY_ENABLED, true)
            putString(KEY_PHRASE_HASH, hash)
            putString(KEY_PHRASE_SALT, salt)
        }
    }

    fun changePhrase(oldPhrase: String, newPhrase: String): Boolean {
        if (!verifyPhrase(oldPhrase)) return false
        setPhrase(newPhrase)
        return true
    }

    fun clearPhrase() {
        prefs.edit {
            putBoolean(KEY_ENABLED, false)
            remove(KEY_PHRASE_HASH)
            remove(KEY_PHRASE_SALT)
        }
        setRevealActive(false)
    }

    fun verifyPhrase(input: String): Boolean {
        if (!isEnabled()) return false
        if (input.isEmpty()) return false
        val storedHash = prefs.getString(KEY_PHRASE_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_PHRASE_SALT, null) ?: return false
        return MessageDigest.isEqual(
            hashPhrase(input, storedSalt).toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
    }

    fun setRevealActive(active: Boolean) {
        if (_revealActiveFlow.value != active) _revealActiveFlow.value = active
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hashPhrase(phrase: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val spec = PBEKeySpec(phrase.toCharArray(), saltBytes, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } finally {
            spec.clearPassword()
        }
    }
}
