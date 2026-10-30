package com.thormakk.keyarmor.domain.model

data class Tag(
    val id:        String,
    val name:      String,
    val canDelete: Boolean = false,
    val isInUse:   Boolean = false
)
