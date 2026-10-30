package com.thormakk.keyarmor.security

object BusquedaEnCurso {

    @Volatile
    private var texto: String = ""

    @Volatile
    private var idDestino: String = ""

    fun recordar(consulta: String, id: String) {
        texto = consulta
        idDestino = id
    }

    fun paraEntrada(id: String): String = if (id.isNotEmpty() && id == idDestino) texto else ""

    fun limpiar() {
        texto = ""
        idDestino = ""
    }
}
