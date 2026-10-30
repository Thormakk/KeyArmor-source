package com.thormakk.keyarmor.autofill

import android.app.assist.AssistStructure
import android.content.Context
import android.os.Build
import android.text.InputType
import android.view.View
import android.view.autofill.AutofillId

data class ParsedAutofillRequest(
    val packageName:  String,
    val webDomain:    String?,
    val usernameId:   AutofillId? = null,
    val emailId:      AutofillId? = null,
    val passwordId:   AutofillId? = null,
    val cardNumberId: AutofillId? = null,
    val cardholderId: AutofillId? = null,
    val expiryDateId: AutofillId? = null,
    val cvvId:        AutofillId? = null,
    val otpId:        AutofillId? = null,
    val isBlindCardFallback: Boolean = false,
    val focusedFieldId: AutofillId? = null,
    val newPasswordIds: Set<AutofillId> = emptySet(),
    val selectFieldIds: Set<AutofillId> = emptySet(),
    val allowHiddenPasswordFill: Boolean = true,
) {
    val isLoginForm: Boolean get() =
        usernameId != null || emailId != null || passwordId != null

    val enContrasenaNueva: Boolean get() =
        focusedFieldId != null && focusedFieldId in newPasswordIds &&
            usernameId == null && emailId == null

    val isCardForm: Boolean get() =
        cardNumberId != null

    val isOtpForm: Boolean get() = otpId != null

    val hasAnyField: Boolean get() = isLoginForm || isCardForm || isOtpForm

    val loginFieldId: AutofillId? get() = usernameId ?: emailId

    companion object
}

object AutofillParser {

    private const val HINT_USERNAME         = "username"
    private const val HINT_EMAIL            = "emailAddress"
    private const val HINT_PASSWORD         = "password"
    private const val HINT_CURRENT_PASSWORD = "currentPassword"
    private const val HINT_NEW_PASSWORD     = "newPassword"
    private const val HINT_CARD_NUMBER      = "creditCardNumber"
    private const val HINT_CARD_HOLDER      = "creditCardHolderName"
    private const val HINT_CARD_EXPIRY      = "creditCardExpirationDate"
    private const val HINT_CARD_EXPIRY_MM   = "creditCardExpirationMonth"
    private const val HINT_CARD_EXPIRY_YY   = "creditCardExpirationYear"
    private const val HINT_CARD_CVV         = "creditCardSecurityCode"
    private const val HINT_OTP              = "oneTimeCode"
    private const val HINT_SMS_OTP          = "smsOTPCode"

    private val usernameKeywords = setOf("user", "usuario", "login", "account",
        "cuenta", "username", "nombre", "name")
    private val emailKeywords    = setOf("email", "correo", "mail", "e-mail")
    private val passwordKeywords = setOf("password", "contraseña", "passwd",
        "pass", "clave", "secret", "pin")
    private val cardNumKeywords  = setOf("tarjeta", "number", "numero",
        "cardnumber", "card_number", "cc_number", "pan",
        "creditcard", "credit_card", "ccnumber", "cnumber", "nrotarjeta")
    private val cvvKeywords      = setOf("cvv", "cvc", "csc", "security", "seguridad",
        "securitycode", "security_code", "codseg")
    private val expiryKeywords   = setOf("expir", "vencim", "caducidad",
        "mmyy", "mm_yy", "mm/yy", "mm/yyyy", "expdate", "exp_date",
        "expirydate", "expirationdate", "validez", "fecha_venc",
        "vto", "vcto", "fec_vcto", "vence", "fecha_exp")
    private val otpKeywords      = setOf("otp", "totp", "2fa", "two.factor",
        "twofactor", "one.time", "onetime", "token", "authenticat",
        "verificacion", "verificación", "verification", "codigo", "código")

