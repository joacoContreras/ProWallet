package com.undef.prowallet.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.undef.prowallet.R
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    biometricEnabled: Boolean,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val context = LocalContext.current
    var statusText by remember { mutableStateOf<String?>(null) }
    var showContinueButton by remember { mutableStateOf(false) }
    var showRetryButtons by remember { mutableStateOf(false) }

    fun launchBiometricPrompt() {
        val activity = context as? FragmentActivity ?: return
        statusText = context.getString(R.string.splash_verifying_identity)
        showContinueButton = false
        showRetryButtons = false
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onNavigateToHome()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Distinguimos cancelación explícita del usuario de un error real (lockout,
                // hardware, etc.) en vez de mandar siempre a Login sin explicar qué pasó.
                val cancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                statusText = context.getString(
                    if (cancelled) R.string.biometric_cancelled_message else R.string.biometric_error_message
                )
                showRetryButtons = true
            }

            override fun onAuthenticationFailed() {
                statusText = context.getString(R.string.splash_biometric_failed)
            }
        })
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.settings_biometric_prompt_title))
            .setSubtitle(context.getString(R.string.splash_biometric_prompt_subtitle))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
        prompt.authenticate(promptInfo)
    }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(1500)

        // Espera la validación real contra Room (AuthViewModel.init), no el flag crudo de
        // DataStore: si el usuario fue borrado de Room mientras la app estaba cerrada,
        // sessionValidated solo se vuelve true después de limpiar la sesión.
        val validatedState = authViewModel.uiState.first { it.sessionValidated }

        if (!validatedState.isLoggedIn) {
            onNavigateToLogin()
            return@LaunchedEffect
        }

        if (!biometricEnabled) {
            onNavigateToHome()
            return@LaunchedEffect
        }

        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        val activity = context as? FragmentActivity

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS || activity == null) {
            // Sin hardware/huella enrolada: se avisa explícitamente en vez de pasar en
            // silencio a Home. El usuario decide cuándo continuar.
            statusText = context.getString(R.string.biometric_not_available_message)
            showContinueButton = true
            return@LaunchedEffect
        }

        launchBiometricPrompt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Tertiary, Color.White)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Primary, PrimaryDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }

            Text(
                text = stringResource(id = R.string.app_name).uppercase(),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = SecondaryDark,
                modifier = Modifier.scale(scale.value)
            )

            Text(
                text = stringResource(id = R.string.splash_subtitle),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Neutral,
                modifier = Modifier.scale(scale.value)
            )

            statusText?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = SecondaryDark
                )
            }

            if (showContinueButton) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onNavigateToHome) {
                    Text(stringResource(R.string.continue_label))
                }
            }

            if (showRetryButtons) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { launchBiometricPrompt() }) {
                        Text(stringResource(R.string.retry_label))
                    }
                    OutlinedButton(onClick = onNavigateToLogin) {
                        Text(stringResource(R.string.back_to_login_label))
                    }
                }
            }
        }
    }
}
