package com.thormakk.keyarmor.presentation.screens.settings

import android.content.Context
import com.thormakk.keyarmor.presentation.util.getFirebaseIdentityTokenOrNull
import android.view.autofill.AutofillManager
import com.google.android.gms.tasks.Tasks
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.AutoLockPrefs
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.data.prefs.BreachCheckPrefs
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs
import com.thormakk.keyarmor.data.prefs.ClipboardPrefs
import com.thormakk.keyarmor.data.prefs.VoiceDictationPrefs
import com.thormakk.keyarmor.presentation.screens.language.LanguageViewModel
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.ExpiryNotificationPrefs
import com.thormakk.keyarmor.data.prefs.FamilyPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.TravelModePrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AccountResetRepository
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.InAppNotificationRepository
import com.thormakk.keyarmor.data.repository.InheritanceRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.presentation.UiText
import com.thormakk.keyarmor.presentation.screens.hub.SecurityLevel
import com.thormakk.keyarmor.presentation.screens.hub.SecurityUiState
import com.thormakk.keyarmor.security.BiometricCryptoManager
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import com.thormakk.keyarmor.workers.BreachCheckWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.crypto.Cipher
import javax.inject.Inject

data class SettingsUiState(
    val biometricAvailable: Boolean   = false,
    val biometricEnabled:   Boolean   = false,
    val biometricPending:      Boolean = false,
    val gateDisablePending:    Boolean = false,
    val lastDriveSync:      Long      = 0L,
    val driveAccount:       String?   = null,
    val hasDriveBackup:     Boolean   = false,
    val userPlan:           UserPlan  = UserPlan.FREE,
    val backupKeyConfirmed: Boolean   = false,
    val clipboardClearEnabled: Boolean = true,
    val clipboardClearSeconds: Int     = 30,
    val autoLockSeconds: Int           = 180,
    val expiryNotifEnabled: Boolean      = true,
    val expiryNotifWarningDays: Set<Int> = setOf(7),
    val breachCheckEnabled: Boolean = false,
    val breachCheckFrequencyDays: Int = BreachCheckPrefs.DEFAULT_FREQUENCY,
    val autofillEnabled: Boolean = false,
    val voiceDictationEnabled: Boolean = false,
    val themeId: String = "dark",
    val languageCode: String = "",
    val travelModeEnabled: Boolean = false,
    val panicPinEnabled: Boolean = false,
    val calculatorDisguiseEnabled: Boolean = false,
    val hiddenEntriesEnabled: Boolean = false,
    val inheritanceEnabled: Boolean = false,
    val inheritanceHasHeir: Boolean = false,
    val isHeirOfOwner: Boolean = false,
    val hasSnapshots: Boolean = false,
    val profileName: String = "",
    val showEditNameDialog: Boolean = false,
    val showDeleteAllDialog: Boolean = false,
    val deleteAllInProgress: Boolean = false,
    val deleteAllAwaitingAuth: Boolean = false,
    val deleteAllNeedsSignIn: Boolean = false,
    val deleteAllExpectedEmail: String? = null,
    val isGateEnabled:  Boolean     = false,
    val gateSections:   Set<String> = emptySet(),
    val gateMode:       String      = BiometricPrefs.MODE_PER_ACCESS,
    val gateTimeout:    Int         = BiometricPrefs.DEFAULT_TIMEOUT
)

