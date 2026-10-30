package com.thormakk.keyarmor.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import com.thormakk.keyarmor.data.prefs.OnboardingPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingWelcomeViewModel @Inject constructor(
    private val onboardingPrefs: OnboardingPrefs
) : ViewModel() {

    fun markWelcomeSeen() {
        onboardingPrefs.hasSeenWelcome = true
        onboardingPrefs.tutorialSkipped = false
    }
}
