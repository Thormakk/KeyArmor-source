package com.thormakk.keyarmor.presentation.screens.hub

enum class SecurityLevel { NO_DATA, SAFE, INTERMEDIATE, CRITICAL }

data class SecurityUiState(
    val level:        SecurityLevel = SecurityLevel.SAFE,
    val weakCount:    Int           = 0,
    val mediumCount:  Int           = 0,
)
