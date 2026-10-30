package com.thormakk.keyarmor.presentation.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.thormakk.keyarmor.security.AutoLockManager

val LocalAutoLockManager = staticCompositionLocalOf<AutoLockManager?> { null }
