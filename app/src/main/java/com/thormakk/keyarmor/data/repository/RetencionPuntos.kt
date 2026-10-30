package com.thormakk.keyarmor.data.repository

object RetencionPuntos {
    const val CUPO_SEMANALES      = 12
    const val CUPO_COMPARTIDO     = 12
    const val MANUALES_PROTEGIDOS = 2

    data class Punto(val id: String, val timestamp: Long, val manual: Boolean)

    fun aBorrar(puntos: List<Punto>): Set<String> {
        val exceso = puntos.size - CUPO_COMPARTIDO
        if (exceso <= 0) return emptySet()
        val protegidos = puntos.filter { it.manual }
            .sortedByDescending { it.timestamp }
            .take(MANUALES_PROTEGIDOS)
            .map { it.id }
            .toSet()
        return puntos.filter { it.id !in protegidos }
            .sortedBy { it.timestamp }
            .take(exceso)
            .map { it.id }
            .toSet()
    }
}
