package com.thormakk.keyarmor.presentation.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.thormakk.keyarmor.data.clipboard.ClipboardClearManager

val LocalClipboardClearManager = staticCompositionLocalOf<ClipboardClearManager> {
    error("LocalClipboardClearManager no fue provisto. Asegurate de usar CompositionLocalProvider en MainActivity.")
}
