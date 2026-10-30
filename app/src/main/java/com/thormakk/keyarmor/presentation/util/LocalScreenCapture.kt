package com.thormakk.keyarmor.presentation.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.thormakk.keyarmor.security.ScreenCaptureManager

val LocalScreenCaptureManager = staticCompositionLocalOf<ScreenCaptureManager> {
    error("LocalScreenCaptureManager no fue provisto. Asegurate de usar CompositionLocalProvider en MainActivity.")
}