sealed class SettingsEvent {
    data class Error(val message: UiText)       : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val autoLockPrefs:    AutoLockPrefs,
    private val authRepository:   AuthRepository,
    private val biometricCrypto:  BiometricCryptoManager,
    private val drivePrefs:       DrivePrefs,
    private val fcmPrefs:         FcmPrefs,
    private val planPrefs:        PlanPrefs,
    private val settingsViewPrefs: com.thormakk.keyarmor.data.prefs.SettingsViewPrefs,
    private val driveRepo:        com.thormakk.keyarmor.data.drive.DriveBackupRepository,
    private val clipboardPrefs:   ClipboardPrefs,
    private val voiceDictationPrefs: VoiceDictationPrefs,
    private val expiryNotifPrefs: ExpiryNotificationPrefs,
    private val breachCheckPrefs: BreachCheckPrefs,
    private val biometricGatePrefs: BiometricPrefs,
    vaultRepository:  VaultRepository,
    notifRepository:  InAppNotificationRepository,
    private val travelModePrefs:  TravelModePrefs,
    private val panicPinPrefs:    PanicPinPrefs,
    private val inheritancePrefs: InheritancePrefs,
    private val snapshotDao: com.thormakk.keyarmor.data.database.dao.VaultSnapshotDao,
    private val snapshotPrefs: com.thormakk.keyarmor.data.prefs.SnapshotPrefs,
    private val calculatorDisguisePrefs: CalculatorDisguisePrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    private val familyPrefs: FamilyPrefs,
    private val inheritanceRepository: InheritanceRepository,
    private val accountResetRepository: AccountResetRepository,
    private val claveDeRespaldo: com.thormakk.keyarmor.data.backup.ClaveDeRespaldo,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val settingsSimple = settingsViewPrefs.simple
    fun setSettingsSimple(simple: Boolean) = settingsViewPrefs.setSimple(simple)

    val unreadNotifCount: StateFlow<Int> =
        notifRepository.observeUnreadCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val securityState: StateFlow<SecurityUiState> = vaultRepository.observeEntries()
        .map { entries ->
            val withPassword = entries.filter { it.password.isNotEmpty() }
            val weakEntries = withPassword.filter { e -> passwordStrength(e.password) <= 1 }.toSet()
            val weakCount   = weakEntries.size
            val mediumCount = withPassword
                .count { e -> e !in weakEntries && passwordStrength(e.password) == 2 }
            val level = when {
                withPassword.isEmpty() -> SecurityLevel.NO_DATA
                weakCount   > 0        -> SecurityLevel.CRITICAL
                mediumCount > 0        -> SecurityLevel.INTERMEDIATE
                else                   -> SecurityLevel.SAFE
            }
            SecurityUiState(
                level        = level,
                weakCount    = weakCount,
                mediumCount  = mediumCount
            )
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SecurityUiState())

    private fun passwordStrength(password: String): Int =
        PasswordStrengthCalculator.calculate(password).coerceAtLeast(0)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    val coercionActive: StateFlow<Boolean> = panicPinPrefs.coercionActiveFlow

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var puntosLocales = 0

    private fun actualizarBackupDeDrive(lastDriveSync: Long, email: String?) {
        if (lastDriveSync > 0L) settingsViewPrefs.setDriveHasBackup(true)
        _uiState.value = _uiState.value.copy(
            hasDriveBackup = lastDriveSync > 0L || settingsViewPrefs.driveHasBackup()
        )
        if (email == null || lastDriveSync > 0L) return
        viewModelScope.launch {
            val r = runCatching {
                driveRepo.getCloudBackupTimestamp(android.accounts.Account(email, "com.google"))
            }
            if (r.isSuccess) {
                settingsViewPrefs.setDriveHasBackup(r.getOrNull() != null)
                _uiState.value = _uiState.value.copy(hasDriveBackup = r.getOrNull() != null)
            }
        }
    }

    private fun actualizarPuntos() {
        _uiState.value = _uiState.value.copy(hasSnapshots = puntosLocales > 0 || snapshotPrefs.driveHasPoints())
    }

    private val autofillManager: AutofillManager? =
        context.getSystemService(AutofillManager::class.java)

    private fun isAutofillEnabled(): Boolean =
        autofillManager?.hasEnabledAutofillServices() == true

