package com.thormakk.keyarmor.presentation.screens.creditcard

import com.thormakk.keyarmor.R

object CreditCardBrandProvider {

    data class CardBrandInfo(
        val brandColorArgb: Long,
        val symbol: String
    )

    data class CardBrand(
        val key:  String,
        val name: String,
        val info: CardBrandInfo
    )

    fun drawableResForBrand(brand: String): Int? {
        val normalized = brand.trim().uppercase()
            .replace("AMERICAN EXPRESS", "AMEX")
            .replace("AMERICANEXPRESS",  "AMEX")
            .replace("DINERS CLUB",      "DINERS")
            .replace("DINERSCLUB",       "DINERS")
            .replace("UNION PAY",        "UNIONPAY")
            .replace("RU PAY",           "RUPAY")
            .replace("POSTE PAY",        "POSTEPAY")
            .replace("CARTE BANCAIRE",   "CB")
            .replace("CARTE BLEUE",      "CB")
            .replace("BCMC",             "BANCONTACT")
        return when (normalized) {
            "VISA"       -> R.drawable.ic_card_visa
            "MASTERCARD" -> R.drawable.ic_card_mastercard
            "AMEX"       -> R.drawable.ic_card_amex
            "DISCOVER"   -> R.drawable.ic_card_discover
            "JCB"        -> R.drawable.ic_card_jcb
            "DINERS"     -> R.drawable.ic_card_dinersclub
            "BANCONTACT" -> R.drawable.ic_card_bancontact
            else -> null
        }
    }

    private val map: Map<String, CardBrandInfo> = buildMap {

        put("VISA",              CardBrandInfo(0xFF1A1F71L, "VISA"))

        put("MASTERCARD",        CardBrandInfo(0xFFEB001BL, "MC"))
        put("MASTER CARD",       CardBrandInfo(0xFFEB001BL, "MC"))
        put("MC",                CardBrandInfo(0xFFEB001BL, "MC"))

        put("AMEX",              CardBrandInfo(0xFF2E77BCL, "AMEX"))
        put("AMERICAN EXPRESS",  CardBrandInfo(0xFF2E77BCL, "AMEX"))
        put("AMERICANEXPRESS",   CardBrandInfo(0xFF2E77BCL, "AMEX"))

        put("DISCOVER",          CardBrandInfo(0xFFFF6600L, "DISC"))

        put("UNIONPAY",          CardBrandInfo(0xFFDA121AL, "UP"))
        put("UNION PAY",         CardBrandInfo(0xFFDA121AL, "UP"))
        put("UP",                CardBrandInfo(0xFFDA121AL, "UP"))

        put("JCB",               CardBrandInfo(0xFF003087L, "JCB"))

        put("MAESTRO",           CardBrandInfo(0xFF009BE1L, "M"))

        put("DINERS",            CardBrandInfo(0xFF004A97L, "DC"))
        put("DINERS CLUB",       CardBrandInfo(0xFF004A97L, "DC"))
        put("DINERSCLUB",        CardBrandInfo(0xFF004A97L, "DC"))

        put("ELO",               CardBrandInfo(0xFF00A4E0L, "ELO"))

        put("MIR",               CardBrandInfo(0xFF4DB45EL, "MIR"))

        put("TROY",              CardBrandInfo(0xFFE30613L, "TROY"))

        put("RUPAY",             CardBrandInfo(0xFF097247L, "RP"))
        put("RU PAY",            CardBrandInfo(0xFF097247L, "RP"))

        put("HIPERCARD",         CardBrandInfo(0xFF822124L, "HIPER"))
        put("HIPER",             CardBrandInfo(0xFF822124L, "HIPER"))

        put("INTERAC",           CardBrandInfo(0xFFFFB81CL, "INT"))

        put("CABAL",             CardBrandInfo(0xFF004B8DL, "CAB"))

        put("NARANJA",           CardBrandInfo(0xFFFF7100L, "NAR"))

        put("CB",                CardBrandInfo(0xFF0F4C81L, "CB"))
        put("CARTE BANCAIRE",    CardBrandInfo(0xFF0F4C81L, "CB"))
        put("CARTE BLEUE",       CardBrandInfo(0xFF0F4C81L, "CB"))

        put("BANCONTACT",        CardBrandInfo(0xFF005498L, "BC"))
        put("BCMC",              CardBrandInfo(0xFF005498L, "BC"))

        put("POSTEPAY",          CardBrandInfo(0xFF009246L, "PP"))
        put("POSTE PAY",         CardBrandInfo(0xFF009246L, "PP"))

        put("MADA",              CardBrandInfo(0xFF00874EL, "MADA"))

        put("UATP",              CardBrandInfo(0xFF002B5CL, "UATP"))
    }

    fun resolve(brand: String): CardBrandInfo? {
        if (brand.isBlank()) return null
        val upper = brand.trim().uppercase()
        map[upper]?.let { return it }
        val firstToken = upper.split(" ", "-", "/").firstOrNull() ?: return null
        return map[firstToken]
    }
}
