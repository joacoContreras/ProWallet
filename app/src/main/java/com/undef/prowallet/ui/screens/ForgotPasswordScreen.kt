package com.undef.prowallet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.CustomTextField
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onCodeSent: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }

    LaunchedEffect(state.resetEmailSent) {
        if (state.resetEmailSent) onCodeSent()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BackgroundLight)
                ) {
                    Icon(Icons.Default.Help, contentDescription = "Help", tint = Primary)
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Hero Image
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo_with_bg),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.forgot_password_title),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.forgot_password_subtitle),
                        fontFamily = PlusJakartaSans,
                        fontSize = 16.sp,
                        color = Neutral,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    CustomTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = stringResource(R.string.email_example_placeholder),
                        leadingIcon = Icons.Default.Email,
                        label = stringResource(R.string.email_address_label)
                    )

                    state.error?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = ErrorRed,
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = { viewModel.sendResetCode(email) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !state.isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.send_code_button),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onNavigateBack() }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.back_to_login),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Primary
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = stringResource(R.string.copyright_text),
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = NeutralLight,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}
