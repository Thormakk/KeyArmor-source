package com.thormakk.keyarmor.presentation.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.security.ThemeTrialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

data class AppThemeOption(
    val id: String,
    val isPremium: Boolean,
    val previewBg: Color,
    val previewBar1: Color,
    val previewBar2: Color,
    val labelColor: Color,
    val defaultAccent: Color,
    val excludedAccents: List<Color> = emptyList(),
    val accentOverride: List<Color>? = null
)

val ALL_THEMES = listOf(
    AppThemeOption("white",     false, Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFFE0E0E0), Color(0xFF1A1A1A),
        defaultAccent = Color(0xFF000000),
        accentOverride = listOf(
            Color(0xFF00695C),
            Color(0xFF000000)
        )
    ),
    AppThemeOption("dark",      false, Color(0xFF0F1115), Color(0xFF13171E), Color(0xFF1C2133), Color(0xFFC0C4CC),
        defaultAccent = Color(0xFF1976D2),
        accentOverride = listOf(
            Color(0xFF1976D2),
            Color(0xFF4FC3F7),
            Color(0xFFD4A017),
            Color(0xFFFFFFFF)
        )
    ),
    AppThemeOption("lila",      true,  Color(0xFFF7F4FF), Color(0xFFEDE5FF), Color(0xFFD0BEEF), Color(0xFF2D1A60),
        defaultAccent = Color(0xFF5E3596),
        accentOverride = listOf(
            Color(0xFF5E3596),
            Color(0xFF8B1A3A)
        )
    ),
    AppThemeOption("menta",     true,  Color(0xFFF2FCF6), Color(0xFFDCF2E8), Color(0xFFB8DECA), Color(0xFF0D3320),
        defaultAccent = Color(0xFF00695C),
        accentOverride = listOf(
            Color(0xFF2A7A50),
            Color(0xFF00695C)
        )
    ),
    AppThemeOption("cream",     true,  Color(0xFFEDE4CE), Color(0xFFDDD3B8), Color(0xFFCCC0A0), Color(0xFF3D3128),
        defaultAccent = Color(0xFF5C3D22),
        accentOverride = listOf(
            Color(0xFF5C3D22),
            Color(0xFF7A5230),
            Color(0xFF8B1A3A)
        )
    ),
    AppThemeOption("sky",       false, Color(0xFFF0F8FF), Color(0xFFDCF0FA), Color(0xFFA8CDE8), Color(0xFF0A2540),
        defaultAccent = Color(0xFF1976D2),
        accentOverride = listOf(
            Color(0xFF1976D2),
            Color(0xFF00695C)
        )
    ),
    AppThemeOption("rose",      false, Color(0xFFFFF5F7), Color(0xFFFAE8EE), Color(0xFFE0B8C8), Color(0xFF3D1020),
        defaultAccent = Color(0xFF8B1A3A),
        accentOverride = listOf(
            Color(0xFF8B1A3A),
            Color(0xFFD32F2F)
        )
    ),
    AppThemeOption("amoled",    true,  Color(0xFF0C1829), Color(0xFF122035), Color(0xFF1A2E4A), Color(0xFF4FC3F7),
        defaultAccent = Color(0xFFFFFFFF),
        accentOverride = listOf(
            Color(0xFF4FC3F7),
            Color(0xFFFFFFFF)
        )
    ),
    AppThemeOption("wine",      true,  Color(0xFF1C0A14), Color(0xFF2A1020), Color(0xFF38152C), Color(0xFFE8B0C8),
        defaultAccent = Color(0xFFE8B0C8),
        accentOverride = listOf(
            Color(0xFFE8B0C8),
            Color(0xFFFFFFFF)
        )
    ),
    AppThemeOption("petroleo",  true,  Color(0xFF16332F), Color(0xFF1E403B), Color(0xFF274C46), Color(0xFFC8D6D2),
        defaultAccent = Color(0xFFFFFFFF),
        accentOverride = listOf(
            Color(0xFF5FD0BE),
            Color(0xFFFFFFFF)
        )
    ),
    AppThemeOption("cafe",      true,  Color(0xFF2F2823), Color(0xFF3B322B), Color(0xFF473C33), Color(0xFFD8CBBF),
        defaultAccent = Color(0xFFFFFFFF),
        accentOverride = listOf(
            Color(0xFFDCB88E),
            Color(0xFFFFFFFF)
        )
    ),
    AppThemeOption("crepusculo", true,  Color(0xFF35506A), Color(0xFF3F5D78), Color(0xFF4A6B87), Color(0xFFB8C4CE),
        defaultAccent = Color(0xFFFFFFFF),
        accentOverride = listOf(
            Color(0xFF4FC3F7),
            Color(0xFFFFFFFF)
        )
    )
)