    init {
        viewModelScope.launch {
            snapshotDao.observeCount().collect { n ->
                puntosLocales = n
                actualizarPuntos()
            }
        }
        viewModelScope.launch {
            val available     = isBiometricAvailable()
            val enabled       = available && authRepository.isBiometricKeyStored()
            val lastDriveSync = authRepository.getLastDriveSync()
            val driveEmail    = drivePrefs.getConnectedEmail()

            _uiState.value = _uiState.value.copy(
                biometricAvailable     = available,
                biometricEnabled       = enabled,
                lastDriveSync          = lastDriveSync,
                driveAccount           = driveEmail,
                userPlan               = planPrefs.effectivePlan(),
                backupKeyConfirmed     = claveDeRespaldo.confirmada(),
                clipboardClearEnabled  = clipboardPrefs.isEnabled(),
                clipboardClearSeconds  = clipboardPrefs.getTimeoutSeconds(),
                autoLockSeconds        = autoLockPrefs.getTimeoutSeconds(),
                expiryNotifEnabled     = expiryNotifPrefs.isEnabled(),
                expiryNotifWarningDays = expiryNotifPrefs.getWarningDaySet(),
                breachCheckEnabled     = breachCheckPrefs.isEnabled(),
                breachCheckFrequencyDays = breachCheckPrefs.getFrequencyDays(),
                autofillEnabled        = isAutofillEnabled(),
                voiceDictationEnabled  = voiceDictationPrefs.isEnabled(),
                themeId                = authRepository.getThemeId(),
                languageCode           = authRepository.getLanguage() ?: LanguageViewModel.detectSystemLanguage(),
                travelModeEnabled      = travelModePrefs.isEnabled(),
                panicPinEnabled        = panicPinPrefs.isPanicPinEnabled(),
                calculatorDisguiseEnabled = calculatorDisguisePrefs.isEnabled(),
                hiddenEntriesEnabled      = hiddenEntriesPrefs.isEnabled(),
                inheritanceEnabled     = inheritancePrefs.isEnabled(),
                inheritanceHasHeir     = inheritancePrefs.getHeirEmails().isNotEmpty(),
                isHeirOfOwner          = inheritancePrefs.getHeirOfOwnerEmail() != null,
                profileName            = authRepository.getName() ?: "",
                isGateEnabled          = biometricGatePrefs.isEnabled(),
                gateSections           = biometricGatePrefs.getEnabledSections(),
                gateMode               = biometricGatePrefs.getMode(),
                gateTimeout            = biometricGatePrefs.getSessionTimeoutSeconds()
            )
            actualizarBackupDeDrive(lastDriveSync, driveEmail)
        }
        viewModelScope.launch {
            planPrefs.planFlow.collect { _ ->
                _uiState.value = _uiState.value.copy(userPlan = planPrefs.effectivePlan())
            }
        }
    }

    fun showEditNameDialog() {
        _uiState.value = _uiState.value.copy(showEditNameDialog = true)
    }

    fun hideEditNameDialog() {
        _uiState.value = _uiState.value.copy(showEditNameDialog = false)
    }

