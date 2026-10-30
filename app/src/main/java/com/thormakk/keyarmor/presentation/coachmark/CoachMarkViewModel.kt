package com.thormakk.keyarmor.presentation.coachmark

import androidx.lifecycle.ViewModel
import com.thormakk.keyarmor.data.prefs.CoachMarkPrefs
import com.thormakk.keyarmor.data.prefs.OnboardingPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CoachMarkViewModel @Inject constructor(
    coachMarkPrefs:  CoachMarkPrefs,
    onboardingPrefs: OnboardingPrefs,
) : ViewModel() {
    val controller = CoachMarkController(coachMarkPrefs, onboardingPrefs)
}
