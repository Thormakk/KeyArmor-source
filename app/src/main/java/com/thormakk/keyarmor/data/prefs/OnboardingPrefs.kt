package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingPrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hasSeenWelcome: Boolean
        get() = prefs.getBoolean(KEY_SEEN_WELCOME, false)
        set(value) = prefs.edit { putBoolean(KEY_SEEN_WELCOME, value) }

    var tutorialSkipped: Boolean
        get() = prefs.getBoolean(KEY_TUTORIAL_SKIPPED, false)
        set(value) = prefs.edit { putBoolean(KEY_TUTORIAL_SKIPPED, value) }

    fun resetWelcome() { hasSeenWelcome = false; tutorialSkipped = false }

    companion object {
        private const val PREFS_NAME       = "onboarding_prefs"
        private const val KEY_SEEN_WELCOME = "has_seen_welcome"
        private const val KEY_TUTORIAL_SKIPPED = "tutorial_skipped"
    }
}
