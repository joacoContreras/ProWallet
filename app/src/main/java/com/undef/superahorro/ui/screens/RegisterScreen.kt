package com.undef.superahorro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.superahorro.ui.components.CustomTextField
import com.undef.superahorro.ui.components.PrimaryButton
import com.undef.superahorro.ui.theme.*
import com.undef.superahorro.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(state.registrationSuccess) {
        if (state.registrationSuccess) {
            viewModel.clearRegistrationSuccess()
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Primary, PrimaryDark))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ProWallet",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = SecondaryDark
                    )
                    Text(
                        text = "Comienza tu viaje hacia la calma financiera.",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Neutral
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Tu nombre completo",
                        leadingIcon = Icons.Default.Person,
                        label = "Full Name"
                    )
                    CustomTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "tu@correo.com",
                        leadingIcon = Icons.Default.Email,
                        label = "Email"
                    )
                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        label = "Password"
                    )
                    CustomTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        label = "Confirm Password"
                    )
                }

                PrimaryButton(
                    text = "Crear Cuenta",
                    onClick = { viewModel.register(fullName, email, password) },
                    enabled = !state.isLoading
                )

                Text(
                    text = buildAnnotatedString {
                        append("¿Ya tienes cuenta? ")
                        withStyle(SpanStyle(color = Secondary, fontWeight = FontWeight.SemiBold)) {
                            append("Iniciar sesión")
                        }
                    },
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}
