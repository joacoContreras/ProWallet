package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.data.MockRepository
import com.undef.prowallet.ui.components.CustomTextField
import com.undef.prowallet.ui.components.PrimaryButton
import com.undef.prowallet.ui.components.SectionCard
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var fullName by remember { mutableStateOf(MockRepository.currentUser.fullName) }
    var email by remember { mutableStateOf(MockRepository.currentUser.email) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(
            title = stringResource(R.string.profile_title),
            onNavigateBack = onNavigateBack,
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                }
            }
        )

        // Avatar section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Primary, Secondary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fullName.take(1).uppercase(),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = fullName,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Text(
                    text = email,
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    color = Neutral
                )
            }
        }

        // Form
        SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = stringResource(R.string.personal_info_section),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Neutral,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = stringResource(R.string.full_name_placeholder),
                    leadingIcon = Icons.Default.Person,
                    label = stringResource(R.string.full_name_label)
                )
                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = stringResource(R.string.email_placeholder),
                    leadingIcon = Icons.Default.Email,
                    label = stringResource(R.string.email_label)
                )
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(text = stringResource(R.string.save_changes_button), onClick = {})
        }

        Spacer(Modifier.height(16.dp))

        // Stats card
        SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = stringResource(R.string.statistics_section),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Neutral,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = stringResource(R.string.stat_purchases), value = "6")
                StatItem(label = stringResource(R.string.stat_this_month), value = "$1,842")
                StatItem(label = stringResource(R.string.stat_savings), value = "12%")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Logout button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(54.dp),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.logout_button),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Secondary
        )
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp,
            color = Neutral
        )
    }
}
