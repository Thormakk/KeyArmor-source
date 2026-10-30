package com.thormakk.keyarmor.widget

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.clipboard.ClipboardClearManager
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CopyPasswordActivity : ComponentActivity() {

    @Inject lateinit var clipboardClearManager: ClipboardClearManager

    private var password: String? = null
    private var copied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, -1)
        if (appWidgetId < 0) { finish(); return }
        lifecycleScope.launch {
            val ctx = applicationContext
            val glanceId = runCatching { GlanceAppWidgetManager(ctx).getGlanceIdBy(appWidgetId) }.getOrNull()
            if (glanceId == null) { finish(); return@launch }
            val prefs = getAppWidgetState(ctx, PreferencesGlanceStateDefinition, glanceId)
            password = prefs[KeyArmorWidget.WIDGET_PASSWORD_KEY]
            if (password == null) { finish(); return@launch }
            copiarSiHayFoco()
        }
    }

    override fun onStart() {
        super.onStart()
        clipboardClearManager.onActivityForeground()
    }

    override fun onStop() {
        super.onStop()
        clipboardClearManager.onActivityBackground()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) copiarSiHayFoco()
    }

    private fun copiarSiHayFoco() {
        if (copied || !hasWindowFocus()) return
        val p = password ?: return
        copied = true
        ClipboardUtil.copySensitive(this, p)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val ctx = contextoDelIdioma(applicationContext)
            Toast.makeText(ctx, ctx.getString(R.string.widget_copied), Toast.LENGTH_SHORT).show()
        }
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    companion object {
        const val EXTRA_APPWIDGET_ID = "appWidgetId"
    }
}