data class AccentColorOption(val color: Color)

val ALL_ACCENT_COLORS = listOf(
    AccentColorOption(Color(0xFF5C3D22)),
    AccentColorOption(Color(0xFF7A5230)),
    AccentColorOption(Color(0xFF1976D2)),
    AccentColorOption(Color(0xFF388E3C)),
    AccentColorOption(Color(0xFF7B1FA2)),
    AccentColorOption(Color(0xFF5E3596)),
    AccentColorOption(Color(0xFF2A7A50)),
    AccentColorOption(Color(0xFFD32F2F)),
    AccentColorOption(Color(0xFF8B1A3A)),
    AccentColorOption(Color(0xFFE91E63)),
    AccentColorOption(Color(0xFFE8B0C8)),
    AccentColorOption(Color(0xFF00695C)),
    AccentColorOption(Color(0xFF4FC3F7)),
    AccentColorOption(Color(0xFF5FD0BE)),
    AccentColorOption(Color(0xFFDCB88E)),
    AccentColorOption(Color(0xFFD4A017)),
    AccentColorOption(Color(0xFFFFFFFF)),
    AccentColorOption(Color(0xFF000000))
)

data class ThemeSelectorUiState(
    val savedThemeId:    String  = "dark",
    val savedAccentArgb: Long    = 0L,
    val isLoading:       Boolean = true
)

@HiltViewModel
class ThemeSelectorViewModel @Inject constructor(
    private val planPrefs:         PlanPrefs,
    private val authRepository:    AuthRepository,
    private val themeTrialManager: ThemeTrialManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeSelectorUiState())
    val uiState: StateFlow<ThemeSelectorUiState> = _uiState

    val trialThemeId: StateFlow<String?> = themeTrialManager.trialThemeId

    val isPremiumUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it != UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() != UserPlan.FREE)

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                savedThemeId    = authRepository.getThemeId(),
                savedAccentArgb = authRepository.getAccentColor(),
                isLoading       = false
            )
        }
    }

    fun saveTheme(theme: AppThemeOption) {
        if (theme.isPremium && planPrefs.effectivePlan() == UserPlan.FREE) return
        themeTrialManager.endTrial()
        viewModelScope.launch {
            val defaultArgb = theme.defaultAccent.toArgb().toLong() and 0xFFFFFFFFL
            authRepository.setThemeId(theme.id)
            authRepository.setAccentColor(defaultArgb)
            _uiState.value = _uiState.value.copy(
                savedThemeId    = theme.id,
                savedAccentArgb = defaultArgb
            )
        }
    }

    fun startThemeTrial(theme: AppThemeOption) {
        if (!theme.isPremium || planPrefs.effectivePlan() != UserPlan.FREE) return
        themeTrialManager.startTrial(theme.id)
    }

    fun saveAccent(color: Color, isPremiumTheme: Boolean = true) {
        if (isPremiumTheme && planPrefs.effectivePlan() == UserPlan.FREE) return
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        viewModelScope.launch {
            authRepository.setAccentColor(argb)
            _uiState.value = _uiState.value.copy(savedAccentArgb = argb)
        }
    }

}

internal fun srgbToLinear(c: Float): Float =
    if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)

internal fun relativeLuminance(color: Color): Float =
    0.2126f * srgbToLinear(color.red) +
    0.7152f * srgbToLinear(color.green) +
    0.0722f * srgbToLinear(color.blue)

internal fun contrastRatio(bg: Color, fg: Color): Float {
    val lighter = maxOf(relativeLuminance(bg), relativeLuminance(fg))
    val darker  = minOf(relativeLuminance(bg), relativeLuminance(fg))
    return (lighter + 0.05f) / (darker + 0.05f)
}

