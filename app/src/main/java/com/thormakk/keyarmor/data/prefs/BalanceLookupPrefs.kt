package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceLookupPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("balance_lookup_prefs", Context.MODE_PRIVATE)

    fun isConsentGranted(): Boolean = prefs.getBoolean(KEY_GRANTED, false)

    fun wasAsked(): Boolean = prefs.getBoolean(KEY_ASKED, false)

    fun setConsent(granted: Boolean) = prefs.edit {
        putBoolean(KEY_GRANTED, granted)
        putBoolean(KEY_ASKED, true)
    }

    private companion object {
        const val KEY_GRANTED = "consent_granted"
        const val KEY_ASKED   = "consent_asked"
    }
}
