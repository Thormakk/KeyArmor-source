package com.thormakk.keyarmor.presentation.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import com.thormakk.keyarmor.data.clipboard.ClipboardClearManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

object ClipboardUtil {

    private const val TAG = "ClipboardUtil"

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ClipboardClearEntryPoint {
        fun clipboardClearManager(): ClipboardClearManager
    }

    fun copySensitive(context: Context, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("keyarmor", text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        cm.setPrimaryClip(clip)
        programarBorrado(context)
    }

    private fun programarBorrado(context: Context) {
        runCatching {
            EntryPointAccessors
                .fromApplication(context.applicationContext, ClipboardClearEntryPoint::class.java)
                .clipboardClearManager()
                .scheduleClear()
        }.onFailure {
            Log.e(TAG, "No se pudo programar el borrado del portapapeles; el dato queda copiado.", it)
        }
    }
}
