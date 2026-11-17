package com.thormakk.keyarmor.presentation.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.ui.theme.Cream200
import com.thormakk.keyarmor.ui.theme.InterFamily
import com.thormakk.keyarmor.ui.theme.NavyAccent
import com.thormakk.keyarmor.ui.theme.WarningAmber
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.ui.theme.Cream300
import com.thormakk.keyarmor.ui.theme.PasswordStrengthFair
import com.thormakk.keyarmor.ui.theme.PasswordStrengthGood
import com.thormakk.keyarmor.ui.theme.PasswordStrengthStrong
import com.thormakk.keyarmor.ui.theme.PasswordStrengthWeak

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var showPassword        by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var understoodNoRecovery by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) onRegisterSuccess()
    }

    val lockBg        = Color(0xFF0D1B2A)
    val lockOnBg      = Color(0xFFE8EAF0)
    val lockOnVariant = Color(0xFF9AA3B2)
    val lockPrimary   = NavyAccent

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val animAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1100),
        label         = "registerAlpha"
    )
    val animScale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 1100),
        label         = "registerScale"
    )

    val fieldColors = TextFieldDefaults.colors(
        focusedTextColor        = lockOnBg,
        unfocusedTextColor      = lockOnBg,
        focusedLabelColor       = lockPrimary,
        unfocusedLabelColor     = lockOnVariant,
        cursorColor             = lockPrimary,
        focusedContainerColor   = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor   = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedTrailingIconColor   = lockOnVariant,
        unfocusedTrailingIconColor = lockOnVariant
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(lockBg)
    ) {
        val logoSize = (maxHeight * 0.20f).coerceIn(90.dp, 180.dp)

        val scrollState = rememberScrollState()
        val density = LocalDensity.current
        val rootHeightPx = LocalView.current.rootView.height
        val imeInsets = WindowInsets.ime
        var pillRestBottomPx by remember { mutableFloatStateOf(0f) }

        val canCreate = !uiState.isLoading &&
            understoodNoRecovery &&
            uiState.name.isNotBlank() &&
            uiState.password.length >= RegisterViewModel.MIN_LENGTH &&
            uiState.confirmPassword == uiState.password
        val pillOn = canCreate || uiState.isLoading

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(animAlpha)
                .scale(animScale)
                .graphicsLayer {
                    val imeBottom = imeInsets.getBottom(density)
                    if (imeBottom > 0 && pillRestBottomPx > 0f) {
                        val keyboardTop = rootHeightPx - imeBottom
                        val shift = pillRestBottomPx + 20.dp.toPx() - keyboardTop
                        translationY = -shift.coerceAtLeast(0f)
                    }
                }
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
                text      = stringResource(R.string.auth_create_intro),
                style     = MaterialTheme.typography.bodyMedium,
                color     = lockOnVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 25.dp).padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(lockOnVariant.copy(alpha = 0.10f))
            ) {
                TextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(stringResource(R.string.auth_your_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(0.dp),
                    colors = fieldColors
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = lockOnVariant)

                TextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text(stringResource(R.string.auth_master_password)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff
                                              else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                        autoCorrectEnabled = false
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(0.dp),
                    colors = fieldColors
                )

                if (uiState.password.isNotEmpty()) {
                    PasswordStrengthBar(
                        strength = viewModel.passwordStrength(uiState.password),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        emptyColor = lockOnVariant.copy(alpha = 0.25f)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = lockOnVariant)

                TextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = { Text(stringResource(R.string.auth_confirm_password)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff
                                              else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = false
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus(); viewModel.register() }
                    ),
                    shape = RoundedCornerShape(0.dp),
                    colors = fieldColors
                )
            }

            val muyCorta = uiState.password.isNotEmpty() &&
                uiState.password.length < RegisterViewModel.MIN_LENGTH
            val noCoinciden = uiState.confirmPassword.isNotEmpty() &&
                uiState.confirmPassword != uiState.password
            if (muyCorta || noCoinciden) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (muyCorta) {
                        stringResource(R.string.error_password_too_short)
                    } else {
                        stringResource(R.string.error_passwords_mismatch)
                    },
                    color = WarningAmber,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp)
                )
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error!!.asString(),
                    color = WarningAmber,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .clickable { understoodNoRecovery = !understoodNoRecovery },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked         = understoodNoRecovery,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor   = lockPrimary,
                        uncheckedColor = lockPrimary.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text  = stringResource(R.string.register_understand_no_recovery),
                    style = MaterialTheme.typography.bodySmall,
                    color = lockPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { focusManager.clearFocus(); viewModel.register() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .onGloballyPositioned {
                        if (imeInsets.getBottom(density) == 0 || pillRestBottomPx == 0f) {
                            pillRestBottomPx = it.positionInWindow().y + it.size.height
                        }
                    },
                enabled = canCreate,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.textButtonColors(
                    containerColor         = if (pillOn) Cream200 else Color.Transparent,
                    contentColor           = Color.Black,
                    disabledContainerColor = if (pillOn) Cream200 else Color.Transparent,
                    disabledContentColor   = lockOnVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    0.5.dp, if (pillOn) Cream200 else lockOnVariant.copy(alpha = 0.4f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.auth_create_vault),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthBar(
    strength:  Int,
    modifier:  Modifier = Modifier,
    barHeight: Dp       = 4.dp,
    emptyColor: Color   = Cream300
) {
    if (strength < 0) return
    val displaySegments = maxOf(1, strength)
    val color = when (strength) {
        0    -> PasswordStrengthWeak
        1    -> PasswordStrengthWeak
        2    -> PasswordStrengthFair
        3    -> PasswordStrengthGood
        else -> PasswordStrengthStrong
    }
    val label = when (strength) {
        0    -> stringResource(R.string.auth_strength_very_weak)
        1    -> stringResource(R.string.auth_strength_weak)
        2    -> stringResource(R.string.auth_strength_fair)
        3    -> stringResource(R.string.auth_strength_strong)
        else -> stringResource(R.string.auth_strength_very_strong)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(barHeight)
                        .background(
                            color = if (index < displaySegments) color else emptyColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AppLogoWithVignette(size: Int) {
    Box(
        modifier = Modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo_keyarmor),
            contentDescription = null,
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape((size / 5).dp)),
            contentScale = ContentScale.Fit
        )
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape((size / 5).dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.45f)
                        )
                    )
                )
        )
    }
}
