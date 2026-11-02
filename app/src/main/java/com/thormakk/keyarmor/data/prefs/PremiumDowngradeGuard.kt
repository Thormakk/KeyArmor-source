package com.thormakk.keyarmor.data.prefs

import com.thormakk.keyarmor.alarms.DisguiseSuspensionManager
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumDowngradeGuard @Inject constructor(
    private val travelModePrefs: TravelModePrefs,
    private val panicPinPrefs: PanicPinPrefs,
    private val calculatorDisguisePrefs: CalculatorDisguisePrefs,
    private val breachCheckPrefs: BreachCheckPrefs,
    private val biometricPrefs: BiometricPrefs,
    private val authRepository: AuthRepository,
    private val disguiseSuspensionManager: dagger.Lazy<DisguiseSuspensionManager>,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    fun deactivateAll() {
        if (travelModePrefs.isEnabled())        travelModePrefs.setEnabled(false)
        if (panicPinPrefs.isPanicPinEnabled() && !panicPinPrefs.coercionActiveFlow.value) {
            panicPinPrefs.clearPanicPin()
        }
        if (calculatorDisguisePrefs.isEnabled()) {
            calculatorDisguisePrefs.disable()
            disguiseSuspensionManager.get().onDisguiseChanged(active = false)
        }
        if (breachCheckPrefs.isEnabled())        breachCheckPrefs.setEnabled(false)
        if (biometricPrefs.isEnabled())          biometricPrefs.setEnabled(false)
    }

    fun resetThemeToFreeDefault() {
        applicationScope.launch {
            authRepository.setThemeId(FREE_FALLBACK_THEME_ID)
            authRepository.setAccentColor(WHITE_ACCENT_ARGB)
        }
    }

    companion object {
        const val FREE_FALLBACK_THEME_ID = "dark"
        private const val WHITE_ACCENT_ARGB = 0xFFFFFFFFL
    }
}
