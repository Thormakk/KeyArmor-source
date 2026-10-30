package com.thormakk.keyarmor.presentation.screens.travelmode

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.TravelModePrefs
import com.thormakk.keyarmor.data.prefs.TravelSuggestionPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.workers.TravelModeSuggestionWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TravelModeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val travelModePrefs: TravelModePrefs,
    private val suggestionPrefs: TravelSuggestionPrefs,
    planPrefs: PlanPrefs
) : ViewModel() {

    val isEnabled: StateFlow<Boolean> = travelModePrefs.travelModeFlow

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    val pendingSuggestion: StateFlow<TravelSuggestionPrefs.PendingSuggestion?> = suggestionPrefs.pendingFlow

    fun isCalendarSuggestionEnabled(): Boolean = suggestionPrefs.isEnabled()

    fun setEnabled(enabled: Boolean) {
        travelModePrefs.setEnabled(enabled)
    }

    fun toggle() {
        travelModePrefs.setEnabled(!travelModePrefs.isEnabled())
    }

    fun setCalendarSuggestionEnabled(enabled: Boolean) {
        suggestionPrefs.setEnabled(enabled)
        if (enabled) {
            TravelModeSuggestionWorker.schedule(context)
        } else {
            TravelModeSuggestionWorker.cancel(context)
        }
    }

    fun acceptSuggestion() {
        travelModePrefs.setEnabled(true)
        suggestionPrefs.clearPending()
    }

    fun dismissSuggestion() {
        val pending = suggestionPrefs.pendingFlow.value ?: return
        suggestionPrefs.addDismissed(pending.eventKey)
        suggestionPrefs.clearPending()
    }
}
