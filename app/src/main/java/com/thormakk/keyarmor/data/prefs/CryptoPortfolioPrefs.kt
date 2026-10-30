package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoPortfolioPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("crypto_portfolio_prefs", Context.MODE_PRIVATE)

    companion object {
        val SUPPORTED_CURRENCIES = listOf(
            "ARS", "BRL", "CAD", "CHF", "CLP", "CNY", "COP", "CZK", "EUR", "GBP",
            "IDR", "INR", "JPY", "KRW", "MXN", "PEN", "PLN", "RUB", "SEK", "TRY",
            "UAH", "USD", "UYU"
        )

        fun detectDefaultCurrency(): String {
            val locale = Locale.getDefault()
            val language = locale.language
            val country  = locale.country

            val langCountry = "${language}_${country}"
            val byLangCountry = mapOf(
                "es_AR" to "ARS", "es_MX" to "MXN", "es_CL" to "CLP",
                "es_CO" to "COP", "es_PE" to "PEN", "es_UY" to "UYU",
                "pt_BR" to "BRL", "en_GB" to "GBP", "en_CA" to "CAD",
                "fr_CA" to "CAD", "fr_CH" to "CHF", "de_CH" to "CHF"
            )
            byLangCountry[langCountry]?.let { return it }

            val byLang = mapOf(
                "ru" to "RUB", "uk" to "UAH", "tr" to "TRY",
                "id" to "IDR", "ja" to "JPY", "ko" to "KRW",
                "zh" to "CNY", "hi" to "INR", "sv" to "SEK",
                "pl" to "PLN", "cs" to "CZK"
            )
            return byLang[language] ?: "USD"
        }
    }

    fun getSelectedCurrency(): String =
        prefs.getString("selected_currency", null) ?: detectDefaultCurrency()

    fun setSelectedCurrency(currency: String) =
        prefs.edit { putString("selected_currency", currency) }
}
