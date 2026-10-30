@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
package com.thormakk.keyarmor.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.withResumed
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thormakk.keyarmor.MainActivity
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.notifications.AppConsentViewModel
import com.thormakk.keyarmor.notifications.AppNotificationViewModel
import com.thormakk.keyarmor.presentation.coachmark.CoachMarkOverlay
import com.thormakk.keyarmor.presentation.coachmark.CoachMarkViewModel
import com.thormakk.keyarmor.presentation.coachmark.LocalCoachMarks
import com.thormakk.keyarmor.presentation.components.BottomNavTab
import com.thormakk.keyarmor.presentation.components.VaultBottomBar
import com.thormakk.keyarmor.presentation.screens.archive.ArchiveScreen
import com.thormakk.keyarmor.presentation.screens.attachments.AttachmentsScreen
import com.thormakk.keyarmor.presentation.screens.audit.AuditScreen
import com.thormakk.keyarmor.presentation.screens.auth.LockScreen
import com.thormakk.keyarmor.presentation.screens.auth.LoginScreen
import com.thormakk.keyarmor.presentation.screens.auth.RegisterScreen
import com.thormakk.keyarmor.presentation.screens.calculator.CalculatorScreen
import com.thormakk.keyarmor.presentation.screens.calculatordisguise.CalculatorDisguiseScreen
import com.thormakk.keyarmor.presentation.screens.bank.AddEditBankAccountScreen
import com.thormakk.keyarmor.presentation.screens.bank.BankAccountDetailScreen
import com.thormakk.keyarmor.presentation.screens.bank.BankAccountsScreen
import com.thormakk.keyarmor.presentation.screens.onboarding.ConnectGoogleScreen
import com.thormakk.keyarmor.presentation.screens.onboarding.OnboardingWelcomeScreen
import com.thormakk.keyarmor.presentation.screens.creditcard.AddEditCreditCardScreen
import com.thormakk.keyarmor.presentation.screens.creditcard.CreditCardDetailScreen
import com.thormakk.keyarmor.presentation.screens.creditcard.CreditCardsScreen
import com.thormakk.keyarmor.presentation.screens.crypto.AddEditCryptoWalletScreen
import com.thormakk.keyarmor.presentation.screens.crypto.CryptoWalletDetailScreen
import com.thormakk.keyarmor.presentation.screens.crypto.CryptoWalletsScreen
import com.thormakk.keyarmor.presentation.screens.custom.AddEditCustomEntryScreen
import com.thormakk.keyarmor.presentation.screens.custom.CustomEntriesScreen
import com.thormakk.keyarmor.presentation.screens.custom.CustomEntryDetailScreen
import com.thormakk.keyarmor.presentation.screens.drivesync.DriveSyncScreen
import com.thormakk.keyarmor.presentation.screens.family.FamilyMemberConsentScreen
import com.thormakk.keyarmor.presentation.screens.family.FamilyScreen
import com.thormakk.keyarmor.presentation.screens.generator.PasswordGeneratorScreen
import com.thormakk.keyarmor.presentation.screens.hub.LegalDocScreen
import com.thormakk.keyarmor.presentation.screens.identity.AddEditIdentityDocumentScreen
import com.thormakk.keyarmor.presentation.screens.identity.IdentityDocumentDetailScreen
import com.thormakk.keyarmor.presentation.screens.identity.IdentityDocumentsScreen
import com.thormakk.keyarmor.presentation.screens.importexport.ImportExportScreen
import com.thormakk.keyarmor.presentation.screens.importexport.PartialExportScreen
import com.thormakk.keyarmor.presentation.screens.inheritance.HeirConsentScreen
import com.thormakk.keyarmor.presentation.screens.inheritance.InheritanceScreen
import com.thormakk.keyarmor.presentation.screens.language.LanguagePickerScreen
import com.thormakk.keyarmor.presentation.screens.language.LanguageScreen
import com.thormakk.keyarmor.presentation.screens.notes.AddEditSecureNoteScreen
import com.thormakk.keyarmor.presentation.screens.notes.SecureNoteDetailScreen
import com.thormakk.keyarmor.presentation.screens.notes.SecureNoteHistoryScreen
import com.thormakk.keyarmor.presentation.screens.notes.SecureNotesScreen
import com.thormakk.keyarmor.presentation.screens.notifications.NotificationsScreen
import com.thormakk.keyarmor.presentation.screens.panicpin.PanicPinScreen
import com.thormakk.keyarmor.presentation.screens.passkeys.PasskeyDetailScreen
import com.thormakk.keyarmor.presentation.screens.passkeys.PasskeyEditScreen
import com.thormakk.keyarmor.presentation.screens.passkeys.PasskeysScreen
import com.thormakk.keyarmor.presentation.screens.plans.MyPlanScreen
import com.thormakk.keyarmor.presentation.screens.plans.PlansScreen
import com.thormakk.keyarmor.presentation.screens.reminders.ReminderAddEditScreen
import com.thormakk.keyarmor.presentation.screens.reminders.RemindersScreen
import com.thormakk.keyarmor.presentation.screens.search.RecentEntriesViewModel
import com.thormakk.keyarmor.presentation.screens.search.SearchScreen
import com.thormakk.keyarmor.presentation.screens.donate.DonateScreen
import com.thormakk.keyarmor.presentation.screens.settings.ChangeMasterPasswordScreen
import com.thormakk.keyarmor.presentation.screens.settings.AutofillSettingsScreen
import com.thormakk.keyarmor.presentation.screens.settings.HiddenEntriesScreen
import com.thormakk.keyarmor.presentation.screens.settings.SettingsScreen
import com.thormakk.keyarmor.presentation.screens.settings.ThemeSelectorScreen
import com.thormakk.keyarmor.presentation.screens.share.ReceiveEntryScreen
import com.thormakk.keyarmor.presentation.screens.share.ShareEntryScreen
import com.thormakk.keyarmor.presentation.screens.timemachine.SnapshotPreviewScreen
import com.thormakk.keyarmor.presentation.screens.timemachine.TimeMachineScreen
import com.thormakk.keyarmor.presentation.screens.trash.TrashScreen
import com.thormakk.keyarmor.presentation.screens.travelmode.TravelModeScreen
import com.thormakk.keyarmor.presentation.screens.voicedictation.VoiceDictationScreen
import com.thormakk.keyarmor.presentation.screens.twofactor.AddEditTwoFactorScreen
import com.thormakk.keyarmor.presentation.screens.twofactor.TwoFactorDetailScreen
import com.thormakk.keyarmor.presentation.screens.twofactor.TwoFactorScreen
import com.thormakk.keyarmor.presentation.screens.vault.AddEditEntryScreen
import com.thormakk.keyarmor.presentation.screens.vault.PasswordDetailScreen
import com.thormakk.keyarmor.presentation.screens.vault.PasswordHistoryScreen
import com.thormakk.keyarmor.presentation.screens.vault.StrengthFilter
import com.thormakk.keyarmor.presentation.screens.vault.VaultScreen
import com.thormakk.keyarmor.presentation.screens.vault.VaultViewModel
import com.thormakk.keyarmor.presentation.screens.vaulthub.VaultHubScreen
import com.thormakk.keyarmor.presentation.screens.vaultmanagement.VaultManagementScreen
import com.thormakk.keyarmor.presentation.screens.vehicle.AddEditVehicleScreen
import com.thormakk.keyarmor.presentation.screens.vehicle.VehicleDetailScreen
import com.thormakk.keyarmor.presentation.screens.vehicle.VehiclesScreen
import com.thormakk.keyarmor.presentation.util.BiometricSessionCache
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.security.BusquedaEnCurso
import com.thormakk.keyarmor.presentation.util.LocalSearchQuery
import com.thormakk.keyarmor.security.AutoLockManager
import com.thormakk.keyarmor.ui.theme.LocalIsBlackChrome
import kotlinx.coroutines.launch

