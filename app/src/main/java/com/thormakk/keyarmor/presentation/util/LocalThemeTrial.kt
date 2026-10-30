package com.thormakk.keyarmor.presentation.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.thormakk.keyarmor.security.ThemeTrialManager

val LocalThemeTrialManager = staticCompositionLocalOf<ThemeTrialManager> {
    error("LocalThemeTrialManager no fue provisto. Asegurate de usar CompositionLocalProvider en MainActivity.")
}
