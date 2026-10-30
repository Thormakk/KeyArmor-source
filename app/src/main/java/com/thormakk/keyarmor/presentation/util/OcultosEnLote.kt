package com.thormakk.keyarmor.presentation.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

fun <T> Flow<List<T>>.sinOcultos(ocultos: Flow<Set<String>>, id: (T) -> String): Flow<List<T>> =
    combine(this, ocultos) { lista, h -> if (h.isEmpty()) lista else lista.filter { id(it) !in h } }
