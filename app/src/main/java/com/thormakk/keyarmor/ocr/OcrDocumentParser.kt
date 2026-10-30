package com.thormakk.keyarmor.ocr

data class OcrResult(
    val docType:        String? = null,
    val number:         String? = null,
    val holderName:     String? = null,
    val issuingCountry: String? = null,
    val birthDate:      String? = null,
    val issueDate:      String? = null,
    val expiryDate:     String? = null
) {
    val hasAnyData: Boolean
        get() = listOf(docType, number, holderName, issuingCountry, birthDate, issueDate, expiryDate)
            .any { !it.isNullOrBlank() }
}

object OcrDocumentParser {

    fun parse(mrzLines: List<String>): OcrResult {
        return tryParseMrz(mrzLines) ?: OcrResult()
    }

    private fun tryParseMrz(lines: List<String>): OcrResult? {
        val candidates = lines.mapNotNull { toMrzCandidate(it) }

        val lines44 = candidates.filter { it.length == 44 }
        val lines30 = candidates.filter { it.length == 30 }

        return when {
            lines44.size >= 2 -> parseTd3(lines44[lines44.size - 2], lines44.last())
            lines30.size >= 3 -> parseTd1(lines30[lines30.size - 3], lines30[lines30.size - 2], lines30.last())
            lines30.size == 2 -> {
                val result = parseTd1Partial(lines30[0], lines30[1])
                if (result.number == null) {
                    val recovered = findLine1DocNumber(lines)
                    if (recovered != null) result.copy(number = recovered) else result
                } else result
            }
            else -> null
        }
    }

    private fun findLine1DocNumber(rawLines: List<String>): String? {
        val pattern = Regex("""^[IPACDV][A-Z<][A-Z]{3}([A-Z0-9<]{4,9})""")
        return rawLines
            .map { it.replace(" ", "").uppercase() }
            .firstNotNullOfOrNull { line ->
                val cleaned = buildString {
                    for (c in line) append(if (c in 'A'..'Z' || c in '0'..'9' || c == '<') c else '<')
                }
                pattern.find(cleaned)
                    ?.groupValues?.getOrNull(1)
                    ?.cleanMrz()
                    ?.validDocNumber()
            }
    }

    private fun toMrzCandidate(raw: String): String? {
        val s = raw.replace(" ", "").uppercase()
        val cleaned = buildString(s.length) {
            for (c in s) {
                append(if (c in 'A'..'Z' || c in '0'..'9' || c == '<') c else '<')
            }
        }
        return when (cleaned.length) {
            in 28..30 -> cleaned.padEnd(30, '<')
            in 42..44 -> cleaned.padEnd(44, '<')
            else      -> null
        }
    }

    private fun parseTd3(l1: String, l2: String): OcrResult {
        val docTypeChar = l1.getOrNull(0)?.toString() ?: ""
        val country     = l1.substring(2, 5).cleanMrz().normalizeCountry()
        val name        = parseMrzName(if (l1.length > 5) l1.substring(5) else "")
        val docNumber   = l2.substring(0, 9).cleanMrz()
        val birth6      = l2.substring(13, 19)
        val expiry6     = l2.substring(21, 27)
        return OcrResult(
            docType        = mrzDocType(docTypeChar),
            number         = docNumber.validDocNumber(),
            holderName     = name,
            issuingCountry = country.takeIf { it.isNotEmpty() },
            birthDate      = mrzDate(birth6),
            expiryDate     = mrzDate(expiry6)
        )
    }

    private fun parseTd1(l1: String, l2: String, l3: String): OcrResult {
        val docTypeChar = l1.getOrNull(0)?.toString() ?: ""
        val country     = l1.substring(2, 5).cleanMrz().normalizeCountry()
        val docNumber   = l1.substring(5, 14).cleanMrz()
        val birth6      = l2.substring(0, 6)
        val expiry6     = l2.substring(8, 14)
        val name        = parseMrzName(l3)
        return OcrResult(
            docType        = mrzDocType(docTypeChar),
            number         = docNumber.validDocNumber(),
            holderName     = name,
            issuingCountry = country.takeIf { it.isNotEmpty() },
            birthDate      = mrzDate(birth6),
            expiryDate     = mrzDate(expiry6)
        )
    }

