package com.thormakk.keyarmor.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.thormakk.keyarmor.data.clipboard.ClipboardClearManager
import com.thormakk.keyarmor.presentation.screens.generator.PasswordGeneratorScreen
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.ui.theme.KeyArmorTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PasswordGeneratorActivity : ComponentActivity() {

    @Inject lateinit var clipboardClearManager: ClipboardClearManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            KeyArmorTheme {
                CompositionLocalProvider(
                    LocalClipboardClearManager provides clipboardClearManager
                ) {
                    PasswordGeneratorScreen(
                        fromEntry     = false,
                        onBack        = { finish() },
                        onUsePassword = {   }
                    )
                }
            }
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
        if (hasFocus) {
            clipboardClearManager.payPendingClearIfAny()
        }
    }
}
