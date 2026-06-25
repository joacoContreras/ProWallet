package com.undef.prowallet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.AuthViewModel

@Composable
fun VerifyCodeScreen(
    viewModel: AuthViewModel,
    onVerified: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val code = remember { mutableStateListOf("", "", "", "", "", "") }

    LaunchedEffect(state.codeVerified) {
        if (state.codeVerified) onVerified()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Primary)
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
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

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo_with_bg),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.verify_identity),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = TextPrimary
            )
            Text(
                text = stringResource(R.string.verify_code_subtitle),
                fontFamily = PlusJakartaSans,
                fontSize = 16.sp,
                color = Neutral,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            state.simulatedCode?.let { simulatedCode ->
                Surface(
                    color = SecondaryLight.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.simulated_code_notice, simulatedCode),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryDark,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

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
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(6) { index ->
                            OutlinedTextField(
                                value = code[index],
                                onValueChange = { if (it.length <= 1) code[index] = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = BackgroundLight,
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }

                    state.error?.let { error ->
                        Text(
                            text = stringResource(error.messageRes),
                            color = ErrorRed,
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = { viewModel.verifyCode(code.joinToString("")) },
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
                                text = stringResource(R.string.verify_code_button),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }

                    TextButton(onClick = { viewModel.resendCode() }, enabled = !state.isLoading) {
                        Text(
                            text = stringResource(R.string.resend_code),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Secondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Surface(
                color = SecondaryLight.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
                    Text(
                        text = stringResource(R.string.secured_by_prowallet),
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Secondary
                    )
                }
            }
        }
    }
}
