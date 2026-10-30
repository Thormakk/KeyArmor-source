package com.thormakk.keyarmor.autofill

import java.text.Normalizer

object FieldLabel {

    private const val MAX_LEN = 60

    private val WIDGET_WORDS = setOf(
        "et", "edit", "edittext", "input", "txt", "tv", "til", "tf", "field", "text", "view", "box",
    )

    private val GENERIC = setOf(
        "password", "pass", "pwd", "passwd", "contrasena", "clave", "senha", "motdepasse",
        "passwort", "kennwort", "wachtwoord", "losenord", "haslo", "heslo", "parola", "sifre",
        "newpassword", "currentpassword", "confirmpassword",
    )

    fun resolve(
        hint:               CharSequence?,
        parentHint:         CharSequence?,
        contentDescription: CharSequence?,
        htmlAttrs:          Map<String, String>,
        idEntry:            String?,
    ): String {
        val legibles = listOf(
            hint?.toString(),
            parentHint?.toString(),
            contentDescription?.toString(),
            htmlAttrs["aria-label"],
            htmlAttrs["placeholder"],
            htmlAttrs["title"],
        )
        for (candidato in legibles) {
            val limpio = clean(candidato)
            if (limpio.isNotEmpty()) return limpio
        }
        val identificadores = listOf(idEntry, htmlAttrs["name"], htmlAttrs["id"])
        for (candidato in identificadores) {
            val limpio = fromIdentifier(candidato)
            if (limpio.isNotEmpty()) return limpio
        }
        return ""
    }

    fun clean(raw: String?): String {
        val t = raw?.replace(Regex("\\s+"), " ")?.trim().orEmpty()
        return if (t.length <= MAX_LEN) t else t.take(MAX_LEN).trimEnd()
    }

    fun fromIdentifier(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val palabras = raw
            .replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.isNotEmpty() }
            .map { it.lowercase() }
        var ini = 0
        var fin = palabras.size
        while (ini < fin && palabras[ini] in WIDGET_WORDS) ini++
        while (fin > ini && palabras[fin - 1] in WIDGET_WORDS) fin--
        val utiles = palabras.subList(ini, fin)
        if (utiles.isEmpty()) return ""
        if (utiles.all { p -> p.all { it.isDigit() } }) return ""
        val texto = utiles.joinToString(" ")
        return clean(texto.replaceFirstChar { it.uppercase() })
    }

    fun normalize(label: String): String =
        Normalizer.normalize(label, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .replace(Regex("[^\\p{L}\\p{N}]+"), "")

    fun rangoPorEtiqueta(guardada: String, actual: String): Int = when {
        normalize(actual).isEmpty()   -> 0
        sameField(guardada, actual)   -> 0
        normalize(guardada).isEmpty() -> 1
        else                          -> 2
    }

    fun sameField(a: String, b: String): Boolean {
        val na = normalize(a)
        val nb = normalize(b)
        return na.isNotEmpty() && na == nb
    }

    fun isGeneric(label: String): Boolean {
        val n = normalize(label)
        return n.isEmpty() || n in GENERIC
    }
}
