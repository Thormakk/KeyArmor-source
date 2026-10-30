package com.thormakk.keyarmor.presentation.screens.hub

data class LegalSection(val title: String, val body: String)

fun getLegalDoc(docKey: String, langCode: String): List<LegalSection> =
    when (langCode) {
        "es"    -> getLegalDocContentES(docKey)
        "en"    -> getLegalDocContentEN(docKey)
        "pt"    -> getLegalDocContentPT(docKey)
        "id"    -> getLegalDocContentID(docKey)
        "fr"    -> getLegalDocContentFR(docKey)
        "de"    -> getLegalDocContentDE(docKey)
        "hi"    -> getLegalDocContentHI(docKey)
        "it"    -> getLegalDocContentIT(docKey)
        "ja"    -> getLegalDocContentJA(docKey)
        "tr"    -> getLegalDocContentTR(docKey)
        "ko"    -> getLegalDocContentKO(docKey)
        "ru"    -> getLegalDocContentRU(docKey)
        "zh"    -> getLegalDocContentZH(docKey)
        "uk"    -> getLegalDocContentUK(docKey)
        "pl"    -> getLegalDocContentPL(docKey)
        "nl"    -> getLegalDocContentNL(docKey)
        "sv"    -> getLegalDocContentSV(docKey)
        "cs"    -> getLegalDocContentCS(docKey)
        "sk"    -> getLegalDocContentSK(docKey)
        else    -> getLegalDocContentEN(docKey)
    }
