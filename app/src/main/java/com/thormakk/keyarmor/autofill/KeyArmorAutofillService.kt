package com.thormakk.keyarmor.autofill

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.app.slice.Slice
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.SaveCallback
import android.service.autofill.SaveInfo
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.AutofillPrefs
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.presentation.util.TotpGenerator
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class KeyArmorAutofillService : AutofillService() {

    @Inject lateinit var sessionManager:           SessionManager
    @Inject lateinit var autoLockManager:          com.thormakk.keyarmor.security.AutoLockManager
    @Inject lateinit var vaultRepository:          VaultRepository
    @Inject lateinit var creditCardRepository:     CreditCardRepository
    @Inject lateinit var twoFactorRepository:      TwoFactorRepository
    @Inject lateinit var vaultMetadataRepository:  VaultMetadataRepository
    @Inject lateinit var publicSuffixList:         PublicSuffixList
    @Inject lateinit var autofillPrefs:            AutofillPrefs

    private val saveInfoFactory by lazy { AutofillSaveInfoFactory(this, publicSuffixList) }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val recentFillsCache = object : LinkedHashMap<String, SavedCredentials>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, SavedCredentials>?) =
            size > 4
    }

    override fun onConnected() {
        super.onConnected()
        serviceScope.launch { runCatching { publicSuffixList.getRegistrableDomain("example.com") } }
        serviceScope.launch {
            sessionManager.isUnlockedFlow
                .drop(1)
                .filter { !it }
                .collect {
                    synchronized(recentFillsCache) { recentFillsCache.clear() }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onFillRequest(
        request:            FillRequest,
        cancellationSignal: CancellationSignal,
        callback:           FillCallback
    ) {
        val structure = request.fillContexts.lastOrNull()?.structure
            ?: run { callback.onSuccess(null); return }

        val parsed = AutofillParser.parse(structure, applicationContext)

        if (parsed.packageName == applicationContext.packageName) {
            callback.onSuccess(null); return
        }

        if (!parsed.hasAnyField) { callback.onSuccess(null); return }

        val targets = saveInfoFactory.blockTargets(parsed)
        if (autofillPrefs.isFullyBlocked(targets)) {
            callback.onSuccess(null); return
        }

        autofillPrefs.noteSeen(targets.first())

        val inlineSpec: InlinePresentationSpec? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                request.inlineSuggestionsRequest?.inlinePresentationSpecs?.firstOrNull()
            else null

        autoLockManager.enforceIfExpired()
        if (!sessionManager.isUnlocked()) {
            val response = if (autofillPrefs.offerUnlockWhenLocked())
                buildLockedResponse(parsed, inlineSpec, parsed.passwordId?.let { FieldLabelReader.read(structure, it) } ?: "") else null
            callback.onSuccess(response)
            return
        }

        if (parsed.isBlindCardFallback && parsed.isCardForm) {
            callback.onSuccess(buildLockedResponse(parsed, inlineSpec, parsed.passwordId?.let { FieldLabelReader.read(structure, it) } ?: ""))
            return
        }

        val savedCreds = SavedCredentials.fromBundle(request.clientState)

        val etiquetaActual = parsed.passwordId?.let { FieldLabelReader.read(structure, it) } ?: ""

        serviceScope.launch {
            if (cancellationSignal.isCanceled) return@launch

            try {
                val responseBuilder = FillResponse.Builder()
                var hasDatasets     = false
                var firstEntry: VaultEntry? = null

                val enContrasenaNueva = parsed.enContrasenaNueva

                if (parsed.isLoginForm && !enContrasenaNueva) {
                    val entries = vaultRepository.observeEntriesForAutofill().first()
                    val deEstaApp = entries.filter { matchesAnyUrl(it.urls, parsed) }
                    val delMismoCampo = deEstaApp.filter { FieldLabel.sameField(it.fieldLabel, etiquetaActual) }
                    val matched = delMismoCampo.ifEmpty {
                        deEstaApp.sortedBy { rangoPorEtiqueta(it.fieldLabel, etiquetaActual) }
                    }

                    val multiVault = matched.map { it.vaultId }.toSet().size > 1
                    val vaultNames: Map<String, String> = if (multiVault)
                        vaultMetadataRepository.getAll().associate { it.id to it.name }
                    else emptyMap()

                    for (entry in matched.take(MAX_DATASETS)) {
                        val vaultName = if (multiVault) vaultNames[entry.vaultId] else null
                        val dataset = buildCredentialDataset(entry, parsed, inlineSpec, vaultName) ?: continue
                        responseBuilder.addDataset(dataset)
                        if (firstEntry == null) firstEntry = entry
                        hasDatasets = true
                    }

                    if (firstEntry != null) {
                        val cacheKey = parsed.webDomain?.lowercase()?.let { saveInfoFactory.rootDomain(it) }
                            ?: parsed.packageName
                        synchronized(recentFillsCache) {
                            recentFillsCache[cacheKey] = SavedCredentials(firstEntry)
                        }
                    }

                    if (!hasDatasets) {
                        val cacheKey = parsed.webDomain?.lowercase()?.let { saveInfoFactory.rootDomain(it) }
                            ?: parsed.packageName
                        val fallbackId = (savedCreds ?: synchronized(recentFillsCache) {
                            recentFillsCache[cacheKey]
                        })?.entryId
                        val fallbackEntry = fallbackId?.let { id -> entries.firstOrNull { it.id == id } }
                        if (fallbackEntry != null) {
                            val dataset = buildDatasetFromSaved(fallbackEntry, parsed, inlineSpec)
                            if (dataset != null) {
                                responseBuilder.addDataset(dataset)
                                hasDatasets = true
                            }
                        }
                    }
                }

                if (parsed.isCardForm) {
                    val cards = creditCardRepository.observeCardsForAutofill().first()
                    for (card in cards.take(MAX_DATASETS)) {
                        val dataset = buildCardDataset(card, parsed, inlineSpec) ?: continue
                        responseBuilder.addDataset(dataset)
                        hasDatasets = true
                    }
                }

                if (parsed.isOtpForm) {
                    val accounts = twoFactorRepository.observeAccounts().first()
                    var totpAdded = 0
                    for (account in accounts) {
                        if (totpAdded >= MAX_DATASETS) break
                        if (!account.hasTotp) continue
                        if (!matchesTotpAccount(account, parsed, vaultRepository, publicSuffixList)) continue
                        val code = TotpGenerator.generate(
                            secret    = account.secret,
                            digits    = account.digits,
                            period    = account.period,
                            algorithm = account.algorithm,
                        ) ?: continue
                        val dataset = buildTotpDataset(account, code.code, parsed, inlineSpec) ?: continue
                        responseBuilder.addDataset(dataset)
                        hasDatasets = true
                        totpAdded++
                    }
                }

                val stateBundle = when {
                    firstEntry  != null -> SavedCredentials(firstEntry).toBundle()
                    savedCreds  != null -> savedCreds.toBundle()
                    else                -> null
                }
                stateBundle?.let { responseBuilder.setClientState(it) }

                val saveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    !autofillPrefs.isSaveBlocked(targets))
                    saveInfoFactory.build(parsed) else null
                saveInfo?.let { responseBuilder.setSaveInfo(it) }
                callback.onSuccess(if (hasDatasets || saveInfo != null) responseBuilder.build() else null)

            } catch (e: Exception) {
                android.util.Log.e("KeyArmorAutofill", "onFillRequest error", e)
                callback.onSuccess(null)
            }
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        autoLockManager.enforceIfExpired()
        if (!sessionManager.isUnlocked()) { callback.onSuccess(); return }

        val structure = request.fillContexts.lastOrNull()?.structure
        if (structure == null) { callback.onSuccess(); return }

        serviceScope.launch {
            try {
                val parsed = AutofillParser.parse(structure, applicationContext)

                if (parsed.packageName == applicationContext.packageName) {
                    callback.onSuccess(); return@launch
                }

                val values = extractValues(structure)

                val idContrasena = parsed.newPasswordIds.firstOrNull { !values[it].isNullOrEmpty() }
                    ?: parsed.passwordId
                val password = idContrasena?.let { values[it] }?.takeIf { it.isNotEmpty() }
                if (password == null) { callback.onSuccess(); return@launch }

                val resolvedUsername: String
                val resolvedEmail: String
                when {
                    parsed.usernameId != null && parsed.emailId != null -> {
                        resolvedUsername = values[parsed.usernameId] ?: ""
                        resolvedEmail    = values[parsed.emailId]    ?: ""
                    }
                    parsed.usernameId != null -> {
                        resolvedUsername = values[parsed.usernameId] ?: ""
                        resolvedEmail    = ""
                    }
                    parsed.emailId != null -> {
                        resolvedUsername = ""
                        resolvedEmail    = values[parsed.emailId] ?: ""
                    }
                    else -> {
                        val loginVal = parsed.loginFieldId?.let { values[it] } ?: ""
                        if (loginVal.contains("@")) {
                            resolvedUsername = ""
                            resolvedEmail    = loginVal
                        } else {
                            resolvedUsername = loginVal
                            resolvedEmail    = ""
                        }
                    }
                }

                val url   = parsed.webDomain ?: parsed.packageName
                val title = saveInfoFactory.deriveTitle(parsed)

                val fieldLabel = idContrasena.let { FieldLabelReader.read(structure, it) }

                val existentes = vaultRepository.observeEntriesForAutofill().first()
                    .filter { matchesAnyUrl(it.urls, parsed) }
                val decision = SaveDecision.decide(
                    existing   = existentes.map {
                        SaveDecision.Existing(it.id, it.title, it.username, it.email, it.password, it.fieldLabel, it.isProtected)
                    },
                    baseTitle  = title,
                    username   = resolvedUsername,
                    email      = resolvedEmail,
                    password   = password,
                    fieldLabel = fieldLabel,
                )

                suspend fun guardar(titulo: String) = vaultRepository.saveEntry(
                    AutofillEntryFactory.create(
                        title      = titulo,
                        url        = url,
                        username   = resolvedUsername,
                        email      = resolvedEmail,
                        password   = password,
                        fieldLabel = fieldLabel,
                    )
                )

                suspend fun preguntarOGuardar(target: SaveDecision.Existing?, sugerido: String) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val token = PendingSaveStore.put(
                            PendingSave(
                                baseTitle      = title,
                                url            = url,
                                username       = resolvedUsername,
                                email          = resolvedEmail,
                                password       = password,
                                fieldLabel     = fieldLabel,
                                target         = target,
                                suggestedTitle = sugerido,
                                takenTitles    = existentes.map { it.title },
                            )
                        )
                        val pending = PendingIntent.getActivity(
                            applicationContext,
                            token.hashCode(),
                            AutofillSaveActivity.createIntent(applicationContext, token),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        callback.onSuccess(pending.intentSender)
                    } else {
                        guardar(sugerido)
                        callback.onSuccess()
                    }
                }

                when (decision) {
                    SaveDecision.Result.AlreadySaved -> callback.onSuccess()
                    is SaveDecision.Result.Create -> { guardar(decision.title); callback.onSuccess() }
                    is SaveDecision.Result.AskUpdateOrAdd ->
                        preguntarOGuardar(decision.target, decision.suggestedTitle)
                    is SaveDecision.Result.AskName ->
                        preguntarOGuardar(null, decision.suggestedTitle)
                }
            } catch (_: Exception) {
                callback.onSuccess()
            }
        }
    }

    private fun buildLockedResponse(
        parsed:     ParsedAutofillRequest,
        inlineSpec: InlinePresentationSpec?,
        fieldLabel: String = ""
    ): FillResponse? {
        val allIds = buildList {
            parsed.usernameId?.let   { add(it) }
            parsed.emailId?.let      { add(it) }
            parsed.passwordId?.let   { add(it) }
            parsed.cardNumberId?.let { add(it) }
            parsed.cardholderId?.let { add(it) }
            parsed.expiryDateId?.let { add(it) }
            parsed.cvvId?.let        { add(it) }
            parsed.otpId?.let        { add(it) }
            if (parsed.isLoginForm) {
                parsed.focusedFieldId
                    ?.takeIf { it == parsed.passwordId || it in parsed.newPasswordIds }
                    ?.let { add(it) }
                addAll(parsed.newPasswordIds)
            }
        }.distinct()
        if (allIds.isEmpty()) return null

        val unlockIntent  = AutofillUnlockActivity.createIntent(this, parsed, fieldLabel)
        val pendingIntent = PendingIntent.getActivity(
            this, REQUEST_CODE_UNLOCK, unlockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title    = getString(R.string.autofill_unlock_title)
        val subtitle = getString(R.string.autofill_unlock_subtitle)
        val presentation = buildPresentation(title, subtitle)

        val builder = FillResponse.Builder()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && inlineSpec != null) {
            val inline = buildInlinePresentation(title, subtitle, inlineSpec, pendingIntent)
            if (inline != null) {
                builder.setAuthentication(allIds.toTypedArray(), pendingIntent.intentSender, presentation, inline)
                return builder.build()
            }
        }
        builder.setAuthentication(allIds.toTypedArray(), pendingIntent.intentSender, presentation)
        return builder.build()
    }

    private fun rangoPorEtiqueta(guardada: String, actual: String): Int =
        FieldLabel.rangoPorEtiqueta(guardada, actual)

    private fun buildCredentialDataset(
        entry:      VaultEntry,
        parsed:     ParsedAutofillRequest,
        inlineSpec: InlinePresentationSpec?,
        vaultName:  String? = null
    ): Dataset? {
        val displayUser  = entry.username.ifEmpty { entry.email }
        val etiquetaGuardada = entry.fieldLabel
        val tituloVisible = listOf(" – ", " - ")
            .map { it + etiquetaGuardada }
            .firstOrNull { etiquetaGuardada.isNotBlank() && entry.title.endsWith(it, ignoreCase = true) }
            ?.let { entry.title.dropLast(it.length) }
            ?: entry.title
        val baseSubtitle = displayUser.ifEmpty {
            val etiqueta   = entry.fieldLabel
            val yaEnTitulo = etiqueta.isNotBlank() &&
                FieldLabel.normalize(tituloVisible).contains(FieldLabel.normalize(etiqueta))
            if (etiqueta.isNotBlank() && !yaEnTitulo) etiqueta else ""
        }
        val subtitle     = if (vaultName != null) "$baseSubtitle · $vaultName" else baseSubtitle
        val warning      = ageWarning(entry)
        val presentation = buildPresentation(tituloVisible, subtitle, warning)
        val builder      = Dataset.Builder(presentation)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && inlineSpec != null) {
            val pi = buildNoOpPendingIntent(entry.title.hashCode())
            val inlineSubtitle = listOfNotNull(subtitle.ifEmpty { null }, warning).joinToString(" · ")
            buildInlinePresentation(tituloVisible, inlineSubtitle, inlineSpec, pi)
                ?.let { builder.setInlinePresentation(it) }
        }

        var hasValue = false
        parsed.loginFieldId?.let {
            builder.setValue(it, AutofillValue.forText(displayUser)); hasValue = true
        }
        if (parsed.usernameId != null && parsed.emailId != null) {
            builder.setValue(parsed.usernameId, AutofillValue.forText(entry.username))
            builder.setValue(parsed.emailId,    AutofillValue.forText(entry.email))
            hasValue = true
        }
        if (parsed.allowHiddenPasswordFill) {
            parsed.passwordId?.let {
                builder.setValue(it, AutofillValue.forText(entry.password)); hasValue = true
            }
        }
        return if (hasValue) builder.build() else null
    }

    private fun buildDatasetFromSaved(
        entry:      VaultEntry,
        parsed:     ParsedAutofillRequest,
        inlineSpec: InlinePresentationSpec?
    ): Dataset? {
        val presentation = buildPresentation(entry.title, entry.username.ifEmpty { entry.email })
        val builder      = Dataset.Builder(presentation)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && inlineSpec != null) {
            val pi = buildNoOpPendingIntent(entry.title.hashCode() + 1)
            buildInlinePresentation(
                entry.title,
                entry.username.ifEmpty { entry.email },
                inlineSpec, pi
            )?.let { builder.setInlinePresentation(it) }
        }

        var hasValue = false
        val user = entry.username.ifEmpty { entry.email }
        parsed.loginFieldId?.let {
            if (user.isNotEmpty()) { builder.setValue(it, AutofillValue.forText(user)); hasValue = true }
        }
        if (parsed.usernameId != null && parsed.emailId != null) {
            builder.setValue(parsed.usernameId, AutofillValue.forText(entry.username))
            builder.setValue(parsed.emailId,    AutofillValue.forText(entry.email))
            hasValue = true
        }
        parsed.passwordId?.let {
            if (entry.password.isNotEmpty()) {
                builder.setValue(it, AutofillValue.forText(entry.password)); hasValue = true
            }
        }
        return if (hasValue) builder.build() else null
    }

    private fun buildCardDataset(
        card:       CreditCard,
        parsed:     ParsedAutofillRequest,
        inlineSpec: InlinePresentationSpec?
    ): Dataset? {
        val title        = card.issuer.ifEmpty { card.brand }.ifEmpty { getString(R.string.hub_pinned_fallback_credit_card) }
        val last4        = card.cardNumber.takeLast(4).let { if (it.isNotEmpty()) "•••• $it" else "" }
        val subtitle     = "${card.cardholder}  $last4".trim()
        val presentation = buildPresentation(title, subtitle)
        val builder      = Dataset.Builder(presentation)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && inlineSpec != null) {
            val pi = buildNoOpPendingIntent(title.hashCode())
            buildInlinePresentation(title, subtitle, inlineSpec, pi)
                ?.let { builder.setInlinePresentation(it) }
        }

        val expiryValue = if (parsed.isBlindCardFallback)
            card.expiryDate.filter { it.isDigit() }
        else
            card.expiryDate

        var hasValue = false
        parsed.cardNumberId?.takeUnless { it in parsed.selectFieldIds }
            ?.let { builder.setValue(it, AutofillValue.forText(card.cardNumber)); hasValue = true }
        parsed.cardholderId?.takeUnless { it in parsed.selectFieldIds }
            ?.let { builder.setValue(it, AutofillValue.forText(card.cardholder)); hasValue = true }
        parsed.expiryDateId?.takeUnless { it in parsed.selectFieldIds }
            ?.let { builder.setValue(it, AutofillValue.forText(expiryValue));     hasValue = true }
        parsed.cvvId?.takeUnless { it in parsed.selectFieldIds }
            ?.let { builder.setValue(it, AutofillValue.forText(card.cvv));        hasValue = true }
        return if (hasValue) builder.build() else null
    }

    private fun buildTotpDataset(
        account:    TwoFactorAccount,
        code:       String,
        parsed:     ParsedAutofillRequest,
        inlineSpec: InlinePresentationSpec?
    ): Dataset? {
        val title    = account.issuer.ifEmpty { account.name }.ifEmpty { "2FA" }
        val subtitle = account.accountLabel.ifEmpty { account.name }
        val presentation = buildPresentation(title, subtitle)
        val builder = Dataset.Builder(presentation)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && inlineSpec != null) {
            val pi = buildNoOpPendingIntent(account.id.hashCode())
            buildInlinePresentation(title, subtitle, inlineSpec, pi)
                ?.let { builder.setInlinePresentation(it) }
        }

        parsed.otpId?.let { builder.setValue(it, AutofillValue.forText(code)) }
            ?: return null
        return builder.build()
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun buildInlinePresentation(
        title:         String,
        subtitle:      String,
        spec:          InlinePresentationSpec,
        pendingIntent: PendingIntent
    ): InlinePresentation? = try {
        if (!UiVersions.getVersions(spec.style).contains(UiVersions.INLINE_UI_VERSION_1)) null
        else {
            val slice: Slice = InlineSuggestionUi
                .newContentBuilder(pendingIntent)
                .setTitle(title)
                .apply { if (subtitle.isNotEmpty()) setSubtitle(subtitle) }
                .setStartIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                .build().slice
            InlinePresentation(slice, spec, false)
        }
    } catch (_: Exception) { null }


    private fun buildNoOpPendingIntent(requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this, requestCode,
            Intent(this, KeyArmorAutofillService::class.java).apply {
                action = "com.thormakk.keyarmor.AUTOFILL_NOOP"
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun ageWarning(entry: VaultEntry): String? {
        if (entry.updatedAt <= 0L) return null
        val months = ((System.currentTimeMillis() - entry.updatedAt) /
                (30L * 24L * 60L * 60L * 1_000L)).toInt()
        return if (months >= 12) getString(R.string.autofill_age_warning, months) else null
    }

    private fun buildPresentation(
        title:   String,
        subtitle: String,
        warning: String? = null
    ): RemoteViews =
        RemoteViews(packageName, R.layout.autofill_item).apply {
            setTextViewText(R.id.autofill_text_title,    title)
            setTextViewText(R.id.autofill_text_subtitle, subtitle)
            if (warning != null) {
                setTextViewText(R.id.autofill_text_warning, warning)
                setViewVisibility(R.id.autofill_text_warning, android.view.View.VISIBLE)
            } else {
                setViewVisibility(R.id.autofill_text_warning, android.view.View.GONE)
            }
        }

    private fun matchesAnyUrl(urls: List<String>, parsed: ParsedAutofillRequest): Boolean {
        if (urls.isEmpty()) return false
        return urls.any { matchesDomainOrPackage(it, parsed) }
    }

    private fun matchesDomainOrPackage(entryUrl: String, parsed: ParsedAutofillRequest): Boolean =
        AutofillMatcher.matches(entryUrl, parsed.webDomain, parsed.packageName, publicSuffixList)

    private fun extractValues(structure: AssistStructure): Map<AutofillId, String> {
        val values = mutableMapOf<AutofillId, String>()
        for (i in 0 until structure.windowNodeCount) {
            collectValues(structure.getWindowNodeAt(i).rootViewNode, values)
        }
        return values
    }

    private fun collectValues(
        node:   AssistStructure.ViewNode,
        values: MutableMap<AutofillId, String>
    ) {
        val id    = node.autofillId
        val value = node.autofillValue
        if (id != null && value != null && value.isText) {
            values[id] = value.textValue.toString()
        }
        for (i in 0 until node.childCount) {
            collectValues(node.getChildAt(i), values)
        }
    }

    private data class SavedCredentials(
        val entryId: String,
    ) {
        constructor(entry: VaultEntry) : this(entryId = entry.id)

        fun toBundle(): Bundle = Bundle().apply {
            putString("entryId", entryId)
        }

        companion object {
            fun fromBundle(bundle: Bundle?): SavedCredentials? {
                val id = bundle?.getString("entryId") ?: return null
                return SavedCredentials(entryId = id)
            }
        }
    }

    companion object {
        private const val MAX_DATASETS        = 5
        private const val REQUEST_CODE_UNLOCK = 1001
    }
}
