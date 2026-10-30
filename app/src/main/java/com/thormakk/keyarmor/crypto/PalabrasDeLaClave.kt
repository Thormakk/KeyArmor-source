package com.thormakk.keyarmor.crypto

import java.security.MessageDigest

object PalabrasDeLaClave {

    const val BYTES = 16

    const val PALABRAS = 12

    const val EN_LA_LISTA = 2048

    private const val BITS_POR_PALABRA = 11
    private const val BITS_DE_VERIFICADOR = 4

    fun aPalabras(clave: ByteArray, lista: List<String>): List<String> {
        require(clave.size == BYTES) { "la clave del respaldo tiene $BYTES bytes" }
        require(lista.size == EN_LA_LISTA) { "la lista tiene que tener $EN_LA_LISTA palabras" }
        val bits = aBits(clave)
        return (0 until PALABRAS).map { grupo ->
            var indice = 0
            for (b in 0 until BITS_POR_PALABRA) {
                indice = (indice shl 1) or if (bits[grupo * BITS_POR_PALABRA + b]) 1 else 0
            }
            lista[indice]
        }
    }

    fun desdePalabras(texto: String, lista: List<String>): LecturaDeLaClave {
        require(lista.size == EN_LA_LISTA) { "la lista tiene que tener $EN_LA_LISTA palabras" }
        val escritas = texto.lowercase()
            .map { if (it in 'a'..'z') it else ' ' }
            .joinToString("")
            .split(' ')
            .filter { it.isNotEmpty() }

        if (escritas.size != PALABRAS) return LecturaDeLaClave.CantidadEquivocada(escritas.size)

        val porPalabra = HashMap<String, Int>(EN_LA_LISTA * 2)
        lista.forEachIndexed { i, p -> porPalabra[p] = i }

        val bits = BooleanArray(PALABRAS * BITS_POR_PALABRA)
        escritas.forEachIndexed { grupo, palabra ->
            val indice = porPalabra[palabra]
                ?: return LecturaDeLaClave.PalabraDesconocida(palabra, grupo + 1)
            for (b in 0 until BITS_POR_PALABRA) {
                bits[grupo * BITS_POR_PALABRA + b] =
                    (indice shr (BITS_POR_PALABRA - 1 - b)) and 1 == 1
            }
        }

        val clave = ByteArray(BYTES)
        for (i in 0 until BYTES * 8) {
            if (bits[i]) {
                clave[i / 8] = (clave[i / 8].toInt() or (1 shl (7 - i % 8))).toByte()
            }
        }

        val esperado = verificador(clave)
        for (i in 0 until BITS_DE_VERIFICADOR) {
            val vino = bits[BYTES * 8 + i]
            val tiene = (esperado shr (7 - i)) and 1 == 1
            if (vino != tiene) {
                clave.fill(0)
                return LecturaDeLaClave.NoCierra
            }
        }
        return LecturaDeLaClave.Clave(clave)
    }

    private fun aBits(clave: ByteArray): BooleanArray {
        val bits = BooleanArray(PALABRAS * BITS_POR_PALABRA)
        for (i in 0 until BYTES * 8) {
            bits[i] = (clave[i / 8].toInt() shr (7 - i % 8)) and 1 == 1
        }
        val v = verificador(clave)
        for (i in 0 until BITS_DE_VERIFICADOR) {
            bits[BYTES * 8 + i] = (v shr (7 - i)) and 1 == 1
        }
        return bits
    }

    private fun verificador(clave: ByteArray): Int =
        MessageDigest.getInstance("SHA-256").digest(clave)[0].toInt() and 0xFF
}

sealed interface LecturaDeLaClave {

    class Clave(val bytes: ByteArray) : LecturaDeLaClave

    data class PalabraDesconocida(val palabra: String, val posicion: Int) : LecturaDeLaClave

    data class CantidadEquivocada(val cuantas: Int) : LecturaDeLaClave

    data object NoCierra : LecturaDeLaClave
}
