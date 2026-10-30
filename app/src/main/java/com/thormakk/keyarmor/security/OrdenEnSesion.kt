package com.thormakk.keyarmor.security

import android.content.Context
import androidx.core.content.edit
import java.util.concurrent.ConcurrentHashMap

object OrdenEnSesion {

    private val ordenes = ConcurrentHashMap<String, String>()
    private const val ARCHIVO = "orden_de_listas"

    fun iniciar(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        prefs.all.forEach { (clave, valor) -> if (valor is String) ordenes.putIfAbsent(clave, valor) }
        prefsRef = prefs
    }

    @Volatile private var prefsRef: android.content.SharedPreferences? = null

    inline fun <reified T : Enum<T>> leer(clave: String, porDefecto: T): T =
        nombre(clave)?.let { n -> enumValues<T>().firstOrNull { it.name == n } } ?: porDefecto

    fun guardar(clave: String, orden: Enum<*>) {
        ordenes[clave] = orden.name
        prefsRef?.edit { putString(clave, orden.name) }
    }

    fun nombre(clave: String): String? = ordenes[clave]
}