@Composable
fun KeyArmorNavGraph(
    navController: NavHostController = rememberNavController(),
    startupViewModel: StartupViewModel = hiltViewModel(),
    autoLockManager: AutoLockManager? = null
) {
    val destination by startupViewModel.destination.collectAsStateWithLifecycle()

    if (destination is StartupDestination.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val startRoute = when (destination) {
        StartupDestination.Language   -> Screen.Language.route
        StartupDestination.Login      -> Screen.Login.route
        StartupDestination.Lock       -> Screen.Lock.route
        StartupDestination.Calculator -> Screen.Calculator.route
        else                          -> Screen.Language.route
    }

    val autoLockVm: AutoLockNavViewModel = hiltViewModel()
    val recentEntriesVm: RecentEntriesViewModel = hiltViewModel()
    val isSessionActive by autoLockVm.isSessionActive.collectAsStateWithLifecycle()

    val coachMarkVm: CoachMarkViewModel = hiltViewModel()
    val coachMarks = coachMarkVm.controller

    val coercionGuardVm: CoercionGuardNavViewModel = hiltViewModel()
    val coercionActive by coercionGuardVm.coercionActive.collectAsStateWithLifecycle()
    val coercionBlockedRoutes = remember {
        setOf(
            Screen.DriveSync.route,
            Screen.BackupKey.route,
            Screen.ImportExport.route,
            Screen.PartialExport.route,
            Screen.TimeMachine.route,
            Screen.SnapshotPreview.route,
            Screen.PanicPin.route,
            Screen.DigitalInheritance.route,
            Screen.HeirConsent.route,
            Screen.FamilyAdmin.route,
            Screen.FamilyMemberConsent.route,
            Screen.MyPlan.route,
            Screen.ChangeMasterPassword.route,
            Screen.HiddenEntries.route,
            Screen.Notifications.route
        )
    }

    LaunchedEffect(isSessionActive) {
        if (!isSessionActive) {
            BiometricSessionCache.invalidateAll()
            val current = navController.currentDestination?.route
            val authRoutes = setOf(Screen.Lock.route, Screen.Login.route, Screen.Register.route, Screen.Language.route, Screen.Calculator.route)
            if (current != null && current !in authRoutes) {
                navController.navigate(Screen.Lock.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            autoLockManager?.startInactivityTimer()
        }
    }

    val coercionBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(coercionActive, coercionBackStackEntry) {
        val current = coercionBackStackEntry?.destination?.route
        if (coercionActive && current in coercionBlockedRoutes) {
            if (!navController.popBackStack()) {
                navController.navigate(Screen.VaultHub.route) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    val slideEnterSpec    = tween<IntOffset>(250, easing = FastOutSlowInEasing)
    val slideExitSpec     = tween<IntOffset>(250, easing = FastOutLinearInEasing)
    val slidePopEnterSpec = tween<IntOffset>(250, easing = LinearOutSlowInEasing)
    val slidePopExitSpec  = tween<IntOffset>(250, easing = FastOutLinearInEasing)

    val tabFadeExitSpec  = tween<Float>(160, easing = FastOutLinearInEasing)
    val tabFadeEnterSpec = tween<Float>(200, easing = FastOutSlowInEasing)
    val tabFadeEnter: EnterTransition = fadeIn(tabFadeEnterSpec) + scaleIn(tabFadeEnterSpec, initialScale = 0.97f)
    val tabFadeExit:  ExitTransition  = fadeOut(tabFadeExitSpec) + scaleOut(tabFadeExitSpec, targetScale = 1.02f)

    val modalEnter:    EnterTransition = slideInHorizontally(tween(300, easing = FastOutSlowInEasing))   { it }  + fadeIn(tween(300, easing = FastOutSlowInEasing))
    val modalExit:     ExitTransition  = slideOutHorizontally(tween(220, easing = FastOutLinearInEasing)) { -it } + fadeOut(tween(220, easing = FastOutLinearInEasing))
    val modalPopEnter: EnterTransition = slideInHorizontally(tween(300, easing = LinearOutSlowInEasing)) { -it } + fadeIn(tween(300, easing = LinearOutSlowInEasing))
    val modalPopExit:  ExitTransition  = slideOutHorizontally(tween(220, easing = FastOutLinearInEasing)) { it }  + fadeOut(tween(220, easing = FastOutLinearInEasing))

    val navScope = rememberCoroutineScope()
    fun navigateShared(route: String) {
        navScope.launch {
            withFrameNanos {}
            withFrameNanos {}
            navController.navigate(route)
        }
    }

    val tabRoutePrefixes = setOf(
        Screen.VaultHub.route,
        Screen.Search.route,
        Screen.Generator.route,
        "settings"
    )
    fun String?.isTabRoute() = this != null && tabRoutePrefixes.any { startsWith(it) }

    val modalRoutes = setOf(
        Screen.Lock.route,
        Screen.Plans.route,
        Screen.Archive.route,
        Screen.Trash.route,
        Screen.PanicPin.route,
        Screen.TravelMode.route,
        Screen.ShareEntry.route,
        Screen.ReceiveEntry.route,
        Screen.Notifications.route,
        Screen.Reminders.route,
        Screen.ReminderAddEdit.route
    )
    fun String?.isModalRoute() = this != null && this in modalRoutes

    val vaultSectionRoutes = setOf(
        Screen.BankAccounts.route,
        Screen.CreditCards.route,
        Screen.IdentityDocuments.route,
        Screen.Vehicles.route,
        Screen.SecureNotes.route,
        Screen.CustomEntries.route,
        Screen.CryptoWallets.route,
        Screen.TwoFactor.route,
        Screen.Reminders.route,
        Screen.Passkeys.route,
        Screen.Archive.route,
        Screen.Trash.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val activeTab = when {
        currentRoute == Screen.VaultHub.route               -> BottomNavTab.VAULT
        currentRoute?.startsWith("vault") == true           -> BottomNavTab.VAULT
        currentRoute in vaultSectionRoutes                  -> BottomNavTab.VAULT
        currentRoute == Screen.Search.route                 -> BottomNavTab.SEARCH
        currentRoute == Screen.Generator.route              -> BottomNavTab.GENERATOR
        currentRoute?.startsWith("settings") == true        -> BottomNavTab.SETTINGS
        else                                                -> BottomNavTab.NONE
    }

    LaunchedEffect(currentRoute) {
        coachMarks.onScreenChanged(currentRoute?.substringBefore("?")?.substringBefore("/"))
    }

    val authRoutes = remember {
        setOf(Screen.Lock.route, Screen.Login.route, Screen.Register.route, Screen.Language.route)
    }

    val pendingDeepLinkId by MainActivity.pendingDeepLinkEntryId.collectAsStateWithLifecycle()
    LaunchedEffect(pendingDeepLinkId, currentRoute) {
        val id = pendingDeepLinkId ?: return@LaunchedEffect
        if (currentRoute != null && currentRoute !in authRoutes) {
            MainActivity.pendingDeepLinkEntryId.value = null
            navController.navigate(Screen.PasswordDetail.createRoute(id)) {
                launchSingleTop = true
            }
        }
    }

    val pendingDeepLinkReminderId by MainActivity.pendingDeepLinkReminderId.collectAsStateWithLifecycle()
    LaunchedEffect(pendingDeepLinkReminderId, currentRoute) {
        val id = pendingDeepLinkReminderId ?: return@LaunchedEffect
        if (currentRoute != null && currentRoute !in authRoutes) {
            MainActivity.pendingDeepLinkReminderId.value = null
            navController.navigate(Screen.ReminderAddEdit.createRoute(id)) {
                launchSingleTop = true
            }
        }
    }

    val pendingFamilyConsent by MainActivity.pendingFamilyConsentArgs.collectAsStateWithLifecycle()
    LaunchedEffect(pendingFamilyConsent, currentRoute) {
        val args = pendingFamilyConsent ?: return@LaunchedEffect
        if (currentRoute != null && currentRoute !in authRoutes) {
            MainActivity.pendingFamilyConsentArgs.value = null
            navController.navigate(Screen.FamilyMemberConsent.createRoute(args.first, args.second)) {
                launchSingleTop = true
            }
        }
    }

    val pendingTravelSuggestion by MainActivity.pendingTravelSuggestion.collectAsStateWithLifecycle()
    LaunchedEffect(pendingTravelSuggestion, currentRoute) {
        if (!pendingTravelSuggestion) return@LaunchedEffect
        if (currentRoute != null && currentRoute !in authRoutes) {
            MainActivity.pendingTravelSuggestion.value = false
            navController.navigate(Screen.TravelMode.route) {
                launchSingleTop = true
            }
        }
    }

    val pendingNotifHint by MainActivity.pendingNotificationHint.collectAsStateWithLifecycle()
    LaunchedEffect(pendingNotifHint, currentRoute, coercionActive) {
        if (!pendingNotifHint) return@LaunchedEffect
        if (coercionActive) {
            MainActivity.pendingNotificationHint.value = false
            return@LaunchedEffect
        }
        if (currentRoute != null && currentRoute !in authRoutes) {
            MainActivity.pendingNotificationHint.value = false
            navController.navigate(Screen.Notifications.route) {
                launchSingleTop = true
            }
        }
    }

    val appNotifVm: AppNotificationViewModel = hiltViewModel()
    val globalSnackbarState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        appNotifVm.notifications.collect { notif ->
            globalSnackbarState.showSnackbar(
                message  = notif.message,
                duration = notif.duration,
            )
        }
    }

    val appConsentVm: AppConsentViewModel = hiltViewModel()
    val consentLifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        appConsentVm.events.collect {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != Screen.HeirConsent.route) {
                consentLifecycleOwner.lifecycle.withResumed {
                    navController.navigate(Screen.HeirConsent.route)
                }
            }
        }
    }

    val isBlackChrome = LocalIsBlackChrome.current
    CompositionLocalProvider(LocalCoachMarks provides coachMarks) {
    Box(Modifier.fillMaxSize()) {
    Scaffold(
        containerColor       = if (isBlackChrome) Color.Black else MaterialTheme.colorScheme.background,
        contentWindowInsets  = WindowInsets(0),
        snackbarHost         = { SnackbarHost(globalSnackbarState, modifier = Modifier.padding(bottom = 40.dp)) },
        bottomBar = {
            if (activeTab != BottomNavTab.NONE) {
                VaultBottomBar(
                    activeTab          = activeTab,
                    vaultTabClickable  = activeTab == BottomNavTab.VAULT && currentRoute != Screen.VaultHub.route,
                    onVaultHubClick  = {
                        navController.navigate(Screen.VaultHub.route) {
                            popUpTo(Screen.VaultHub.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onSearchClick    = {
                        navController.navigate(Screen.Search.route) {
                            popUpTo(Screen.VaultHub.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onGeneratorClick = {
                        navController.navigate(Screen.Generator.route) {
                            popUpTo(Screen.VaultHub.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onSettingsClick  = {
                        navController.navigate(Screen.Settings.PLAIN_ROUTE) {
                            popUpTo(Screen.VaultHub.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { shellPadding ->

    CompositionLocalProvider(LocalAutoLockManager provides autoLockManager) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(shellPadding)
            .onFocusChanged { if (it.hasFocus) autoLockManager?.recordInteraction() }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    autoLockManager?.recordInteraction()
                }
            }
    ) {

    SharedTransitionLayout {
    val sharedTransitionScope = this

    NavHost(
        navController      = navController,
        startDestination   = startRoute,
        enterTransition    = {
            val from = initialState.destination.route
            val to   = targetState.destination.route
            when {
                from.isTabRoute() && to.isTabRoute()     -> tabFadeEnter
                to.isModalRoute() || from.isModalRoute() -> modalEnter
                else -> slideInHorizontally(initialOffsetX = { it }, animationSpec = slideEnterSpec)
            }
        },
        exitTransition     = {
            val from = initialState.destination.route
            val to   = targetState.destination.route
            when {
                from.isTabRoute() && to.isTabRoute()     -> tabFadeExit
                to.isModalRoute() || from.isModalRoute() -> modalExit
                else -> slideOutHorizontally(targetOffsetX = { -it }, animationSpec = slideExitSpec)
            }
        },
        popEnterTransition = {
            val from = initialState.destination.route
            val to   = targetState.destination.route
            when {
                from.isTabRoute() && to.isTabRoute()     -> tabFadeEnter
                to.isModalRoute() || from.isModalRoute() -> modalPopEnter
                else -> slideInHorizontally(initialOffsetX = { -it }, animationSpec = slidePopEnterSpec)
            }
        },
        popExitTransition  = {
            val from = initialState.destination.route
            val to   = targetState.destination.route
            when {
                from.isTabRoute() && to.isTabRoute()     -> tabFadeExit
                to.isModalRoute() || from.isModalRoute() -> modalPopExit
                else -> slideOutHorizontally(targetOffsetX = { it }, animationSpec = slidePopExitSpec)
            }
        }
    ) {
        composable(Screen.Language.route) {
            LanguageScreen(
                onLanguageSelected = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Language.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.ConnectGoogle.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ConnectGoogle.route) {
            ConnectGoogleScreen(
                onContinue = { skipTutorial ->
                    val destino = if (skipTutorial) Screen.VaultHub.route else Screen.OnboardingWelcome.route
                    navController.navigate(destino) {
                        popUpTo(Screen.ConnectGoogle.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.OnboardingWelcome.route) {
            OnboardingWelcomeScreen(
                onFinish = {
                    navController.navigate(Screen.VaultHub.route) {
                        popUpTo(Screen.OnboardingWelcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.VaultHub.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Lock.route) {
            LockScreen(
                onUnlocked = {
                    navController.navigate(Screen.VaultHub.route) {
                        popUpTo(Screen.Lock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Calculator.route) {
            CalculatorScreen(
                onUnlockTriggered = { navController.navigate(Screen.Lock.route) }
            )
        }

        composable(Screen.VaultHub.route) {
            VaultHubScreen(
                onPasswordsClick        = { navController.navigate(Screen.Vault.PLAIN_ROUTE) },
                onPasswordStrengthClick = { filter ->
                    navController.navigate(Screen.Vault.withFilter(filter)) {
                        launchSingleTop = true
                    }
                },
                onBankAccountsClick  = { navController.navigate(Screen.BankAccounts.route) },
                onCustomEntriesClick = { navController.navigate(Screen.CustomEntries.route) },
                onSecureNotesClick   = { navController.navigate(Screen.SecureNotes.route) },
                onCreditCardsClick   = { navController.navigate(Screen.CreditCards.route) },
                onRemindersClick     = { navController.navigate(Screen.Reminders.route) },
                onArchiveClick       = { navController.navigate(Screen.Archive.PLAIN_ROUTE) },
                onTrashClick         = { navController.navigate(Screen.Trash.PLAIN_ROUTE) },
                onTwoFactorClick     = { navController.navigate(Screen.TwoFactor.route) },
                onCryptoWalletsClick = { navController.navigate(Screen.CryptoWallets.route) },
                onPlansClick         = { navController.navigate(Screen.Plans.route) },
                onManageVaultsClick  = { navController.navigate(Screen.VaultManagement.route) },
                onPasskeysClick          = { navController.navigate(Screen.Passkeys.route) },
                onIdentityDocumentsClick = { navController.navigate(Screen.IdentityDocuments.route) },
                onVehiclesClick          = { navController.navigate(Screen.Vehicles.route) },
                onAddPasswordClick         = { navController.navigate(Screen.AddEditEntry.createRoute("new")) },
                onAddTwoFactorClick        = { navController.navigate(Screen.AddEditTwoFactor.createRoute("new")) },
                onAddCreditCardClick       = { navController.navigate(Screen.AddEditCreditCard.createRoute("new")) },
                onAddBankAccountClick      = { navController.navigate(Screen.AddEditBankAccount.createRoute("new")) },
                onAddCryptoWalletClick     = { navController.navigate(Screen.AddEditCryptoWallet.createRoute("new")) },
                onAddReminderClick         = { navController.navigate(Screen.ReminderAddEdit.createRoute(Screen.ReminderAddEdit.NEW_ID)) },
                onAddSecureNoteClick       = { navController.navigate(Screen.AddEditSecureNote.createRoute("new")) },
                onAddCustomEntryClick      = { navController.navigate(Screen.AddEditCustomEntry.createRoute("new")) },
                onAddIdentityDocumentClick = { navController.navigate(Screen.AddEditIdentityDocument.createRoute("new")) },
                onAddVehicleClick          = { navController.navigate(Screen.AddEditVehicle.createRoute("new")) },
                onNotificationsClick       = { navController.navigate(Screen.Notifications.route) },
                onNavigateToDriveSync      = { navController.navigate(Screen.DriveSync.route) },
                onPinnedEntryClick         = { id, type ->
                    recentEntriesVm.recordAccess(id, type)
                    when (type) {
                        HubEntryType.PASSWORD          -> navigateShared(Screen.PasswordDetail.createRoute(id))
                        HubEntryType.BANK_ACCOUNT      -> navigateShared(Screen.BankAccountDetail.createRoute(id))
                        HubEntryType.CREDIT_CARD       -> navigateShared(Screen.CreditCardDetail.createRoute(id))
                        HubEntryType.CRYPTO_WALLET     -> navigateShared(Screen.CryptoWalletDetail.createRoute(id))
                        HubEntryType.SECURE_NOTE       -> navController.navigate(Screen.SecureNoteDetail.createRoute(id))
                        HubEntryType.CUSTOM_ENTRY      -> navController.navigate(Screen.CustomEntryDetail.createRoute(id))
                        HubEntryType.IDENTITY_DOCUMENT -> navController.navigate(Screen.IdentityDocumentDetail.createRoute(id))
                        HubEntryType.VEHICLE           -> navController.navigate(Screen.VehicleDetail.createRoute(id))
                        HubEntryType.TWO_FACTOR        -> navController.navigate(Screen.TwoFactorDetail.createRoute(id))
                    }
                },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(Screen.HeirConsent.route) {
            HeirConsentScreen(
                onDone = { navController.popBackStack() }
            )
        }

        composable(Screen.FamilyAdmin.route) {
            FamilyScreen(
                onBack          = { navController.popBackStack() },
                onNavigatePlans = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(
            route     = Screen.FamilyMemberConsent.route,
            arguments = listOf(
                navArgument("adminEmail") { defaultValue = ""; nullable = false },
                navArgument("token")      { defaultValue = ""; nullable = false }
            )
        ) { backStackEntry ->
            val adminEmail  = backStackEntry.arguments?.getString("adminEmail") ?: ""
            val inviteToken = backStackEntry.arguments?.getString("token") ?: ""
            FamilyMemberConsentScreen(
                adminEmail  = adminEmail,
                inviteToken = inviteToken,
                onDone      = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.Vault.route,
            arguments = listOf(
                navArgument("strengthFilter") { defaultValue = ""; nullable = false }
            )
        ) { backStackEntry ->
            val initialFilter    = backStackEntry.arguments?.getString("strengthFilter") ?: ""
            val vaultVm: VaultViewModel = hiltViewModel()
            LaunchedEffect(initialFilter) {
                if (initialFilter.isNotEmpty()) {
                    val filterSet = when (initialFilter) {
                        "WEAK"   -> setOf(StrengthFilter.WEAK)
                        "MEDIUM" -> setOf(StrengthFilter.MEDIUM)
                        "STRONG" -> setOf(StrengthFilter.STRONG)
                        else     -> emptySet()
                    }
                    if (filterSet.isNotEmpty()) vaultVm.onStrengthFiltersChange(filterSet)
                }
            }
            VaultScreen(
                onAddEntry              = { navController.navigate(Screen.AddEditEntry.createRoute("new")) },
                onEntryClick            = { id ->
                    recentEntriesVm.recordAccess(id, HubEntryType.PASSWORD)
                    navigateShared(Screen.PasswordDetail.createRoute(id))
                },
                onEditEntry             = { id -> navController.navigate(Screen.AddEditEntry.createRoute(id)) },
                onVaultHubClick         = { navController.popBackStack() },
                onPlansClick            = { navController.navigate(Screen.Plans.route) },
                viewModel               = vaultVm,
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBack             = { navController.popBackStack() },
                onPasswordClick    = { id, sq -> BusquedaEnCurso.recordar(sq, id); navController.navigate(Screen.PasswordDetail.createRoute(id)) },
                onBankAccountClick = { id, sq -> BusquedaEnCurso.recordar(sq, id); navController.navigate(Screen.BankAccountDetail.createRoute(id)) },
                onCreditCardClick  = { id, sq -> BusquedaEnCurso.recordar(sq, id); navController.navigate(Screen.CreditCardDetail.createRoute(id)) },
                onSecureNoteClick  = { id, sq -> BusquedaEnCurso.recordar(sq, id); navController.navigate(Screen.SecureNoteDetail.createRoute(id)) },
                onCustomEntryClick  = { id, sq -> BusquedaEnCurso.recordar(sq, id); navController.navigate(Screen.CustomEntryDetail.createRoute(id)) },
                onTwoFactorClick    = { id, sq -> BusquedaEnCurso.recordar(sq, id); navController.navigate(Screen.TwoFactorDetail.createRoute(id)) },
                onCryptoWalletClick       = { id, sq -> BusquedaEnCurso.recordar(sq, id); navController.navigate(Screen.CryptoWalletDetail.createRoute(id)) },
                onPasskeyClick            = { id, sq -> BusquedaEnCurso.recordar(sq, id); navController.navigate(Screen.PasskeyDetail.createRoute(id)) },
                onIdentityDocumentClick   = { id, sq -> BusquedaEnCurso.recordar(sq, id); navController.navigate(Screen.IdentityDocumentDetail.createRoute(id)) },
                onVehicleClick            = { id, sq -> BusquedaEnCurso.recordar(sq, id); navController.navigate(Screen.VehicleDetail.createRoute(id)) },
                onReminderClick           = { id -> navController.navigate(Screen.ReminderAddEdit.createRoute(id)) },
                onArchiveClick            = { id -> navController.navigate(Screen.Archive.withScrollTo(id)) },
                onTrashClick              = { id -> navController.navigate(Screen.Trash.withScrollTo(id)) },
                onPlansClick              = { navController.navigate(Screen.Plans.route) },
                onBackupResultClick       = { snapshotId, sq -> BusquedaEnCurso.recordar(sq, snapshotId); navController.navigate(Screen.SnapshotPreview.createRoute(snapshotId)) }
            )
        }


        composable(Screen.TwoFactor.route) {
            TwoFactorScreen(
                onBack           = { navController.popBackStack() },
                onAddAccount     = { navController.navigate(Screen.AddEditTwoFactor.createRoute("new")) },
                onEditAccount    = { id -> navController.navigate(Screen.TwoFactorDetail.createRoute(id)) },
                onPlansClick     = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(
            route     = Screen.AddEditTwoFactor.route,
            arguments = listOf(navArgument("id") {
                type         = NavType.StringType
                defaultValue = "new"
            })
        ) {
            AddEditTwoFactorScreen(
                onBack       = { navController.popBackStack() },
                onSaved      = { navController.popBackStack() },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(
            route     = Screen.AddEditEntry.route,
            arguments = listOf(
                navArgument("id")         { type = NavType.StringType;  defaultValue = "new"   },
                navArgument("cloneOf")    { type = NavType.StringType;  defaultValue = ""      },
                navArgument("prefillUrl") { type = NavType.StringType;  defaultValue = ""      }
            )
        ) {
            val currentEntry by navController.currentBackStackEntryAsState()
            val vaultBackStackEntry = remember(currentEntry) {
                try { navController.getBackStackEntry(Screen.Vault.route) }
                catch (_: IllegalArgumentException) { null }
            }
            val standaloneVm: VaultViewModel = hiltViewModel()
            val sharedVaultViewModel: VaultViewModel = if (vaultBackStackEntry != null)
                hiltViewModel(vaultBackStackEntry) else standaloneVm

            AddEditEntryScreen(
                onBack              = { navController.popBackStack() },
                onSaved             = { navController.popBackStack() },
                onDeleted           = { entryId ->
                    sharedVaultViewModel.startPendingDelete(entryId)
                    navController.popBackStack()
                },
                onPlansClick        = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(Screen.Generator.route) {
            PasswordGeneratorScreen(
                fromEntry     = false,
                onBack        = { navController.popBackStack() },
                onUsePassword = {}
            )
        }

        composable(Screen.AppHub.route) {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.Settings.PLAIN_ROUTE) {
                    popUpTo(Screen.AppHub.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(
            route     = Screen.LegalDoc.route,
            arguments = listOf(navArgument("docKey") { type = NavType.StringType })
        ) { backStackEntry ->
            LegalDocScreen(
                docKey = backStackEntry.arguments?.getString("docKey") ?: "privacy",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.Settings.route,
            arguments = listOf(navArgument("highlight") { defaultValue = ""; nullable = false })
        ) { backStackEntry ->
            val highlight = remember(backStackEntry) {
                val handle = backStackEntry.savedStateHandle
                if (handle.get<Boolean>("highlight_consumed") == true) {
                    ""
                } else {
                    val v = backStackEntry.arguments?.getString("highlight").orEmpty()
                    if (v.isNotEmpty()) handle["highlight_consumed"] = true
                    v
                }
            }
            val currentEntry by navController.currentBackStackEntryAsState()
            val vaultBackStackForSettings = remember(currentEntry) {
                try { navController.getBackStackEntry(Screen.Vault.route) }
                catch (_: IllegalArgumentException) { null }
            }
            val standaloneVmForSettings: VaultViewModel = hiltViewModel()
            val sharedVmForSettings: VaultViewModel = if (vaultBackStackForSettings != null)
                hiltViewModel(vaultBackStackForSettings) else standaloneVmForSettings

            SettingsScreen(
                onBack                      = { navController.popBackStack() },
                onThemeClick                = { navController.navigate(Screen.ThemeSelector.route) },
                onLanguageClick             = { navController.navigate(Screen.LanguagePicker.route) },
                onPanicPinClick             = { navController.navigate(Screen.PanicPin.route) },
                onCalculatorDisguiseClick   = { navController.navigate(Screen.CalculatorDisguise.route) },
                onTravelModeClick           = { navController.navigate(Screen.TravelMode.route) },
                onVoiceDictationClick       = { navController.navigate(Screen.VoiceDictation.route) },
                onDriveSyncClick            = { navController.navigate(Screen.DriveSync.route) },
                onBackupKeyClick            = { navController.navigate(Screen.BackupKey.route) },
                onDigitalInheritanceClick   = { navController.navigate(Screen.DigitalInheritance.route) },
                onFamilyPlanClick           = { navController.navigate(Screen.FamilyAdmin.route) },
                onImportExportClick         = { navController.navigate(Screen.ImportExport.route) },
                onChangeMasterPasswordClick = { navController.navigate(Screen.ChangeMasterPassword.route) },
                onHiddenEntriesClick        = { navController.navigate(Screen.HiddenEntries.route) },
                onAutofillClick             = { navController.navigate(Screen.AutofillSettings.route) },
                onDonateClick               = { navController.navigate(Screen.Donate.route) },
                onReceiveEntryClick         = { navController.navigate(Screen.ReceiveEntry.route) },
                onPlansClick                = { navController.navigate(Screen.Plans.route) },
                onMyPlanClick               = { navController.navigate(Screen.MyPlan.route) },
                onAuditClick                = { navController.navigate(Screen.Audit.route) },
                onNotificationsClick        = { navController.navigate(Screen.Notifications.route) },
                onLegalDocClick             = { docKey -> navController.navigate(Screen.LegalDoc.createRoute(docKey)) },
                onNavigateToWeakPasswords   = {
                    if (vaultBackStackForSettings != null) {
                        sharedVmForSettings.onStrengthFiltersChange(setOf(StrengthFilter.WEAK))
                        navController.popBackStack(Screen.Vault.route, false)
                    } else {
                        navController.navigate(Screen.Vault.withFilter("WEAK")) {
                            popUpTo(Screen.Settings.PLAIN_ROUTE) { inclusive = true }
                        }
                    }
                },
                onNavigateToMediumPasswords = {
                    if (vaultBackStackForSettings != null) {
                        sharedVmForSettings.onStrengthFiltersChange(setOf(StrengthFilter.MEDIUM))
                        navController.popBackStack(Screen.Vault.route, false)
                    } else {
                        navController.navigate(Screen.Vault.withFilter("MEDIUM")) {
                            popUpTo(Screen.Settings.PLAIN_ROUTE) { inclusive = true }
                        }
                    }
                },
                highlightDrive              = highlight == "drive",
                onVaultManagementClick  = { navController.navigate(Screen.VaultManagement.route) },
                onTimeMachineClick      = { navController.navigate(Screen.TimeMachine.route) },
                highlightTimeMachine    = highlight == "time_machine"
            )
        }

        composable(Screen.VaultManagement.route) {
            VaultManagementScreen(
                onBack       = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(
            route     = Screen.ShareEntry.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id")   { type = NavType.StringType }
            )
        ) {
            ShareEntryScreen(
                onBack       = { navController.popBackStack() }
            )
        }

        composable(Screen.ReceiveEntry.route) {
            ReceiveEntryScreen(
                onBack       = { navController.popBackStack() },
                onSaved      = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ImportExport.route) {
            ImportExportScreen(
                onBack               = { navController.popBackStack() },
                onPartialExportClick = { navController.navigate(Screen.PartialExport.route) }
            )
        }

        composable(route = Screen.PartialExport.route) {
            PartialExportScreen(onBack = { navController.popBackStack() })
        }

        composable(route = Screen.DigitalInheritance.route) {
            InheritanceScreen(
                onBack       = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(route = Screen.DriveSync.route) {
            DriveSyncScreen(
                onBack             = { navController.popBackStack() },
                onNavigatePlans    = { navController.navigate(Screen.Plans.route) },
                onTimeMachineClick = { navController.navigate(Screen.Settings.routeWithHighlight("time_machine")) },
                onOpenBackupKey    = { navController.navigate(Screen.BackupKey.route) }
            )
        }

        composable(route = Screen.BackupKey.route) {
            com.thormakk.keyarmor.presentation.screens.drivesync.BackupKeyScreen(
                onBack      = { navController.popBackStack() },
                onOpenDrive = { navController.navigate(Screen.DriveSync.route) }
            )
        }

        composable(route = Screen.TimeMachine.route) {
            TimeMachineScreen(
                onBack         = { navController.popBackStack() },
                onPreviewClick = { id -> navController.navigate(Screen.SnapshotPreview.createRoute(id)) },
                onPlansClick   = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(
            route     = Screen.SnapshotPreview.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            SnapshotPreviewScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ThemeSelector.route) {
            ThemeSelectorScreen(
                onBack       = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(route = Screen.LanguagePicker.route) {
            LanguagePickerScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.BankAccounts.route) {
            BankAccountsScreen(
                onBack                  = { navController.popBackStack() },
                onAddAccount            = { navController.navigate(Screen.AddEditBankAccount.createRoute("new")) },
                onEditAccount           = { id ->
                    recentEntriesVm.recordAccess(id, HubEntryType.BANK_ACCOUNT)
                    navigateShared(Screen.BankAccountDetail.createRoute(id))
                },
                onOpenEdit              = { id -> navController.navigate(Screen.AddEditBankAccount.createRoute(id)) },
                onPlansClick            = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(
            route     = Screen.AddEditBankAccount.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType; defaultValue = "new" },
                navArgument("cloneOf") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            AddEditBankAccountScreen(
                onBack              = { navController.popBackStack() },
                onSaved             = { navController.popBackStack() },
                onViewCredential    = { entryId ->
                    navController.navigate(Screen.LinkedEntry.createRoute(entryId))
                },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(
            route     = Screen.LinkedEntry.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            PasswordDetailScreen(
                onBack             = { navController.popBackStack() },
                onEdit             = { id -> navController.navigate(Screen.AddEditEntry.createRoute(id)) },
                onDeleted          = { navController.popBackStack() },
                onCloneClick       = { id -> navController.navigate(Screen.AddEditEntry.createRoute("new", cloneOf = id)) },
                onShareClick       = { id -> navController.navigate(Screen.ShareEntry.createRoute("PASSWORD", id)) },
                onAttachmentsClick = { id -> navController.navigate(Screen.Attachments.createRoute(id)) },
                onHistoryClick     = { id -> navController.navigate(Screen.PasswordHistory.createRoute(id)) },
                onPlansClick       = { navController.navigate(Screen.Plans.route) },
                onPasskeyClick     = { id -> navController.navigate(Screen.PasskeyDetail.createRoute(id)) }
            )
        }

        composable(Screen.CustomEntries.route) {
            CustomEntriesScreen(
                onBack           = { navController.popBackStack() },
                onAddEntry       = { navController.navigate(Screen.AddEditCustomEntry.createRoute("new")) },
                onEditEntry      = { id ->
                    recentEntriesVm.recordAccess(id, HubEntryType.CUSTOM_ENTRY)
                    navController.navigate(Screen.CustomEntryDetail.createRoute(id))
                },
                onOpenEdit       = { id -> navController.navigate(Screen.AddEditCustomEntry.createRoute(id)) },
                onPlansClick     = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(
            route     = Screen.AddEditCustomEntry.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType; defaultValue = "new" },
                navArgument("cloneOf") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            AddEditCustomEntryScreen(
                onBack             = { navController.popBackStack() },
                onSaved            = { navController.popBackStack() }
            )
        }

        composable(Screen.SecureNotes.route) {
            SecureNotesScreen(
                onBack           = { navController.popBackStack() },
                onAddNote        = { navController.navigate(Screen.AddEditSecureNote.createRoute("new")) },
                onEditNote       = { id ->
                    recentEntriesVm.recordAccess(id, HubEntryType.SECURE_NOTE)
                    navController.navigate(Screen.SecureNoteDetail.createRoute(id))
                },
                onOpenEdit       = { id -> navController.navigate(Screen.AddEditSecureNote.createRoute(id)) },
                onPlansClick     = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(
            route     = Screen.AddEditSecureNote.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType; defaultValue = "new" },
                navArgument("cloneOf") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            AddEditSecureNoteScreen(
                onBack             = { navController.popBackStack() },
                onSaved            = { navController.popBackStack() }
            )
        }

        composable(Screen.CreditCards.route) {
            CreditCardsScreen(
                onBack                   = { navController.popBackStack() },
                onAddCard                = { navController.navigate(Screen.AddEditCreditCard.createRoute("new")) },
                onEditCard               = { id ->
                    recentEntriesVm.recordAccess(id, HubEntryType.CREDIT_CARD)
                    navigateShared(Screen.CreditCardDetail.createRoute(id))
                },
                onOpenEdit               = { id -> navController.navigate(Screen.AddEditCreditCard.createRoute(id)) },
                onPlansClick             = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope    = sharedTransitionScope,
                animatedVisibilityScope  = this
            )
        }

        composable(
            route     = Screen.AddEditCreditCard.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType; defaultValue = "new" },
                navArgument("cloneOf") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            AddEditCreditCardScreen(
                onBack             = { navController.popBackStack() },
                onSaved            = { navController.popBackStack() },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(Screen.CryptoWallets.route) {
            CryptoWalletsScreen(
                onBack                  = { navController.popBackStack() },
                onAddWallet             = { navController.navigate(Screen.AddEditCryptoWallet.createRoute("new")) },
                onEditWallet            = { id ->
                    recentEntriesVm.recordAccess(id, HubEntryType.CRYPTO_WALLET)
                    navigateShared(Screen.CryptoWalletDetail.createRoute(id))
                },
                onPlansClick            = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(
            route     = Screen.AddEditCryptoWallet.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType; defaultValue = "new" },
                navArgument("cloneOf") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            AddEditCryptoWalletScreen(
                onBack             = { navController.popBackStack() },
                onSaved            = { navController.popBackStack() },
                onViewCredential   = { entryId -> navController.navigate(Screen.LinkedEntry.createRoute(entryId)) },
                onPlansClick       = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(
            route     = Screen.Attachments.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) {
            AttachmentsScreen(
                onBack       = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(
            route     = Screen.PasswordHistory.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) {
            PasswordHistoryScreen(
                onBack       = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(Screen.Plans.route) {
            PlansScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.MyPlan.route) {
            MyPlanScreen(
                onBack               = { navController.popBackStack() },
                onViewPlansClick     = { navController.navigate(Screen.Plans.route) },
                onManageFamilyClick  = { navController.navigate(Screen.FamilyAdmin.route) },
                onManageGoogleAccount = { navController.navigate(Screen.DriveSync.route) }
            )
        }

        composable(
            route     = Screen.Archive.route,
            arguments = listOf(navArgument("scrollToId") { defaultValue = ""; nullable = false })
        ) { backStackEntry ->
            val scrollToId = backStackEntry.arguments?.getString("scrollToId")?.takeIf { it.isNotEmpty() }
            ArchiveScreen(
                onBack       = { navController.popBackStack() },
                scrollToId   = scrollToId,
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(
            route     = Screen.Trash.route,
            arguments = listOf(navArgument("scrollToId") { defaultValue = ""; nullable = false })
        ) { backStackEntry ->
            val scrollToId = backStackEntry.arguments?.getString("scrollToId")?.takeIf { it.isNotEmpty() }
            TrashScreen(
                onBack       = { navController.popBackStack() },
                scrollToId   = scrollToId
            )
        }

        composable(Screen.PanicPin.route) {
            PanicPinScreen(
                onBack       = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }
        composable(Screen.CalculatorDisguise.route) {
            CalculatorDisguiseScreen(
                onBack       = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }
        composable(Screen.Audit.route) { backStackEntry ->
            val currentEntry by navController.currentBackStackEntryAsState()
            val vaultBackStackForAudit = remember(currentEntry) {
                try { navController.getBackStackEntry(Screen.Vault.route) }
                catch (_: IllegalArgumentException) { null }
            }
            val standaloneVmForAudit: VaultViewModel = hiltViewModel()
            val sharedVmForAudit: VaultViewModel = if (vaultBackStackForAudit != null)
                hiltViewModel(vaultBackStackForAudit) else standaloneVmForAudit

            AuditScreen(
                onBack       = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) },
                onEntryClick = { id -> navController.navigate(Screen.PasswordDetail.createRoute(id)) },
                onNavigateToWeakPasswords = {
                    if (vaultBackStackForAudit != null) {
                        sharedVmForAudit.onStrengthFiltersChange(setOf(StrengthFilter.WEAK))
                        navController.popBackStack(Screen.Vault.route, false)
                    } else {
                        navController.navigate(Screen.Vault.withFilter("WEAK")) {
                            popUpTo(Screen.Audit.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToMediumPasswords = {
                    if (vaultBackStackForAudit != null) {
                        sharedVmForAudit.onStrengthFiltersChange(setOf(StrengthFilter.MEDIUM))
                        navController.popBackStack(Screen.Vault.route, false)
                    } else {
                        navController.navigate(Screen.Vault.withFilter("MEDIUM")) {
                            popUpTo(Screen.Audit.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.TravelMode.route) {
            TravelModeScreen(
                onBack       = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }
        composable(Screen.VoiceDictation.route) {
            VoiceDictationScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.HiddenEntries.route) {
            HiddenEntriesScreen(
                onBack       = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }
        composable(Screen.AutofillSettings.route) {
            AutofillSettingsScreen(
                onBack              = { navController.popBackStack() },
                onAssociateNewEntry = { rawValue ->
                    navController.navigate(Screen.AddEditEntry.createRoute("new", prefillUrl = rawValue))
                },
            )
        }
        composable(Screen.ChangeMasterPassword.route) {
            ChangeMasterPasswordScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Donate.route) {
            DonateScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route     = Screen.BankAccountDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val sq = BusquedaEnCurso.paraEntrada(it.arguments?.getString("id").orEmpty())
            CompositionLocalProvider(LocalSearchQuery provides sq) {
            BankAccountDetailScreen(
                onBack             = { navController.popBackStack() },
                onEdit             = { id -> navController.navigate(Screen.AddEditBankAccount.createRoute(id)) },
                onViewCredential   = { entryId -> navController.navigate(Screen.LinkedEntry.createRoute(entryId)) },
                onDeleted          = { navController.popBackStack() },
                onCloneClick       = { id -> navController.navigate(Screen.AddEditBankAccount.createRoute("new", cloneOf = id)) },
                onShareClick            = { id -> navController.navigate(Screen.ShareEntry.createRoute("BANK_ACCOUNT", id)) },
                onAttachmentsClick      = { id -> navController.navigate(Screen.Attachments.createRoute(id)) },
                onPlansClick            = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
            }
        }

        composable(
            route     = Screen.CreditCardDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val sq = BusquedaEnCurso.paraEntrada(it.arguments?.getString("id").orEmpty())
            CompositionLocalProvider(LocalSearchQuery provides sq) {
            CreditCardDetailScreen(
                onBack                   = { navController.popBackStack() },
                onEdit                   = { id -> navController.navigate(Screen.AddEditCreditCard.createRoute(id)) },
                onDeleted                = { navController.popBackStack() },
                onCloneClick             = { id -> navController.navigate(Screen.AddEditCreditCard.createRoute("new", cloneOf = id)) },
                onShareClick             = { id -> navController.navigate(Screen.ShareEntry.createRoute("CREDIT_CARD", id)) },
                onAttachmentsClick       = { id -> navController.navigate(Screen.Attachments.createRoute(id)) },
                onPlansClick             = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope    = sharedTransitionScope,
                animatedVisibilityScope  = this
            )
            }
        }

        composable(
            route     = Screen.CryptoWalletDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val sq = BusquedaEnCurso.paraEntrada(it.arguments?.getString("id").orEmpty())
            CompositionLocalProvider(LocalSearchQuery provides sq) {
            CryptoWalletDetailScreen(
                onBack                  = { navController.popBackStack() },
                onEdit                  = { id -> navController.navigate(Screen.AddEditCryptoWallet.createRoute(id)) },
                onViewCredential        = { entryId -> navController.navigate(Screen.LinkedEntry.createRoute(entryId)) },
                onDeleted               = { navController.popBackStack() },
                onCloneClick            = { id -> navController.navigate(Screen.AddEditCryptoWallet.createRoute("new", cloneOf = id)) },
                onShareClick            = { id -> navController.navigate(Screen.ShareEntry.createRoute("CRYPTO_WALLET", id)) },
                onAttachmentsClick      = { id -> navController.navigate(Screen.Attachments.createRoute(id)) },
                onPlansClick            = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
            }
        }

        composable(
            route     = Screen.PasswordDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val sq = BusquedaEnCurso.paraEntrada(it.arguments?.getString("id").orEmpty())
            CompositionLocalProvider(LocalSearchQuery provides sq) {
            PasswordDetailScreen(
                onBack             = { navController.popBackStack() },
                onEdit             = { id -> navController.navigate(Screen.AddEditEntry.createRoute(id)) },
                onDeleted          = { navController.popBackStack() },
                onCloneClick       = { id -> navController.navigate(Screen.AddEditEntry.createRoute("new", cloneOf = id)) },
                onShareClick       = { id -> navController.navigate(Screen.ShareEntry.createRoute("PASSWORD", id)) },
                onAttachmentsClick = { id -> navController.navigate(Screen.Attachments.createRoute(id)) },
                onHistoryClick     = { id -> navController.navigate(Screen.PasswordHistory.createRoute(id)) },
                onPlansClick       = { navController.navigate(Screen.Plans.route) },
                onPasskeyClick     = { id -> navController.navigate(Screen.PasskeyDetail.createRoute(id)) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
            }
        }

        composable(
            route     = Screen.SecureNoteDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val sq = BusquedaEnCurso.paraEntrada(it.arguments?.getString("id").orEmpty())
            CompositionLocalProvider(LocalSearchQuery provides sq) {
            SecureNoteDetailScreen(
                onBack             = { navController.popBackStack() },
                onEdit             = { id -> navController.navigate(Screen.AddEditSecureNote.createRoute(id)) },
                onDeleted          = { navController.popBackStack() },
                onCloneClick       = { id -> navController.navigate(Screen.AddEditSecureNote.createRoute("new", cloneOf = id)) },
                onShareClick       = { id -> navController.navigate(Screen.ShareEntry.createRoute("SECURE_NOTE", id)) },
                onAttachmentsClick = { id -> navController.navigate(Screen.Attachments.createRoute(id)) },
                onPlansClick       = { navController.navigate(Screen.Plans.route) },
                onHistoryClick     = { id -> navController.navigate(Screen.SecureNoteHistory.createRoute(id)) }
            )
            }
        }

        composable(
            route     = Screen.SecureNoteHistory.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            SecureNoteHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.CustomEntryDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val sq = BusquedaEnCurso.paraEntrada(it.arguments?.getString("id").orEmpty())
            CompositionLocalProvider(LocalSearchQuery provides sq) {
            CustomEntryDetailScreen(
                onBack             = { navController.popBackStack() },
                onEdit             = { id -> navController.navigate(Screen.AddEditCustomEntry.createRoute(id)) },
                onDeleted          = { navController.popBackStack() },
                onCloneClick       = { id -> navController.navigate(Screen.AddEditCustomEntry.createRoute("new", cloneOf = id)) },
                onShareClick       = { id -> navController.navigate(Screen.ShareEntry.createRoute("CUSTOM_ENTRY", id)) },
                onAttachmentsClick = { id -> navController.navigate(Screen.Attachments.createRoute(id)) },
                onPlansClick       = { navController.navigate(Screen.Plans.route) }
            )
            }
        }

        composable(
            route     = Screen.TwoFactorDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val sq = BusquedaEnCurso.paraEntrada(it.arguments?.getString("id").orEmpty())
            CompositionLocalProvider(LocalSearchQuery provides sq) {
            TwoFactorDetailScreen(
                onBack       = { navController.popBackStack() },
                onEdit       = { id -> navController.navigate(Screen.AddEditTwoFactor.createRoute(id)) },
                onDeleted    = { navController.popBackStack() },
                onPlansClick = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
            }
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBack          = { navController.popBackStack() },
                onAuditClick    = { navController.navigate(Screen.Audit.route) },
                onReminderClick = { id ->
                    navController.navigate(Screen.ReminderAddEdit.createRoute(id))
                },
                onEntryClick    = { id ->
                    when {
                        id.startsWith("identity:") ->
                            navController.navigate(Screen.IdentityDocumentDetail.createRoute(id.removePrefix("identity:")))
                        id.startsWith("vehicle:")  ->
                            navController.navigate(Screen.VehicleDetail.createRoute(id.removePrefix("vehicle:")))
                        id.startsWith("password:") ->
                            navController.navigate(Screen.PasswordDetail.createRoute(id.removePrefix("password:")))
                        else ->
                            navController.navigate(Screen.PasswordDetail.createRoute(id))
                    }
                }
            )
        }

        composable(Screen.Passkeys.route) {
            PasskeysScreen(
                onBack                  = { navController.popBackStack() },
                onPasskeyClick          = { id -> navController.navigate(Screen.PasskeyDetail.createRoute(id)) },
                onPasskeyEditClick      = { id -> navController.navigate(Screen.PasskeyEdit.createRoute(id)) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(
            route     = Screen.PasskeyDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val sq = BusquedaEnCurso.paraEntrada(it.arguments?.getString("id").orEmpty())
            CompositionLocalProvider(LocalSearchQuery provides sq) {
            PasskeyDetailScreen(
                onBack                  = { navController.popBackStack() },
                onEdit                  = { id -> navController.navigate(Screen.PasskeyEdit.createRoute(id)) },
                onViewCredential        = { entryId -> navController.navigate(Screen.LinkedEntry.createRoute(entryId)) },
                onDeleted               = { navController.popBackStack() },
                onArchived              = { navController.popBackStack() },
                onPlansClick            = { navController.navigate(Screen.Plans.route) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
            }
        }

        composable(
            route     = Screen.PasskeyEdit.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            PasskeyEditScreen(
                onBack           = { navController.popBackStack() },
                onViewCredential = { entryId -> navController.navigate(Screen.LinkedEntry.createRoute(entryId)) },
                sharedTransitionScope   = sharedTransitionScope,
                animatedVisibilityScope = this
            )
        }

        composable(Screen.IdentityDocuments.route) {
            IdentityDocumentsScreen(
                onBack         = { navController.popBackStack() },
                onAddDocument  = { navController.navigate(Screen.AddEditIdentityDocument.createRoute("new")) },
                onEditDocument = { id ->
                    recentEntriesVm.recordAccess(id, HubEntryType.IDENTITY_DOCUMENT)
                    navController.navigate(Screen.IdentityDocumentDetail.createRoute(id))
                },
                onPlansClick   = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(
            route     = Screen.AddEditIdentityDocument.route,
            arguments = listOf(
                navArgument("id") { defaultValue = "new" },
                navArgument("cloneOf") { defaultValue = ""; nullable = false }
            )
        ) {
            AddEditIdentityDocumentScreen(
                onBack       = { navController.popBackStack() },
                onSaved      = { navController.popBackStack() },
                onDeleted    = { navController.navigate(Screen.IdentityDocuments.route) { popUpTo(Screen.IdentityDocuments.route) { inclusive = false } } }
            )
        }

        composable(
            route     = Screen.IdentityDocumentDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val sq = BusquedaEnCurso.paraEntrada(it.arguments?.getString("id").orEmpty())
            CompositionLocalProvider(LocalSearchQuery provides sq) {
            IdentityDocumentDetailScreen(
                onBack             = { navController.popBackStack() },
                onEdit             = { id -> navController.navigate(Screen.AddEditIdentityDocument.createRoute(id)) },
                onDeleted          = { navController.navigate(Screen.IdentityDocuments.route) { popUpTo(Screen.IdentityDocuments.route) { inclusive = false } } },
                onAttachmentsClick = { id -> navController.navigate(Screen.Attachments.createRoute(id)) },
                onPlansClick       = { navController.navigate(Screen.Plans.route) }
            )
            }
        }

        composable(Screen.Vehicles.route) {
            VehiclesScreen(
                onBack        = { navController.popBackStack() },
                onAddVehicle  = { navController.navigate(Screen.AddEditVehicle.createRoute("new")) },
                onEditVehicle = { id ->
                    recentEntriesVm.recordAccess(id, HubEntryType.VEHICLE)
                    navController.navigate(Screen.VehicleDetail.createRoute(id))
                },
                onPlansClick  = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(
            route     = Screen.AddEditVehicle.route,
            arguments = listOf(
                navArgument("id") { defaultValue = "new" },
                navArgument("cloneOf") { defaultValue = ""; nullable = false }
            )
        ) {
            AddEditVehicleScreen(
                onBack       = { navController.popBackStack() },
                onSaved      = { navController.popBackStack() },
                onDeleted    = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.VehicleDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val sq = BusquedaEnCurso.paraEntrada(it.arguments?.getString("id").orEmpty())
            CompositionLocalProvider(LocalSearchQuery provides sq) {
            VehicleDetailScreen(
                onBack              = { navController.popBackStack() },
                onEdit              = { id -> navController.navigate(Screen.AddEditVehicle.createRoute(id)) },
                onDeleted           = { navController.navigate(Screen.Vehicles.route) { popUpTo(Screen.Vehicles.route) { inclusive = false } } },
                onPlansClick        = { navController.navigate(Screen.Plans.route) },
                onAttachmentsClick  = { id -> navController.navigate(Screen.Attachments.createRoute(id)) },
                onShareClick        = { id -> navController.navigate(Screen.ShareEntry.createRoute("VEHICLE", id)) }
            )
            }
        }

        composable(Screen.Reminders.route) {
            RemindersScreen(
                onBack       = { navController.popBackStack() },
                onAddClick   = { initialDateMs ->
                    navController.navigate(
                        Screen.ReminderAddEdit.createRoute(Screen.ReminderAddEdit.NEW_ID, initialDateMs)
                    )
                },
                onEditClick  = { id -> navController.navigate(Screen.ReminderAddEdit.createRoute(id)) },
                onPlansClick = { navController.navigate(Screen.Plans.route) }
            )
        }

        composable(
            route     = Screen.ReminderAddEdit.route,
            arguments = listOf(
                navArgument("id") { defaultValue = Screen.ReminderAddEdit.NEW_ID },
                navArgument("initialDate") { type = NavType.LongType; defaultValue = -1L }
            )
        ) {
            val reminderId = it.arguments?.getString("id") ?: ""
            ReminderAddEditScreen(
                onBack             = { navController.popBackStack() },
                onAttachmentsClick = { navController.navigate(Screen.Attachments.createRoute(reminderId)) }
            )
        }

    }
    }
    }
    }
    }

    CoachMarkOverlay(coachMarks)
    }
    }
}
