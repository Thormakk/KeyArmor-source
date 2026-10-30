@file:Suppress("UNUSED_VALUE")
package com.thormakk.keyarmor.presentation.screens.auth

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.ui.theme.Cream200
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.ui.theme.InterFamily
import com.thormakk.keyarmor.ui.theme.NavyAccent
import com.thormakk.keyarmor.ui.theme.WarningAmber

private const val BIO_UNLOCK = "UNLOCK"
private const val BIO_SETUP  = "SETUP"

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    viewModel: LockViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()
    val coercionActive by viewModel.coercionActive.collectAsStateWithLifecycle()
    val focusManager  = LocalFocusManager.current
    val context       = LocalContext.current
    val activity      = context as FragmentActivity

    val showBiometricUnlock = uiState.biometricEnabled && !coercionActive

    var showPassword         by remember { mutableStateOf(false) }
    var biometricAutoLaunched by remember { mutableStateOf(false) }

    val bioOperation = remember { mutableStateOf<String?>(null) }

    val biometricCallback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val op = bioOperation.value.also { bioOperation.value = null }
                if (result.authenticationType !=
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC) {
                    if (op == BIO_SETUP) viewModel.skipBiometricSetup()
                    return
                }
                when (op) {
                    BIO_UNLOCK -> result.cryptoObject?.cipher?.let { viewModel.onBiometricUnlockSuccess(it) }
                    BIO_SETUP  -> result.cryptoObject?.cipher
                        ?.let { viewModel.onBiometricSetupSuccess(it) }
                        ?: viewModel.skipBiometricSetup()
                }
            }
            override fun onAuthenticationFailed() { }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                val op = bioOperation.value.also { bioOperation.value = null }
                if (op == BIO_SETUP) viewModel.skipBiometricSetup()
            }
        }
    }

    val biometricPrompt = remember(activity) {
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), biometricCallback)
    }

    val lockTitle         = stringResource(R.string.lock_title)
    val actionCancel      = stringResource(R.string.action_cancel)
    val authBioTitle      = stringResource(R.string.auth_biometric_title)
    val lockSetupSubtitle = stringResource(R.string.lock_biometric_setup_subtitle)
    val lockNotNow        = stringResource(R.string.lock_not_now)

    val unlockPromptInfo = remember(lockTitle, actionCancel) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(lockTitle)
            .setNegativeButtonText(actionCancel)
            .build()
    }

    val setupPromptInfo = remember(authBioTitle, lockSetupSubtitle, lockNotNow) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(authBioTitle)
            .setSubtitle(lockSetupSubtitle)
            .setNegativeButtonText(lockNotNow)
            .build()
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) onUnlocked()
    }

    LaunchedEffect(uiState.biometricSetupPending) {
        if (uiState.biometricSetupPending) {
            val cipher = viewModel.getSetupCipher()
            if (cipher != null) {
                bioOperation.value = BIO_SETUP
                biometricPrompt.authenticate(setupPromptInfo, BiometricPrompt.CryptoObject(cipher))
            } else {
                viewModel.skipBiometricSetup()
            }
        }
    }

    suspend fun tryAutoLaunchBiometric() {
        if (showBiometricUnlock && bioOperation.value == null && !uiState.isLoading && !uiState.success) {
            val cipher = viewModel.getUnlockCipher()
            if (cipher != null) {
                bioOperation.value = BIO_UNLOCK
                biometricPrompt.authenticate(unlockPromptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    LaunchedEffect(showBiometricUnlock) {
        if (showBiometricUnlock && !biometricAutoLaunched && !uiState.isLoading && !uiState.success) {
            biometricAutoLaunched = true
            tryAutoLaunchBiometric()
        }
    }

    val autoLockManager = LocalAutoLockManager.current
    LaunchedEffect(autoLockManager) {
        autoLockManager?.foregroundResumedFlow?.collect {
            tryAutoLaunchBiometric()
        }
    }


    LaunchedEffect(uiState.retryBiometricUnlock) {
        if (uiState.retryBiometricUnlock) {
            viewModel.consumeBiometricRetry()
            val cipher = viewModel.getUnlockCipher()
            if (cipher != null) {
                bioOperation.value = BIO_UNLOCK
                biometricPrompt.authenticate(unlockPromptInfo, BiometricPrompt.CryptoObject(cipher))
            } else {
                viewModel.onBiometricUnavailableTransient()
            }
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val animAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1100),
        label         = "lockAlpha"
    )
    val animScale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 1100),
        label         = "lockScale"
    )

    val lockBg         = Color(0xFF0D1B2A)
    val lockOnBg       = Color(0xFFE8EAF0)
    val lockOnVariant  = Color(0xFF9AA3B2)
    val lockPrimary    = NavyAccent

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(lockBg)
    ) {
        val logoSize = (maxHeight * 0.20f).coerceIn(90.dp, 180.dp)

        val scrollState = rememberScrollState()
        val density = androidx.compose.ui.platform.LocalDensity.current
        val rootHeightPx = androidx.compose.ui.platform.LocalView.current.rootView.height
        val imeBottom = WindowInsets.ime.getBottom(density)
        var pillBottomPx by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
        LaunchedEffect(imeBottom, pillBottomPx) {
            if (imeBottom > 0 && pillBottomPx > 0f) {
                val keyboardTop = rootHeightPx - imeBottom
                val overlap = pillBottomPx + with(density) { 20.dp.toPx() } - keyboardTop
                if (overlap > 1f) scrollState.animateScrollBy(overlap)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(animAlpha)
                .scale(animScale)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 7.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            AppLogoWithVignette(size = logoSize.value.toInt(), bgColor = lockBg)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text  = "KeyArmor",
                style = TextStyle(
                    fontFamily    = InterFamily,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 30.sp,
                    letterSpacing = 1.sp
                ),
                color = lockOnBg
            )
            Text(
                text      = if (uiState.name.isNotBlank())
                                stringResource(R.string.auth_greeting, uiState.name)
                            else
                                stringResource(R.string.lock_protection_subtitle),
                style     = MaterialTheme.typography.titleMedium,
                color     = lockOnVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(lockOnVariant.copy(alpha = 0.10f))
            ) {
                androidx.compose.material3.TextField(
                    value                = uiState.password,
                    onValueChange        = viewModel::onPasswordChange,
                    label                = { Text(stringResource(R.string.auth_master_password)) },
                    singleLine           = true,
                    modifier             = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon         = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector        = if (showPassword) Icons.Default.VisibilityOff
                                                     else Icons.Default.Visibility,
                                contentDescription = null,
                                tint               = lockOnVariant
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done,
                        autoCorrectEnabled  = false
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus(); viewModel.unlock() }
                    ),
                    shape  = RoundedCornerShape(0.dp),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor        = lockOnBg,
                        unfocusedTextColor      = lockOnBg,
                        focusedLabelColor       = lockPrimary,
                        unfocusedLabelColor     = lockOnVariant,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor             = lockPrimary,
                        focusedTrailingIconColor   = lockOnVariant,
                        unfocusedTrailingIconColor = lockOnVariant
                    )
                )

                HorizontalDivider(
                    modifier  = Modifier.padding(start = 16.dp),
                    thickness = 0.5.dp,
                    color     = lockOnVariant
                )
                androidx.compose.foundation.layout.Box(
                    modifier         = Modifier.fillMaxWidth().padding(end = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    ForgotMasterPasswordLink(linkColor = lockOnVariant)
                }

            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text     = uiState.error!!.asString(),
                    color    = WarningAmber,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val canUnlock = uiState.password.length >= com.thormakk.keyarmor.presentation.screens.panicpin.PANIC_PIN_LENGTH
            val pillOn    = canUnlock || uiState.isLoading
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { pillBottomPx = it.positionInWindow().y + it.size.height }
            ) {
                TextButton(
                    onClick  = { focusManager.clearFocus(); viewModel.unlock() },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    enabled  = canUnlock && !uiState.isLoading,
                    shape    = RoundedCornerShape(50),
                    colors   = ButtonDefaults.textButtonColors(
                        containerColor         = if (pillOn) Cream200 else Color.Transparent,
                        contentColor           = Color.Black,
                        disabledContainerColor = if (pillOn) Cream200 else Color.Transparent,
                        disabledContentColor   = lockOnVariant.copy(alpha = 0.5f)
                    ),
                    border   = androidx.compose.foundation.BorderStroke(
                        0.5.dp, if (pillOn) Cream200 else lockOnVariant.copy(alpha = 0.4f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text       = stringResource(R.string.lock_action_unlock),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (showBiometricUnlock) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 25.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        modifier  = Modifier.weight(1f),
                        color     = lockOnVariant.copy(alpha = 0.25f)
                    )
                    Text(
                        text  = stringResource(R.string.label_or),
                        style = MaterialTheme.typography.bodySmall,
                        color = lockOnVariant
                    )
                    HorizontalDivider(
                        modifier  = Modifier.weight(1f),
                        color     = lockOnVariant.copy(alpha = 0.25f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (showBiometricUnlock) {
                val fpPulse = rememberInfiniteTransition(label = "fp_pulse")
                val fpProgress by fpPulse.animateFloat(
                    initialValue = 0f,
                    targetValue  = 1f,
                    animationSpec = infiniteRepeatable(
                        animation  = tween(durationMillis = 2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "fp_progress"
                )
                val fpColor = lockPrimary

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp).height(52.dp)) {
                    TextButton(
                        enabled = bioOperation.value == null,
                        onClick  = {
                            val cipher = viewModel.getUnlockCipher()
                            if (cipher != null) {
                                bioOperation.value = BIO_UNLOCK
                                biometricPrompt.authenticate(
                                    unlockPromptInfo,
                                    BiometricPrompt.CryptoObject(cipher)
                                )
                            } else {
                                viewModel.onBiometricUnavailableTransient()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .drawBehind {
                                    val center    = Offset(size.width / 2f, size.height / 2f)
                                    val minRadius = size.minDimension / 2f
                                    val maxRadius = 52.dp.toPx() / 2f
                                    listOf(0f, 1f / 3f, 2f / 3f).forEach { offset ->
                                        val p = (fpProgress + offset) % 1f
                                        drawCircle(
                                            color  = fpColor.copy(alpha = (1f - p) * 0.45f),
                                            radius = minRadius + (maxRadius - minRadius) * p,
                                            center = center,
                                            style  = Stroke(width = 1.5.dp.toPx())
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint               = lockPrimary,
                                modifier           = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text       = stringResource(R.string.lock_action_biometric),
                            color      = lockPrimary,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

        }
    }
}
