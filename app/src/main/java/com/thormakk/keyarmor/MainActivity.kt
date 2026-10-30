package com.thormakk.keyarmor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.thormakk.keyarmor.data.billing.RevenueCatManager
import com.thormakk.keyarmor.data.clipboard.ClipboardClearManager
import com.thormakk.keyarmor.data.drive.DriveAutoBackupManager
import com.thormakk.keyarmor.data.prefs.BreachCheckPrefs
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.TravelSuggestionPrefs
import com.thormakk.keyarmor.notifications.NotificationHelper
import com.thormakk.keyarmor.presentation.navigation.KeyArmorNavGraph
import com.thormakk.keyarmor.presentation.navigation.StartupDestination
import com.thormakk.keyarmor.presentation.navigation.StartupViewModel
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.presentation.util.LocalScreenCaptureManager
import com.thormakk.keyarmor.presentation.util.LocalThemeTrialManager
import com.thormakk.keyarmor.security.AutoLockManager
import com.thormakk.keyarmor.security.ScreenCaptureManager
import com.thormakk.keyarmor.security.SessionManager
import com.thormakk.keyarmor.security.ThemeTrialManager
import com.thormakk.keyarmor.ui.theme.KeyArmorTheme
import com.thormakk.keyarmor.workers.BreachCheckWorker
import com.thormakk.keyarmor.workers.PasswordExpiryWorker
import com.thormakk.keyarmor.workers.TravelModeSuggestionWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {

    @Inject lateinit var clipboardClearManager: ClipboardClearManager
    @Inject lateinit var autoLockManager: AutoLockManager
    @Inject lateinit var screenCaptureManager: ScreenCaptureManager
    @Inject lateinit var themeTrialManager: ThemeTrialManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var breachCheckPrefs: BreachCheckPrefs
    @Inject lateinit var revenueCatManager: RevenueCatManager
    @Inject lateinit var driveAutoBackupManager: DriveAutoBackupManager
    @Inject lateinit var fcmPrefs: FcmPrefs
    @Inject lateinit var travelSuggestionPrefs: TravelSuggestionPrefs
    @Inject lateinit var securityPrefs: com.thormakk.keyarmor.data.prefs.SecurityPrefs

    private val startupViewModel: StartupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            startupViewModel.destination.value is StartupDestination.Loading
        }
        handleDeepLinkIntent(intent)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        lifecycleScope.launch {
            screenCaptureManager.permitActive.collect { active ->
                if (active) window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                else        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        lifecycleScope.launch {
            sessionManager.isUnlockedFlow.collect { unlocked ->
                if (!unlocked) {
                    screenCaptureManager.onVaultLocked()
                    themeTrialManager.onVaultLocked()
                }
            }
        }
        enableEdgeToEdge()

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            com.revenuecat.purchases.Purchases.logLevel = com.revenuecat.purchases.LogLevel.ERROR
            com.revenuecat.purchases.Purchases.configure(
                com.revenuecat.purchases.PurchasesConfiguration.Builder(this, "<CLAVE PUBLICA DE REVENUECAT>").build()
            )
            revenueCatManager.initialize()
        }, 500)

        lifecycleScope.launch(Dispatchers.Default) {
            if (breachCheckPrefs.isEnabled()) {
                BreachCheckWorker.schedule(this@MainActivity, breachCheckPrefs.getFrequencyDays(), replace = false)
            }

            PasswordExpiryWorker.schedule(this@MainActivity)

            if (travelSuggestionPrefs.isEnabled()) {
                TravelModeSuggestionWorker.schedule(this@MainActivity)
            }

            val migrationPrefs = getSharedPreferences("keyarmor_migration", MODE_PRIVATE)
            if (!migrationPrefs.getBoolean("expiry_notif_targetid_fix_v1", false)) {
                PasswordExpiryWorker.runNow(this@MainActivity)
                migrationPrefs.edit { putBoolean("expiry_notif_targetid_fix_v1", true) }
            }
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                isAppForegrounded = false
                autoLockManager.onAppBackground()
                driveAutoBackupManager.triggerOnAppBackground()
                clipboardClearManager.onActivityBackground()
                screenCaptureManager.revoke()
            }
            override fun onStart(owner: LifecycleOwner) {
                isAppForegrounded = true
                autoLockManager.onAppForeground()
                clipboardClearManager.onActivityForeground()
                screenCaptureManager.checkExpiry()
                themeTrialManager.checkExpiry()
            }
        })

        setContent {
            val startupVm: StartupViewModel = hiltViewModel()
            val themeId     by startupVm.themeId.collectAsStateWithLifecycle()
            val accentArgb  by startupVm.accentColor.collectAsStateWithLifecycle()
            KeyArmorTheme(themeId = themeId, accentArgb = accentArgb) {
                CompositionLocalProvider(
                    LocalClipboardClearManager provides clipboardClearManager,
                    LocalScreenCaptureManager provides screenCaptureManager,
                    LocalThemeTrialManager provides themeTrialManager
                ) {
                    PedirPermisoDeNotificaciones()
                    KeyArmorNavGraph(autoLockManager = autoLockManager)
                }
            }
        }
    }

    @Composable
    private fun PedirPermisoDeNotificaciones() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val desbloqueada by sessionManager.isUnlockedFlow.collectAsStateWithLifecycle()

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {   }

        LaunchedEffect(desbloqueada) {
            if (!desbloqueada) return@LaunchedEffect
            if (securityPrefs.wasNotificationPermissionAsked()) return@LaunchedEffect
            val yaConcedido = ContextCompat.checkSelfPermission(
                this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            securityPrefs.setNotificationPermissionAsked()
            if (!yaConcedido) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            clipboardClearManager.payPendingClearIfAny()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent) {
        val vieneDeNotificacionFamiliar =
            intent.hasExtra(NotificationHelper.EXTRA_FAMILY_ADMIN_EMAIL) ||
                intent.hasExtra(NotificationHelper.EXTRA_FAMILY_INVITE_TOKEN)
        val pendingAdmin  = fcmPrefs.pendingFamilyInviteAdminEmail
        val pendingToken  = fcmPrefs.pendingFamilyInviteToken
        if (pendingAdmin != null && pendingToken != null) {
            pendingFamilyConsentArgs.value = pendingAdmin to pendingToken
            return
        }
        if (vieneDeNotificacionFamiliar) return

        val fromNotification  = intent.getBooleanExtra(NotificationHelper.EXTRA_FROM_NOTIFICATION, false)
        val entryId           = intent.getStringExtra(NotificationHelper.EXTRA_ENTRY_ID)
        val reminderId        = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_ID)
        val travelSuggestion  = intent.getBooleanExtra(NotificationHelper.EXTRA_TRAVEL_SUGGESTION, false)

        if (entryId != null) {
            pendingDeepLinkEntryId.value = entryId
            return
        }
        if (reminderId != null) {
            pendingDeepLinkReminderId.value = reminderId
            return
        }
        if (travelSuggestion) {
            pendingTravelSuggestion.value = true
            return
        }
        if (fromNotification) {
            pendingNotificationHint.value = true
        }
    }

    companion object {
        val pendingDeepLinkEntryId    = MutableStateFlow<String?>(null)
        val pendingDeepLinkReminderId = MutableStateFlow<String?>(null)
        val pendingNotificationHint   = MutableStateFlow(false)
        val pendingTravelSuggestion   = MutableStateFlow(false)
        val pendingFamilyConsentArgs  = MutableStateFlow<Pair<String, String>?>(null)
        @Volatile
        var isAppForegrounded: Boolean = false
    }
}
