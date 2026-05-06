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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.CustomTextField
import com.undef.prowallet.ui.components.PrimaryButton
import com.undef.prowallet.ui.components.SectionCard
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    var fullName by remember { mutableStateOf("Alex Rivera") }
    var email by remember { mutableStateOf("alex.rivera@pro.wallet") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                            Icon(Icons.Default.CameraAlt, contentDescription = "Change photo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    CustomTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Full Name",
                        leadingIcon = Icons.Default.Person,
                        label = "Full Name"
                    )
                    CustomTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email",
                        leadingIcon = Icons.Default.Email,
                        label = "Email"
                    )
                    
                    PrimaryButton(
                        text = "Save Profile",
                        onClick = { },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // App Settings
            Text(
                text = "APP SETTINGS",
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
                    title = "Notifications",
                    subtitle = "Receive spending alerts",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                Divider(color = Color(0xFFF8F8F8))
                SettingsSwitch(
                    icon = Icons.Default.Fingerprint,
                    iconBg = ErrorRed.copy(alpha = 0.1f),
                    iconTint = ErrorRed,
                    title = "Biometrics",
                    subtitle = "Unlock with fingerprint",
                    checked = biometricEnabled,
                    onCheckedChange = { biometricEnabled = it }
                )
                Divider(color = Color(0xFFF8F8F8))
                SettingsSwitch(
                    icon = Icons.Default.DarkMode,
                    iconBg = NeutralDark.copy(alpha = 0.1f),
                    iconTint = NeutralDark,
                    title = "Dark Mode",
                    subtitle = "Application dark theme",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
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
