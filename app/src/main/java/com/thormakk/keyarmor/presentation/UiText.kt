package com.thormakk.keyarmor.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {

    class StringResource(
        @StringRes val id: Int,
        vararg val args: Any
    ) : UiText()

    data class DynamicString(val value: String) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is StringResource -> stringResource(id, *args)
        is DynamicString  -> value
    }

    fun asString(context: Context): String = when (this) {
        is StringResource -> context.getString(id, *args)
        is DynamicString  -> value
    }
}