    private fun parseTd1Partial(l1: String, l2: String): OcrResult {
        val nameLine = listOf(l1, l2).firstOrNull { it.contains("<<") && it[0].isLetter() }
        val dataLine = if (nameLine == l1) l2 else l1
        return OcrResult(
            holderName = nameLine?.let { parseMrzName(it) },
            birthDate  = if (dataLine.length >= 7)  mrzDate(dataLine.substring(0, 6))  else null,
            expiryDate = if (dataLine.length >= 14) mrzDate(dataLine.substring(8, 14)) else null
        )
    }

    private fun mrzDate(s: String): String? {
        if (s.length != 6 || !s.all { it.isDigit() }) return null
        val yy = s.substring(0, 2).toIntOrNull() ?: return null
        val mm = s.substring(2, 4).toIntOrNull() ?: return null
        val dd = s.substring(4, 6).toIntOrNull() ?: return null
        if (mm !in 1..12 || dd !in 1..31) return null
        val yyyy = if (yy < 40) 2000 + yy else 1900 + yy
        return "%02d/%02d/%04d".format(dd, mm, yyyy)
    }

    private fun parseMrzName(raw: String): String? {
        val clean = raw.replace('0', 'O').trimEnd('<').ifEmpty { return null }
        val parts   = clean.split(Regex("<<+"))
        val surname = parts.getOrNull(0)?.replace("<", " ")?.trim().orEmpty()
        val given   = parts.getOrNull(1)?.replace("<", " ")?.trim().orEmpty()
        return when {
            surname.isNotEmpty() && given.isNotEmpty() -> "$given $surname"
            surname.isNotEmpty() -> surname
            else                 -> null
        }
    }

    private fun mrzDocType(ch: String) = when (ch) {
        "P"            -> "Passport"
        "I", "A", "C" -> "ID Card"
        "V"            -> "Visa"
        "D"            -> "Driver License"
        else           -> null
    }

    private fun String.cleanMrz() = replace("<", "").trim()

    private fun String.validDocNumber(): String? =
        takeIf { it.isNotEmpty() && it.count { c -> c.isDigit() } >= 3 }

    private fun String.normalizeCountry(): String {
        if (length != 3 || any { !it.isLetter() }) return this
        for (shift in 0..2) {
            val rotated = substring(shift) + substring(0, shift)
            if (rotated in MRZ_COUNTRY_CODES) return rotated
        }
        return this
    }

    private val MRZ_COUNTRY_CODES = setOf(
        "AFG","ALB","DZA","AND","AGO","ATG","ARG","ARM","AUS","AUT","AZE",
        "BHS","BHR","BGD","BRB","BLR","BEL","BLZ","BEN","BTN","BOL","BIH",
        "BWA","BRA","BRN","BGR","BFA","BDI","CPV","KHM","CMR","CAN","CAF",
        "TCD","CHL","CHN","COL","COM","COD","COG","CRI","HRV","CUB","CYP",
        "CZE","DNK","DJI","DMA","DOM","ECU","EGY","SLV","GNQ","ERI","EST",
        "SWZ","ETH","FJI","FIN","FRA","GAB","GMB","GEO","DEU","GHA","GRC",
        "GRD","GTM","GIN","GNB","GUY","HTI","HND","HUN","ISL","IND","IDN",
        "IRN","IRQ","IRL","ISR","ITA","JAM","JPN","JOR","KAZ","KEN","KIR",
        "PRK","KOR","KWT","KGZ","LAO","LVA","LBN","LSO","LBR","LBY","LIE",
        "LTU","LUX","MDG","MWI","MYS","MDV","MLI","MLT","MHL","MRT","MUS",
        "MEX","FSM","MDA","MCO","MNG","MNE","MAR","MOZ","MMR","NAM","NRU",
        "NPL","NLD","NZL","NIC","NER","NGA","MKD","NOR","OMN","PAK","PLW",
        "PAN","PNG","PRY","PER","PHL","POL","PRT","QAT","ROU","RUS","RWA",
        "KNA","LCA","VCT","WSM","SMR","STP","SAU","SEN","SRB","SYC","SLE",
        "SGP","SVK","SVN","SLB","SOM","ZAF","SSD","ESP","LKA","SDN","SUR",
        "SWE","CHE","SYR","TWN","TJK","TZA","THA","TLS","TGO","TON","TTO",
        "TUN","TUR","TKM","TUV","UGA","UKR","ARE","GBR","USA","URY","UZB",
        "VUT","VEN","VNM","YEM","ZMB","ZWE",
        "UTO","XXA","XXB","XXC","XXX","D<<","GBD","GBN","GBO","GBP","GBR",
        "UNO","RKS","XKX"
    )
}