    private val searchKeywords   = setOf("search", "buscar", "busqueda", "búsqueda",
        "query", "omnibox", "url_bar", "urlbar", "location_bar", "locationbar",
        "search_box", "searchbox", "search_field", "searchfield", "search-input",
        "suggest", "recherche", "suche", "pesquisa", "ricerca", "arama")

    private val cardNumClassKeywords = setOf("cardnumber", "cardno", "pannumber")
    private val cvvClassKeywords     = setOf("cvcedit", "cvcfield", "cvvinput",
        "securitycodeedit", "securitycodeinput", "cvvedit", "cvvfield")
    private val expiryClassKeywords  = setOf("expirydate", "expirationdate", "expirydateedit")

    fun parse(structure: AssistStructure, context: Context): ParsedAutofillRequest {
        val packageName = structure.activityComponent.packageName

        val vis  = FieldAccumulator()
        val hid  = FieldAccumulator()
        var webDomain:        String?     = null
        var focusedId:        AutofillId? = null
        var focusedTypeClass: Int         = 0
        var focusedMaxLen:    Int         = -1
        var focusedIsFreeText: Boolean    = false
        var focusedIsSearch:   Boolean    = false
        var focusedHasOwnLabel: Boolean   = false

        val dominioPorCampo = mutableMapOf<AutofillId, String>()
        val absPos = mutableMapOf<AutofillId, IntArray>()

        for (i in 0 until structure.windowNodeCount) {
            traverseNode(structure.getWindowNodeAt(i).rootViewNode, absMap = absPos) { node, parentHint, dominioNodo ->
                if (node.webDomain != null) webDomain = node.webDomain

                val id = node.autofillId ?: return@traverseNode
                if (dominioNodo != null) dominioPorCampo[id] = dominioNodo
                val visible = node.visibility == View.VISIBLE
                val acc = if (visible) vis else hid

                if (node.isFocused && visible) {
                    focusedId        = id
                    focusedTypeClass = node.inputType and InputType.TYPE_MASK_CLASS
                    focusedMaxLen    = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                                           node.maxTextLength else -1
                    val htmlTag = node.htmlInfo?.tag?.lowercase()
                    focusedIsFreeText = htmlTag == "textarea" ||
                        (htmlTag != null && htmlTag != "input") ||
                        (node.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
                    focusedIsSearch   = isSearchField(node)
                    focusedHasOwnLabel = !node.hint.isNullOrBlank() || !node.idEntry.isNullOrBlank() ||
                        !node.contentDescription.isNullOrBlank()
                }

                classifyNode(node, id, acc, parentHint)
            }
        }

        if (webDomain != null && !TrustedBrowsers.isTrusted(packageName, context)) {
            webDomain = null
            dominioPorCampo.clear()
        }

        val focusedAlreadyClassified = focusedId != null && (
            focusedId == vis.usernameId   || focusedId == vis.emailId      ||
            focusedId == vis.passwordId   || focusedId == vis.cardNumberId ||
            focusedId == vis.cvvId        || focusedId == vis.expiryDateId ||
            focusedId == hid.usernameId   || focusedId == hid.emailId      ||
            focusedId == hid.passwordId   || focusedId == hid.cardNumberId ||
            focusedId == hid.cvvId        || focusedId == hid.expiryDateId
        )
        if (vis.otpId == null && hid.otpId == null && !focusedAlreadyClassified && focusedId != null &&
            !focusedIsFreeText) {
            val isNumericClass   = focusedTypeClass == InputType.TYPE_CLASS_NUMBER &&
                                   focusedMaxLen in 4..8
            val webSinPistas     = webDomain != null && !focusedHasOwnLabel && !focusedIsSearch
            val isShortText      = focusedTypeClass == InputType.TYPE_CLASS_TEXT &&
                                   (focusedMaxLen in 4..8 || (focusedMaxLen == -1 && webSinPistas))
            val isWebUnknownType = focusedTypeClass == 0 && webSinPistas &&
                                   vis.cardNumberId == null && hid.cardNumberId == null
            if (isNumericClass || isShortText || isWebUnknownType) vis.otpId = focusedId
        }

        if (vis.cardNumberId == null && hid.cardNumberId == null &&
            vis.usernameId   == null && vis.emailId      == null && vis.passwordId == null &&
            vis.otpId        == null && hid.otpId        == null &&
            webDomain        == null) {
            val nullFields = vis.unclassifiedNullTypeIds
            val lens = nullFields.map { it.second }.sorted()
            val plausibleByLength = nullFields.size >= 3 && lens.all { it > 0 } &&
                lens.last() in 15..25 && lens.first() in 3..4 &&
                lens.getOrNull(1)?.let { it in 5..8 } == true
            var geoCard: AutofillId? = null; var geoExp: AutofillId? = null; var geoCvv: AutofillId? = null
            if (nullFields.size == 3) {
                val g = nullFields.mapNotNull { f -> absPos[f.first]?.let { f.first to it } }
                if (g.size == 3) {
                    val porY = g.sortedBy { it.second[1] }
                    val top = porY[0]; val a = porY[1]; val b = porY[2]
                    val mismaFila = kotlin.math.abs(a.second[1] - b.second[1]) <= 20
                    val anchosIguales = kotlin.math.abs(a.second[2] - b.second[2]) <= a.second[2] / 4
                    val arribaAncho = top.second[1] < a.second[1] && top.second[2] >= 3 * a.second[2] / 2
                    if (mismaFila && anchosIguales && arribaAncho) {
                        val (izq, der) = if (a.second[0] <= b.second[0]) a to b else b to a
                        geoCard = top.first; geoExp = izq.first; geoCvv = der.first
                    }
                }
            }
            val pantallaDeTarjeta = geoCard != null
            val positionalOk = pantallaDeTarjeta && lens.all { it <= 0 }
            if (plausibleByLength) {
                val cardField   = nullFields.maxByOrNull  { it.second }
                val cvvField    = nullFields.filter { it != cardField }
                                            .minByOrNull { it.second }
                val expiryField = nullFields.firstOrNull { it != cardField && it != cvvField }
                if (cardField != null && cvvField != null && expiryField != null) {
                    vis.cardNumberId     = cardField.first
                    vis.cvvId            = cvvField.first
                    vis.expiryDateId     = expiryField.first
                    vis.blindCardFallback = true
                }
            } else if (positionalOk) {
                vis.cvvId            = geoCvv
                vis.expiryDateId     = geoExp
                vis.cardNumberId     = geoCard
                vis.blindCardFallback = true
            }
        }

        if (vis.cardNumberId != null &&
            vis.cvvId == null && vis.expiryDateId == null &&
            vis.usernameId == null && vis.emailId == null && vis.passwordId == null &&
            vis.otpId == null && hid.otpId == null &&
            webDomain == null) {
            val nullFields = vis.unclassifiedNullTypeIds
            if (nullFields.size >= 2 && nullFields.all { it.second > 0 }) {
                val sorted = nullFields.sortedBy { it.second }
                vis.cvvId            = sorted[0].first
                vis.expiryDateId     = sorted[1].first
                vis.blindCardFallback = true
            } else if (nullFields.size >= 2) {
                vis.expiryDateId     = nullFields[0].first
                vis.cvvId            = nullFields[1].first
                vis.blindCardFallback = true
            }
        }

        val isVisibleOtpOnlyForm = vis.otpId != null &&
            vis.usernameId == null && vis.emailId == null &&
            vis.passwordId == null && vis.cardNumberId == null

        val focoActual: AutofillId? = focusedId
        val focoEsContrasena = focoActual != null &&
            (focoActual in vis.allPasswordIds || focoActual in hid.allPasswordIds)
        val mergedPassword = when {
            isVisibleOtpOnlyForm -> null
            focoEsContrasena     -> focoActual
            else                 -> vis.passwordId ?: hid.passwordId
        }
        val mergedUser     = if (isVisibleOtpOnlyForm) null else (vis.usernameId ?: hid.usernameId)
        val mergedMail     = if (isVisibleOtpOnlyForm) null else (vis.emailId    ?: hid.emailId)
        val looksLikeLogin = mergedPassword != null && (mergedUser != null || mergedMail != null)

        val passwordEsFallbackOculto = vis.passwordId == null && hid.passwordId != null
        val hayLoginVisible          = vis.usernameId != null  || vis.emailId != null
        val allowHiddenPasswordFill  = !passwordEsFallbackOculto || hayLoginVisible

        val idsDeCredenciales     = listOfNotNull(mergedPassword, mergedUser, mergedMail)
        val dominiosDeCredenciales = idsDeCredenciales.mapNotNull { dominioPorCampo[it] }.toSet()
        val esOrigenCruzado        = dominiosDeCredenciales.size > 1

        val dominioAutoritativo =
            mergedPassword?.let { dominioPorCampo[it] }
                ?: mergedUser?.let { dominioPorCampo[it] }
                ?: mergedMail?.let { dominioPorCampo[it] }
                ?: webDomain

        return ParsedAutofillRequest(
            packageName         = packageName,
            webDomain           = if (esOrigenCruzado) null else dominioAutoritativo,
            usernameId          = if (esOrigenCruzado) null else mergedUser,
            emailId             = if (esOrigenCruzado) null else mergedMail,
            passwordId          = if (esOrigenCruzado) null else mergedPassword,
            cardNumberId        = if (looksLikeLogin || esOrigenCruzado) null else (vis.cardNumberId ?: hid.cardNumberId),
            cardholderId        = if (looksLikeLogin || esOrigenCruzado) null else (vis.cardholderId ?: hid.cardholderId),
            expiryDateId        = if (looksLikeLogin || esOrigenCruzado) null else (vis.expiryDateId ?: hid.expiryDateId),
            cvvId               = if (looksLikeLogin || esOrigenCruzado) null else (vis.cvvId        ?: hid.cvvId),
            otpId               = if (esOrigenCruzado) null else (vis.otpId ?: hid.otpId),
            isBlindCardFallback = !looksLikeLogin && vis.blindCardFallback,
            focusedFieldId      = focusedId,
            newPasswordIds      = if (esOrigenCruzado) emptySet() else (vis.newPasswordIds + hid.newPasswordIds),
            selectFieldIds      = vis.selectFieldIds + hid.selectFieldIds,
            allowHiddenPasswordFill = allowHiddenPasswordFill,
        )
    }

    private fun classifyNode(
        node:       AssistStructure.ViewNode,
        id:         AutofillId,
        acc:        FieldAccumulator,
        parentHint: String = ""
    ) {
        if (isSearchField(node)) return

        val hints = node.autofillHints
        if (!hints.isNullOrEmpty()) {
            var matched = false
            for (hint in hints) {
                when (hint) {
                    HINT_USERNAME                          -> { acc.setUsernameIfEmpty(id); matched = true }
                    HINT_EMAIL                            -> { acc.setEmailIfEmpty(id); matched = true }
                    HINT_PASSWORD, HINT_CURRENT_PASSWORD,
                    HINT_NEW_PASSWORD,
                    "current-password", "new-password"   -> {
                        acc.setPasswordIfEmpty(id); matched = true
                        if (hint == HINT_NEW_PASSWORD || hint == "new-password") acc.newPasswordIds.add(id)
                    }
                    HINT_CARD_NUMBER                      -> { acc.setCardNumberIfEmpty(id); matched = true }
                    HINT_CARD_HOLDER                      -> { acc.setCardholderIfEmpty(id); matched = true }
                    HINT_CARD_EXPIRY, HINT_CARD_EXPIRY_MM,
                    HINT_CARD_EXPIRY_YY                   -> { acc.setExpiryDateIfEmpty(id); matched = true }
                    HINT_CARD_CVV                         -> { acc.setCvvIfEmpty(id); matched = true }
                    HINT_OTP, HINT_SMS_OTP,
                    "one-time-code"                       -> { acc.setOtpIfEmpty(id); matched = true }
                }
            }
            if (matched) return
        }

        val classLower = node.className?.lowercase() ?: ""
        if (classLower.isNotEmpty() && (classLower.contains("edit") || classLower.contains("input"))) {
            when {
                cardNumClassKeywords.any  { classLower.contains(it) } -> { acc.setCardNumberIfEmpty(id); return }
                cvvClassKeywords.any      { classLower.contains(it) } -> { acc.setCvvIfEmpty(id); return }
                expiryClassKeywords.any   { classLower.contains(it) } -> { acc.setExpiryDateIfEmpty(id); return }
            }
        }

        val htmlAttrs = node.htmlInfo?.attributes
        val htmlAutocomplete = htmlAttrs
            ?.find { it.first.equals("autocomplete", ignoreCase = true) }
            ?.second?.lowercase()
        if (htmlAutocomplete != null) {
            when {
                htmlAutocomplete == "one-time-code"             -> { acc.setOtpIfEmpty(id); return }
                htmlAutocomplete.contains("otp")                -> { acc.setOtpIfEmpty(id); return }
                htmlAutocomplete == "username"                  -> acc.setUsernameIfEmpty(id)
                htmlAutocomplete == "email"                     -> acc.setEmailIfEmpty(id)
                htmlAutocomplete == "current-password" ||
                htmlAutocomplete == "new-password" ||
                htmlAutocomplete == "password"                  -> {
                    acc.setPasswordIfEmpty(id)
                    if (htmlAutocomplete == "new-password") acc.newPasswordIds.add(id)
                }
                htmlAutocomplete == "cc-number"                 -> acc.setCardNumberIfEmpty(id)
                htmlAutocomplete == "cc-name"                   -> acc.setCardholderIfEmpty(id)
                htmlAutocomplete.startsWith("cc-exp")           -> {
                    acc.setExpiryDateIfEmpty(id)
                    if (htmlAttrs.any { it.first.equals("type", ignoreCase = true) &&
                            it.second.equals("select-one", ignoreCase = true) }) {
                        acc.selectFieldIds.add(id)
                    }
                }
                htmlAutocomplete == "cc-csc"                    -> acc.setCvvIfEmpty(id)
            }
        }

        if (htmlAttrs != null) {
            val chromeHint = (htmlAttrs.find { it.first.equals("computed-autofill-hints", ignoreCase = true) }
                ?: htmlAttrs.find { it.first.equals("ua-autofill-hints", ignoreCase = true) })
                ?.second?.uppercase()
            val isSelectField = htmlAttrs.any {
                it.first.equals("type", ignoreCase = true) && it.second.equals("select-one", ignoreCase = true)
            }
            if (chromeHint != null) {
                when {
                    chromeHint.contains("CREDIT_CARD_NUMBER")            -> { acc.setCardNumberIfEmpty(id); return }
                    chromeHint.contains("CREDIT_CARD_NAME")              -> { acc.setCardholderIfEmpty(id); return }
                    chromeHint.contains("CREDIT_CARD_VERIFICATION_CODE") ||
                    chromeHint.contains("CREDIT_CARD_CVC")               -> { acc.setCvvIfEmpty(id); return }
                    chromeHint.contains("CREDIT_CARD_EXP")               -> {
                        acc.setExpiryDateIfEmpty(id)
                        if (isSelectField) acc.selectFieldIds.add(id)
                        return
                    }
                    chromeHint.contains("EMAIL")                         -> { acc.setEmailIfEmpty(id); return }
                    chromeHint.contains("PASSWORD")                      -> { acc.setPasswordIfEmpty(id); return }
                    chromeHint.contains("USERNAME") ||
                    chromeHint.contains("LOGIN")                         -> { acc.setUsernameIfEmpty(id); return }
                }
            }
        }

        if (htmlAttrs != null && acc.otpId == null) {
            val htmlName   = htmlAttrs.find { it.first.equals("name", ignoreCase = true) }
                                 ?.second?.lowercase() ?: ""
            val htmlId     = htmlAttrs.find { it.first.equals("id",   ignoreCase = true) }
                                 ?.second?.lowercase() ?: ""
            val attrText   = "$htmlName $htmlId".trim()
            if (attrText.isNotEmpty() && otpKeywords.any { attrText.contains(it) }) {
                acc.setOtpIfEmpty(id); return
            }
        }

        if (htmlAttrs != null) {
            val htmlInputmode = htmlAttrs.find { it.first.equals("inputmode", ignoreCase = true) }
                                    ?.second?.lowercase()
            val htmlMaxlength = htmlAttrs.find { it.first.equals("maxlength", ignoreCase = true) }
                                    ?.second?.toIntOrNull()
            if (htmlInputmode == "numeric" && htmlMaxlength != null) {
                when {
                    htmlMaxlength >= 15 && acc.cardNumberId == null ->
                        acc.setCardNumberIfEmpty(id)
                    htmlMaxlength in 4..8 &&
                    acc.otpId == null && acc.usernameId == null &&
                    acc.emailId == null && acc.passwordId == null &&
                    acc.cardNumberId == null ->
                        { acc.setOtpIfEmpty(id); return }
                }
            }
        }

        val htmlType = htmlAttrs
            ?.find { it.first.equals("type", ignoreCase = true) }
            ?.second?.lowercase()
        if (htmlType != null) {
            when (htmlType) {
                "password" -> acc.setPasswordIfEmpty(id)
                "email"    -> if (acc.emailId    == null) acc.emailId    = id
                "text", "tel" -> checkHeuristic(node,
                    onUsername   = { if (acc.usernameId   == null) acc.usernameId   = id },
                    onEmail      = { if (acc.emailId      == null) acc.emailId      = id },
                    onPassword   = {   },
                    onCardNumber = { if (acc.cardNumberId == null) acc.cardNumberId = id },
                    onCvv        = { if (acc.cvvId        == null) acc.cvvId        = id },
                    onExpiryDate = { if (acc.expiryDateId == null) acc.expiryDateId = id },
                    onOtp        = { if (acc.otpId        == null) acc.otpId        = id },
                    parentHint   = parentHint
                )
                "number"   -> checkHeuristic(node,
                    onUsername   = {},
                    onEmail      = {},
                    onPassword   = {},
                    onCardNumber = { if (acc.cardNumberId == null) acc.cardNumberId = id },
                    onCvv        = { if (acc.cvvId        == null) acc.cvvId        = id },
                    onExpiryDate = {},
                    onOtp        = { if (acc.otpId        == null) acc.otpId        = id },
                    parentHint   = parentHint
                )
            }
            return
        }

        val inputType = node.inputType
        val typeClass = inputType and InputType.TYPE_MASK_CLASS
        val typeVar   = inputType and InputType.TYPE_MASK_VARIATION

        if (typeClass == InputType.TYPE_CLASS_TEXT) {
            when (typeVar) {
                InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ->
                    acc.setPasswordIfEmpty(id)

                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ->
                    if (acc.emailId == null) acc.emailId = id

                InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE,
                InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE,
                InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT -> Unit

                else -> if (inputType and (InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                                           InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0) Unit
                else checkHeuristic(node,
                    onUsername   = { if (acc.usernameId   == null) acc.usernameId   = id },
                    onEmail      = { if (acc.emailId      == null) acc.emailId      = id },
                    onPassword   = {   },
                    onCardNumber = { if (acc.cardNumberId == null) acc.cardNumberId = id },
                    onCvv        = { if (acc.cvvId        == null) acc.cvvId        = id },
                    onExpiryDate = { if (acc.expiryDateId == null) acc.expiryDateId = id },
                    onOtp        = { if (acc.otpId        == null) acc.otpId        = id },
                    parentHint   = parentHint
                )
            }
        }

        if (typeClass == InputType.TYPE_CLASS_NUMBER) {
            checkHeuristic(node,
                onUsername   = {},
                onEmail      = {},
                onPassword   = {},
                onCardNumber = { if (acc.cardNumberId == null) acc.cardNumberId = id },
                onCvv        = { if (acc.cvvId        == null) acc.cvvId        = id },
                onExpiryDate = {},
                onOtp        = { if (acc.otpId        == null) acc.otpId        = id },
                parentHint   = parentHint
            )
        }

        val ownLabelBlank = node.hint.isNullOrBlank() && node.idEntry.isNullOrBlank() &&
            node.contentDescription.isNullOrBlank()
        if (typeClass == 0 && parentHint.isNotBlank() && ownLabelBlank) {
            checkHeuristic(node,
                onUsername   = { if (acc.usernameId   == null) acc.usernameId   = id },
                onEmail      = { if (acc.emailId      == null) acc.emailId      = id },
                onPassword   = { acc.setPasswordIfEmpty(id) },
                onCardNumber = { if (acc.cardNumberId == null) acc.cardNumberId = id },
                onCvv        = { if (acc.cvvId        == null) acc.cvvId        = id },
                onExpiryDate = { if (acc.expiryDateId == null) acc.expiryDateId = id },
                onOtp        = { if (acc.otpId        == null) acc.otpId        = id },
                parentHint   = parentHint
            )
        }

        if (typeClass == 0 && classLower.contains("edit")) {
            val isAlreadyClassified = id == acc.cardNumberId || id == acc.cvvId ||
                id == acc.expiryDateId || id == acc.cardholderId ||
                id == acc.usernameId   || id == acc.emailId      ||
                id == acc.passwordId   || id == acc.otpId
            if (!isAlreadyClassified) {
                val maxLen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                                 node.maxTextLength else -1
                acc.unclassifiedNullTypeIds.add(Pair(id, maxLen))
            }
        }
    }

    private fun isSearchField(node: AssistStructure.ViewNode): Boolean {
        val htmlAttrs = node.htmlInfo?.attributes
        val htmlType = htmlAttrs?.find { it.first.equals("type", ignoreCase = true) }?.second?.lowercase()
        if (htmlType == "search") return true

        val htmlName = htmlAttrs?.find { it.first.equals("name", ignoreCase = true) }?.second?.lowercase() ?: ""
        val htmlId   = htmlAttrs?.find { it.first.equals("id",   ignoreCase = true) }?.second?.lowercase() ?: ""
        if (htmlName == "q" || htmlId == "q") return true

        val typeClass = node.inputType and InputType.TYPE_MASK_CLASS
        val typeVar   = node.inputType and InputType.TYPE_MASK_VARIATION
        if (typeClass == InputType.TYPE_CLASS_TEXT &&
            typeVar == InputType.TYPE_TEXT_VARIATION_FILTER) return true

        val text = listOf(
            node.idEntry, node.hint, node.contentDescription?.toString(),
            node.className, htmlName, htmlId
        ).joinToString(" ") { it?.lowercase() ?: "" }
        return searchKeywords.any { text.contains(it) }
    }

    private fun checkHeuristic(
        node:          AssistStructure.ViewNode,
        onUsername:    () -> Unit,
        onEmail:       () -> Unit,
        onPassword:    () -> Unit,
        onCardNumber:  () -> Unit,
        onCvv:         () -> Unit,
        onExpiryDate:  () -> Unit,
        onOtp:         () -> Unit,
        parentHint:    String = "",
    ) {
        val hint    = node.hint                           ?: ""
        val idEntry = node.idEntry                        ?: ""
        val desc    = node.contentDescription?.toString() ?: ""
        val text    = "$hint $idEntry $desc $parentHint".trim()
        if (text.isBlank()) return
        val kind = when {
            matchesKeyword(text, otpKeywords)      -> "otp"
            matchesKeyword(text, cardNumKeywords)  -> "cardNumber"
            matchesKeyword(text, cvvKeywords)      -> "cvv"
            matchesKeyword(text, expiryKeywords)   -> "expiry"
            matchesKeyword(text, passwordKeywords) -> "password"
            matchesKeyword(text, emailKeywords)    -> "email"
            matchesKeyword(text, usernameKeywords) -> "username"
            else -> return
        }
        when (kind) {
            "otp"        -> onOtp()
            "cardNumber" -> onCardNumber()
            "cvv"        -> onCvv()
            "expiry"     -> onExpiryDate()
            "password"   -> onPassword()
            "email"      -> onEmail()
            "username"   -> onUsername()
        }
    }

    private fun matchesKeyword(raw: String, keywords: Set<String>): Boolean {
        val lower = raw.lowercase()
        val words = raw
            .replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
            .lowercase()
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.isNotEmpty() }
            .toSet()
        return keywords.any { k ->
            if (k.length <= 4 && k.all { it.isLetterOrDigit() }) k in words else lower.contains(k)
        }
    }

    private fun traverseNode(
        node:         AssistStructure.ViewNode,
        parentHint:   String = "",
        parentDomain: String? = null,
        ox:           Int = 0,
        oy:           Int = 0,
        absMap:       MutableMap<AutofillId, IntArray>? = null,
        action:       (AssistStructure.ViewNode, String, String?) -> Unit
    ) {
        val nx = ox + node.left
        val ny = oy + node.top
        node.autofillId?.let { absMap?.put(it, intArrayOf(nx, ny, node.width, node.height)) }
        val dominioVigente = node.webDomain?.takeIf { it.isNotBlank() } ?: parentDomain
        action(node, parentHint, dominioVigente)

        val childHint = node.hint?.lowercase()?.takeIf { it.isNotBlank() }
            ?: node.idEntry?.lowercase()?.takeIf { it.isNotBlank() && it.length > 4 }
            ?: parentHint
        for (i in 0 until node.childCount) {
            traverseNode(node.getChildAt(i), childHint, dominioVigente,
                nx - node.scrollX, ny - node.scrollY, absMap, action)
        }
    }

    private class FieldAccumulator {
        var usernameId:   AutofillId? = null
        var emailId:      AutofillId? = null
        var passwordId:   AutofillId? = null
        var cardNumberId: AutofillId? = null
        var cardholderId: AutofillId? = null
        var expiryDateId: AutofillId? = null
        var cvvId:        AutofillId? = null
        var otpId:        AutofillId? = null

        val unclassifiedNullTypeIds: MutableList<Pair<AutofillId, Int>> = mutableListOf()

        var blindCardFallback: Boolean = false

        val selectFieldIds: MutableSet<AutofillId> = mutableSetOf()

        val newPasswordIds: MutableSet<AutofillId> = linkedSetOf()

        fun setUsernameIfEmpty(id: AutofillId) { if (usernameId == null) usernameId = id }

        fun setEmailIfEmpty(id: AutofillId) { if (emailId == null) emailId = id }

        val allPasswordIds: MutableSet<AutofillId> = linkedSetOf()
        fun setPasswordIfEmpty(id: AutofillId) { allPasswordIds.add(id); if (passwordId == null) passwordId = id }

        fun setCardNumberIfEmpty(id: AutofillId) { if (cardNumberId == null) cardNumberId = id }

        fun setCardholderIfEmpty(id: AutofillId) { if (cardholderId == null) cardholderId = id }

        fun setExpiryDateIfEmpty(id: AutofillId) { if (expiryDateId == null) expiryDateId = id }

        fun setCvvIfEmpty(id: AutofillId) { if (cvvId == null) cvvId = id }

        fun setOtpIfEmpty(id: AutofillId) { if (otpId == null) otpId = id }
    }
}
