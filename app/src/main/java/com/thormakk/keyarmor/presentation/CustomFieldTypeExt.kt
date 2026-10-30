package com.thormakk.keyarmor.presentation

import androidx.annotation.StringRes
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.CustomFieldType

@StringRes
fun CustomFieldType.labelRes(): Int = when (this) {
    CustomFieldType.TEXT     -> R.string.field_type_text
    CustomFieldType.NUMBER   -> R.string.field_type_number
    CustomFieldType.PASSWORD -> R.string.field_type_password
    CustomFieldType.DATE     -> R.string.field_type_date
    CustomFieldType.EMAIL    -> R.string.field_type_email
    CustomFieldType.URL      -> R.string.field_type_url
    CustomFieldType.WALLET   -> R.string.field_type_wallet
    CustomFieldType.PHONE    -> R.string.field_type_phone
    CustomFieldType.PIN      -> R.string.field_type_pin
    CustomFieldType.CODE     -> R.string.field_type_code
    CustomFieldType.NOTE     -> R.string.field_type_note
}

@StringRes
fun CustomFieldType.formatHintRes(): Int? = when (this) {
    CustomFieldType.DATE   -> R.string.field_hint_date
    CustomFieldType.EMAIL  -> R.string.field_hint_email
    CustomFieldType.URL    -> R.string.field_hint_url
    CustomFieldType.WALLET -> R.string.field_hint_wallet
    CustomFieldType.PHONE  -> R.string.field_hint_phone
    else                   -> null
}
