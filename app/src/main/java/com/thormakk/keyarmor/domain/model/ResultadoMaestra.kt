package com.thormakk.keyarmor.domain.model

sealed interface ResultadoMaestra {

    data object Correcta : ResultadoMaestra

    data object Incorrecta : ResultadoMaestra

    data class EnEspera(val faltanMs: Long) : ResultadoMaestra
}
