package com.thormakk.keyarmor.autofill

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.service.autofill.SaveInfo
import androidx.annotation.RequiresApi
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.BlockTarget
import java.net.URI

class AutofillSaveInfoFactory(
    private val context: Context,
    private val publicSuffixList: PublicSuffixList,
) {

    fun rootDomain(host: String): String =
        publicSuffixList.getRegistrableDomain(host) ?: run {
            val parts = host.split(".")
            if (parts.size >= 2) parts.takeLast(2).joinToString(".") else host
        }

    fun extractHost(raw: String): String? {
        val withScheme = if (raw.contains("://")) raw else "https://$raw"
        return try {
            URI(withScheme).host?.removePrefix("www.")
        } catch (_: Exception) {
            raw.removePrefix("www.").substringBefore("/")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun build(parsed: ParsedAutofillRequest): SaveInfo? {
        val passwordIds = (parsed.newPasswordIds + listOfNotNull(parsed.passwordId)).distinct()
        if (passwordIds.isEmpty()) return null
        val optionalIds = listOfNotNull(parsed.loginFieldId).toTypedArray()
        @Suppress("DEPRECATION")
        val builder = SaveInfo.Builder(
            SaveInfo.SAVE_DATA_TYPE_PASSWORD,
            passwordIds.toTypedArray()
        ).setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
        if (optionalIds.isNotEmpty()) builder.setOptionalIds(optionalIds)
        builder.setDescription(context.getString(R.string.autofill_save_description, deriveTitle(parsed)))
        addNeverAction(builder, blockTargets(parsed).first())
        return builder.build()
    }

    fun blockTargets(parsed: ParsedAutofillRequest): List<BlockTarget> {
        val site = parsed.webDomain
            ?.let { extractHost(it) }
            ?.takeIf { it.isNotBlank() }
            ?.let { rootDomain(it) }
        return listOfNotNull(
            site?.let { BlockTarget.site(it) },
            BlockTarget.app(parsed.packageName),
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addNeverAction(builder: SaveInfo.Builder, target: BlockTarget) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        if (target.value.isBlank()) return
        val intent = AutofillBlockActivity.createIntent(context, target)
        val pending = PendingIntent.getActivity(
            context,
            REQUEST_CODE_BLOCK + target.key.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        builder.setNegativeAction(SaveInfo.NEGATIVE_BUTTON_STYLE_NEVER, pending.intentSender)
    }

    fun deriveTitle(parsed: ParsedAutofillRequest): String {
        parsed.webDomain
            ?.let { extractHost(it) }
            ?.takeIf { it.isNotBlank() }
            ?.let { AutofillTitle.fromRegistrableDomain(rootDomain(it)) }
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        return AutofillTitle.fromPackageName(parsed.packageName)
            .ifEmpty { parsed.webDomain ?: parsed.packageName }
    }

    companion object {
        private const val REQUEST_CODE_BLOCK = 2000
    }
}
