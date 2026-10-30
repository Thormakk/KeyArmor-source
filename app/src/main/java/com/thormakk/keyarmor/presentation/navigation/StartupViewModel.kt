package com.thormakk.keyarmor.presentation.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.PremiumDowngradeGuard
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.presentation.screens.settings.ALL_THEMES
import com.thormakk.keyarmor.security.ThemeTrialManager
import com.thormakk.keyarmor.workers.HeartbeatWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StartupDestination {
    object Loading : StartupDestination()
    object Language : StartupDestination()
    object Login : StartupDestination()
    object Lock : StartupDestination()
    object Calculator : StartupDestination()
}

@HiltViewModel
class StartupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository:    AuthRepository,
    private val planPrefs:         PlanPrefs,
    private val inheritancePrefs:  InheritancePrefs,
    private val calculatorDisguisePrefs: CalculatorDisguisePrefs,
    private val premiumDowngradeGuard: PremiumDowngradeGuard,
    private val themeTrialManager: ThemeTrialManager
) : ViewModel() {

    private val _destination  = MutableStateFlow<StartupDestination>(StartupDestination.Loading)
    val destination: StateFlow<StartupDestination> = _destination

    private val _themeId = MutableStateFlow(FREE_FALLBACK_THEME_ID)
    val themeId: StateFlow<String> = _themeId

    private val _accentColor = MutableStateFlow(0L)
    val accentColor: StateFlow<Long> = _accentColor

    init {
        combine(authRepository.themeIdFlow, planPrefs.planFlow, themeTrialManager.trialThemeId) { themeId, plan, trialThemeId ->
            val trialTheme = ALL_THEMES.find { it.id == trialThemeId }
            val theme = ALL_THEMES.find { it.id == themeId }
            if (trialTheme != null) {
                trialTheme.id
            } else if (theme == null || (plan == UserPlan.FREE && theme.isPremium)) {
                FREE_FALLBACK_THEME_ID
            } else themeId
        }
            .onEach { _themeId.value = it }
            .launchIn(viewModelScope)

        combine(
            authRepository.themeIdFlow, authRepository.accentColorFlow,
            themeTrialManager.trialThemeId, planPrefs.planFlow
        ) { themeId, argb, trialThemeId, plan ->
            val trialActive = ALL_THEMES.any { it.id == trialThemeId }
            val theme = ALL_THEMES.find { it.id == themeId }
            val isFreeTheme = theme?.isPremium == false
            when {
                trialActive -> 0L
                plan != UserPlan.FREE || isFreeTheme -> argb
                else -> 0L
            }
        }
            .onEach { _accentColor.value = it }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            val skipDisguise = skipDisguiseNextStartup
            skipDisguiseNextStartup = false

            val destination = when {
                authRepository.isFirstLaunch()      -> StartupDestination.Language
                !authRepository.isRegistered()      -> StartupDestination.Login
                skipDisguise                        -> StartupDestination.Lock
                calculatorDisguisePrefs.isEnabled() -> StartupDestination.Calculator
                else                                 -> StartupDestination.Lock
            }
            _destination.value = destination
        }

        viewModelScope.launch {
            planPrefs.refresh()

            if (planPrefs.effectivePlan() == UserPlan.FREE) {
                premiumDowngradeGuard.deactivateAll()
            }

            if (!inheritancePrefs.isHeartbeatIntervalMigrated()) {
                HeartbeatWorker.cancel(context)
                android.util.Log.i("HeartbeatMigration", "✓ Worker periódico cancelado — heartbeat migrado a apertura de app")
                inheritancePrefs.markHeartbeatIntervalMigrated()
            } else {
                android.util.Log.d("HeartbeatMigration", "Sin migración necesaria (ya migrado)")
            }

            val loadedThemeId = authRepository.getThemeId()
            val themeMeta = ALL_THEMES.find { it.id == loadedThemeId }
            if (themeMeta == null || (planPrefs.effectivePlan() == UserPlan.FREE && themeMeta.isPremium)) {
                authRepository.setThemeId(FREE_FALLBACK_THEME_ID)
                authRepository.setAccentColor(FREE_FALLBACK_ACCENT_ARGB)
            }

            authRepository.getAccentColor()
        }
    }

    companion object {
        private const val FREE_FALLBACK_THEME_ID =
            com.thormakk.keyarmor.data.prefs.PremiumDowngradeGuard.FREE_FALLBACK_THEME_ID
        private const val FREE_FALLBACK_ACCENT_ARGB = 0xFFFFFFFFL

        @Volatile
        var skipDisguiseNextStartup: Boolean = false
    }
}
