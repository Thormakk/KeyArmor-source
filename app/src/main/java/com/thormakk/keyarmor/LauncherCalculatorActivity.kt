package com.thormakk.keyarmor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs
import com.thormakk.keyarmor.presentation.navigation.StartupViewModel
import com.thormakk.keyarmor.presentation.screens.calculator.CalculatorScreen
import com.thormakk.keyarmor.presentation.screens.calculator.CalculatorViewModel

class LauncherCalculatorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { false }
        enableEdgeToEdge()
        window.setBackgroundDrawableResource(android.R.color.black)

        val disguisePrefs = CalculatorDisguisePrefs(applicationContext)

        setContent {
            val viewModel: CalculatorViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        CalculatorViewModel(disguisePrefs) as T
                }
            )

            var shown by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue   = if (shown) 1f else 0.92f,
                animationSpec = tween(durationMillis = 220),
                label         = "disguiseGrowScale"
            )
            val contentAlpha by animateFloatAsState(
                targetValue   = if (shown) 1f else 0f,
                animationSpec = tween(durationMillis = 220),
                label         = "disguiseGrowAlpha"
            )
            LaunchedEffect(Unit) { shown = true }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha  = contentAlpha
                    }
            ) {
            CalculatorScreen(
                onUnlockTriggered = {
                    StartupViewModel.skipDisguiseNextStartup = true
                    startActivity(Intent(this@LauncherCalculatorActivity, MainActivity::class.java))
                },
                viewModel = viewModel
            )
            }
        }
    }
}
