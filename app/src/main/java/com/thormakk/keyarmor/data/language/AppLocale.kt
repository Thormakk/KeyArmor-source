package com.thormakk.keyarmor.data.language

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.thormakk.keyarmor.data.repository.TagRepository
import java.util.Locale

fun idiomaActivo(): String {
    val elegido  = AppCompatDelegate.getApplicationLocales()
    val etiqueta = if (!elegido.isEmpty) elegido[0]?.language else LocaleListCompat.getDefault()[0]?.language
    val normalizado = when (etiqueta) {
        "in" -> "id"
        "iw" -> "he"
        "ji" -> "yi"
        else -> etiqueta
    }
    return TagRepository.SUPPORTED_LANGUAGE_CODES.firstOrNull { it == normalizado } ?: "en"
}

fun contextoDelIdioma(base: Context, codigo: String? = null): Context {
    val config = Configuration(base.resources.configuration)
    config.setLocale(Locale.forLanguageTag(codigo ?: idiomaActivo()))
    return base.createConfigurationContext(config)
}