    fun updateName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            authRepository.setName(trimmed)
            _uiState.value = _uiState.value.copy(profileName = trimmed, showEditNameDialog = false)
        }
    }

    fun setAutoLockSeconds(seconds: Int) {
        autoLockPrefs.setTimeoutSeconds(seconds)
        _uiState.value = _uiState.value.copy(autoLockSeconds = seconds)
    }

    fun setBreachCheckEnabled(enabled: Boolean) {
        breachCheckPrefs.setEnabled(enabled)
        _uiState.value = _uiState.value.copy(breachCheckEnabled = enabled)
        if (enabled) {
            BreachCheckWorker.schedule(context, breachCheckPrefs.getFrequencyDays(), replace = false)
        } else {
            BreachCheckWorker.cancel(context)
        }
    }

    fun setBreachCheckFrequencyDays(days: Int) {
        breachCheckPrefs.setFrequencyDays(days)
        _uiState.value = _uiState.value.copy(breachCheckFrequencyDays = days)
        if (breachCheckPrefs.isEnabled()) {
            BreachCheckWorker.schedule(context, days, replace = true)
        }
    }

    fun setExpiryNotifEnabled(enabled: Boolean) {
        expiryNotifPrefs.setEnabled(enabled)
        _uiState.value = _uiState.value.copy(expiryNotifEnabled = enabled)
    }

    fun toggleExpiryWarningDay(day: Int) {
        expiryNotifPrefs.toggleWarningDay(day)
        _uiState.value = _uiState.value.copy(
            expiryNotifWarningDays = expiryNotifPrefs.getWarningDaySet()
        )
    }

    fun setClipboardClearEnabled(enabled: Boolean) {
        clipboardPrefs.setEnabled(enabled)
        _uiState.value = _uiState.value.copy(clipboardClearEnabled = enabled)
    }

    fun setClipboardClearSeconds(seconds: Int) {
        clipboardPrefs.setTimeoutSeconds(seconds)
        _uiState.value = _uiState.value.copy(clipboardClearSeconds = seconds)
    }

    fun requestEnableBiometric() {
        _uiState.value = _uiState.value.copy(biometricPending = true)
    }

    fun getSetupCipher(): Cipher? =
        try { biometricCrypto.getEncryptionCipher() } catch (e: Exception) { null }

    fun onBiometricSetupSuccess(cipher: Cipher) {
        val masterKeyBytes = authRepository.getMasterKeyBytes() ?: run {
            cancelEnableBiometric(); return
        }
        viewModelScope.launch {
            try {
                val encrypted = biometricCrypto.encryptMasterKey(cipher, masterKeyBytes)
                masterKeyBytes.fill(0)
                authRepository.setBiometricKey(encrypted)
                authRepository.markBiometricSetupSeen()
                _uiState.value = _uiState.value.copy(
                    biometricEnabled = true,
                    biometricPending = false
                )
            } catch (e: Exception) {
                masterKeyBytes.fill(0)
                cancelEnableBiometric()
            }
        }
    }

    fun cancelEnableBiometric() {
        _uiState.value = _uiState.value.copy(biometricPending = false)
    }

    fun disableBiometric() {
        viewModelScope.launch {
            authRepository.clearBiometricKey()
            biometricCrypto.deleteKey()
            _uiState.value = _uiState.value.copy(biometricEnabled = false)
        }
    }

    fun requestDisableGate() {
        _uiState.value = _uiState.value.copy(gateDisablePending = true)
    }

    fun onGateDisableConfirmed() {
        biometricGatePrefs.setEnabled(false)
        _uiState.value = _uiState.value.copy(isGateEnabled = false, gateDisablePending = false)
    }

    fun cancelGateDisable() {
        _uiState.value = _uiState.value.copy(gateDisablePending = false)
    }

    fun refreshDriveState() {
        viewModelScope.launch {
            val email         = drivePrefs.getConnectedEmail()
            val lastDriveSync = authRepository.getLastDriveSync()
            _uiState.value = _uiState.value.copy(
                driveAccount    = email,
                lastDriveSync   = lastDriveSync,
                backupKeyConfirmed = claveDeRespaldo.confirmada(),
                autofillEnabled = isAutofillEnabled(),
                voiceDictationEnabled = voiceDictationPrefs.isEnabled(),
                themeId         = authRepository.getThemeId(),
                languageCode    = authRepository.getLanguage() ?: LanguageViewModel.detectSystemLanguage(),
                travelModeEnabled         = travelModePrefs.isEnabled(),
                panicPinEnabled           = panicPinPrefs.isPanicPinEnabled(),
                calculatorDisguiseEnabled = calculatorDisguisePrefs.isEnabled(),
                hiddenEntriesEnabled      = hiddenEntriesPrefs.isEnabled(),
                inheritanceEnabled        = inheritancePrefs.isEnabled(),
                inheritanceHasHeir        = inheritancePrefs.getHeirEmails().isNotEmpty(),
                isHeirOfOwner             = inheritancePrefs.getHeirOfOwnerEmail() != null
            )
            actualizarPuntos()
            actualizarBackupDeDrive(lastDriveSync, email)
        }
    }

    fun setGateEnabled(enabled: Boolean) {
        biometricGatePrefs.setEnabled(enabled)
        _uiState.value = _uiState.value.copy(isGateEnabled = enabled)
    }

    fun toggleGateSection(section: String) {
        biometricGatePrefs.toggleSection(section)
        _uiState.value = _uiState.value.copy(gateSections = biometricGatePrefs.getEnabledSections())
    }

    fun setGateMode(mode: String) {
        biometricGatePrefs.setMode(mode)
        _uiState.value = _uiState.value.copy(gateMode = mode)
    }

    fun setGateTimeout(seconds: Int) {
        biometricGatePrefs.setSessionTimeoutSeconds(seconds)
        _uiState.value = _uiState.value.copy(gateTimeout = seconds)
    }

    fun showDeleteAllDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAllDialog = true)
    }

    fun hideDeleteAllDialog() {
        if (_uiState.value.deleteAllInProgress) return
        _uiState.value = _uiState.value.copy(
            showDeleteAllDialog   = false,
            deleteAllNeedsSignIn  = false,
            deleteAllAwaitingAuth = false
        )
    }

    fun requestDeleteAllAuth() {
        if (_uiState.value.deleteAllInProgress) return
        _uiState.value = _uiState.value.copy(deleteAllAwaitingAuth = true)
    }

    fun onDeleteAllAuthorized() {
        _uiState.value = _uiState.value.copy(deleteAllAwaitingAuth = false)
        confirmDeleteAll()
    }

    fun onDeleteAllAuthCancelled() {
        _uiState.value = _uiState.value.copy(deleteAllAwaitingAuth = false)
    }

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    fun confirmDeleteAll() {
        if (_uiState.value.deleteAllInProgress) return
        _uiState.value = _uiState.value.copy(deleteAllInProgress = true, deleteAllNeedsSignIn = false)
        viewModelScope.launch {
            val hasBackendData =
                inheritancePrefs.isEnabled() ||
                inheritancePrefs.getHeirOfOwnerEmail() != null ||
                inheritancePrefs.getDeviceToken() != null ||
                familyPrefs.isMember() ||
                familyPrefs.getMemberEmails().isNotEmpty() ||
                fcmPrefs.lastRegisteredFcmToken != null ||
                drivePrefs.getConnectedEmail() != null

            val idToken = fetchGoogleIdToken()

            if (idToken == null && hasBackendData) {
                _uiState.value = _uiState.value.copy(
                    deleteAllInProgress    = false,
                    deleteAllNeedsSignIn   = true,
                    deleteAllExpectedEmail = drivePrefs.getConnectedEmail()
                )
                return@launch
            }

            runDeleteAll(idToken)
        }
    }

    fun onDeleteAllSignInResult(idToken: String?, email: String?) {
        if (idToken == null) {
            _uiState.value = _uiState.value.copy(deleteAllInProgress = false)
            return
        }
        val expected = _uiState.value.deleteAllExpectedEmail
        if (expected != null && email != null && !email.equals(expected, ignoreCase = true)) {
            viewModelScope.launch {
                _events.send(SettingsEvent.Error(
                    UiText.StringResource(com.thormakk.keyarmor.R.string.delete_all_error_wrong_account, expected)
                ))
            }
            return
        }
        _uiState.value = _uiState.value.copy(deleteAllInProgress = true, deleteAllNeedsSignIn = false)
        viewModelScope.launch { runDeleteAll(idToken) }
    }

    fun confirmDeleteLocalOnly() {
        if (_uiState.value.deleteAllInProgress) return
        _uiState.value = _uiState.value.copy(deleteAllInProgress = true, deleteAllNeedsSignIn = false)
        viewModelScope.launch { runDeleteAll(idToken = null, skipBackend = true) }
    }

    private suspend fun runDeleteAll(idToken: String?, skipBackend: Boolean = false) {
        if (idToken != null && !skipBackend) {
            val heirOf   = listOfNotNull(inheritancePrefs.getHeirOfOwnerEmail())
            val memberOf = listOfNotNull(familyPrefs.getAdminEmail())
            val ok = runCatching {
                inheritanceRepository.deleteAllUserData(idToken, heirOf, memberOf)
            }.isSuccess
            if (!ok) {
                _uiState.value = _uiState.value.copy(deleteAllInProgress = false)
                _events.send(SettingsEvent.Error(
                    UiText.StringResource(com.thormakk.keyarmor.R.string.delete_all_error_backend)
                ))
                return
            }
        }
        accountResetRepository.wipeLocalEverything()
        accountResetRepository.restartApp()
    }

    private suspend fun fetchGoogleIdToken(): String? = getFirebaseIdentityTokenOrNull(context)?.idToken

    private fun isBiometricAvailable(): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
}
