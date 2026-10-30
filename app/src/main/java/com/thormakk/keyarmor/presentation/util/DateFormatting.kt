package com.thormakk.keyarmor.presentation.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatDateLocalized(millis: Long, locale: Locale, utc: Boolean = false): String {
    val fmt = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
    if (utc) fmt.timeZone = TimeZone.getTimeZone("UTC")
    return fmt.format(Date(millis))
}
