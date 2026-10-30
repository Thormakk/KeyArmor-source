package com.thormakk.keyarmor.data.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.crypto.PalabrasDeLaClave
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListaDePalabras @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val palabras: List<String> by lazy {
        val leidas = context.resources.openRawResource(R.raw.palabras_clave)
            .bufferedReader()
            .useLines { lineas -> lineas.map { it.trim() }.filter { it.isNotEmpty() }.toList() }
        check(leidas.size == PalabrasDeLaClave.EN_LA_LISTA) {
            "la lista de palabras tiene ${leidas.size} y tiene que tener " +
                "${PalabrasDeLaClave.EN_LA_LISTA}"
        }
        leidas
    }
}
