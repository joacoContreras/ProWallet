package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import androidx.compose.ui.tooling.preview.Preview
import com.undef.prowallet.ui.components.CustomTextField
import com.undef.prowallet.ui.components.PrimaryButton
import com.undef.prowallet.ui.components.SectionCard
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.AuthViewModel
import com.undef.prowallet.viewmodel.SettingsViewModel

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ProWalletTheme {
        SettingsScreen(
            authViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            settingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            onNavigateBack = {}
        )
    }
}

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    var fullName by remember(authState.user) { mutableStateOf(authState.user?.fullName ?: "") }
    val email = authState.user?.email ?: ""
    var notificationsEnabled by remember { mutableStateOf(true) }
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState()
    val darkModeEnabled by settingsViewModel.darkMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val profileSavedMsg = stringResource(R.string.profile_saved_msg)

    LaunchedEffect(authState.profileUpdated) {
        if (authState.profileUpdated) {
            snackbarHostState.showSnackbar(profileSavedMsg)
            authViewModel.clearProfileUpdated()
        }
    }

    val onBiometricToggle: (Boolean) -> Unit = { enable ->
        if (!enable) {
            settingsViewModel.setBiometricEnabled(false)
        } else {
            val biometricManager = BiometricManager.from(context)
            val canAuth = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                val activity = context as? FragmentActivity
                if (activity != null) {
                    val executor = ContextCompat.getMainExecutor(context)
                    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            settingsViewModel.setBiometricEnabled(true)
                        }
                    })
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle(context.getString(R.string.settings_biometric_prompt_title))
                        .setSubtitle(context.getString(R.string.settings_biometric_prompt_subtitle))
                        .setNegativeButtonText(context.getString(R.string.cancel))
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                        .build()
                    prompt.authenticate(promptInfo)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight
    ) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(title = stringResource(R.string.settings_title), onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Photo & Basic Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = PrimaryDarker)
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Secondary)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.settings_change_photo), tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    CustomTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = stringResource(R.string.full_name_label),
                        leadingIcon = Icons.Default.Person,
                        label = stringResource(R.string.full_name_label)
                    )
                    CustomTextField(
                        value = email,
                        onValueChange = { },
                        placeholder = stringResource(R.string.email_label),
                        leadingIcon = Icons.Default.Email,
                        label = stringResource(R.string.email_label),
                        readOnly = true
                    )

                    PrimaryButton(
                        text = stringResource(R.string.save_profile_button),
                        onClick = { authViewModel.updateProfileName(fullName) },
                        enabled = !authState.isLoading && fullName.isNotBlank() && fullName != (authState.user?.fullName ?: ""),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // App Settings
            Text(
                text = stringResource(R.string.app_settings_section),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Neutral,
                letterSpacing = 1.sp
            )

            SectionCard {
                SettingsSwitch(
                    icon = Icons.Default.Notifications,
                    iconBg = Primary.copy(alpha = 0.15f),
                    iconTint = PrimaryDarker,
                    title = stringResource(R.string.settings_notifications_title),
                    subtitle = stringResource(R.string.settings_notifications_subtitle),
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                HorizontalDivider(color = Color(0xFFF8F8F8))
                SettingsSwitch(
                    icon = Icons.Default.Fingerprint,
                    iconBg = ErrorRed.copy(alpha = 0.1f),
                    iconTint = ErrorRed,
                    title = stringResource(R.string.settings_biometrics_title),
                    subtitle = stringResource(R.string.settings_biometrics_subtitle),
                    checked = biometricEnabled,
                    onCheckedChange = onBiometricToggle
                )
                HorizontalDivider(color = Color(0xFFF8F8F8))
                SettingsSwitch(
                    icon = Icons.Default.DarkMode,
                    iconBg = NeutralDark.copy(alpha = 0.1f),
                    iconTint = NeutralDark,
                    title = stringResource(R.string.settings_dark_mode_title),
                    subtitle = stringResource(R.string.settings_dark_mode_subtitle),
                    checked = darkModeEnabled,
                    onCheckedChange = { settingsViewModel.setDarkMode(it) }
                )
            }

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.app_name).uppercase()} v1.0.0",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = NeutralLight,
                    letterSpacing = 1.sp
                )
                Text(
                    text = stringResource(R.string.developed_for),
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = NeutralLight
                )
            }
        }
    }
    }
}

@Composable
private fun SettingsSwitch(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 12.sp, color = Neutral)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Secondary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = NeutralLight
            )
        )
    }
}
