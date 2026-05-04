package com.undef.superahorro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.superahorro.ui.components.SectionCard
import com.undef.superahorro.ui.components.TopBar
import com.undef.superahorro.ui.theme.*

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var budgetAlertsEnabled by remember { mutableStateOf(true) }
    var weeklyReportEnabled by remember { mutableStateOf(true) }
    var currencySync by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(title = "Configuración", onNavigateBack = onNavigateBack)

        Spacer(Modifier.height(8.dp))

        // Notifications section
        SectionLabel(text = "NOTIFICACIONES")
        SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsSwitch(
                icon = Icons.Default.Notifications,
                iconBg = Primary.copy(alpha = 0.15f),
                iconTint = PrimaryDarker,
                title = "Notificaciones",
                subtitle = "Recibir alertas de gastos",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            Divider(color = Color(0xFFF0F0F0))
            SettingsSwitch(
                icon = Icons.Default.NotificationsActive,
                iconBg = Secondary.copy(alpha = 0.1f),
                iconTint = Secondary,
                title = "Alertas de presupuesto",
                subtitle = "Cuando superes el 80% del mes",
                checked = budgetAlertsEnabled,
                onCheckedChange = { budgetAlertsEnabled = it }
            )
            Divider(color = Color(0xFFF0F0F0))
            SettingsSwitch(
                icon = Icons.Default.Assessment,
                iconBg = Tertiary,
                iconTint = SecondaryDark,
                title = "Reporte semanal",
                subtitle = "Resumen de gastos cada lunes",
                checked = weeklyReportEnabled,
                onCheckedChange = { weeklyReportEnabled = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Security section
        SectionLabel(text = "SEGURIDAD")
        SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsSwitch(
                icon = Icons.Default.Fingerprint,
                iconBg = ErrorRed.copy(alpha = 0.1f),
                iconTint = ErrorRed,
                title = "Biometría",
                subtitle = "Desbloqueo con huella dactilar",
                checked = biometricEnabled,
                onCheckedChange = { biometricEnabled = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Appearance section
        SectionLabel(text = "APARIENCIA")
        SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsSwitch(
                icon = Icons.Default.DarkMode,
                iconBg = NeutralDark.copy(alpha = 0.1f),
                iconTint = NeutralDark,
                title = "Modo oscuro",
                subtitle = "Tema oscuro de la aplicación",
                checked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Data section
        SectionLabel(text = "DATOS")
        SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsSwitch(
                icon = Icons.Default.Sync,
                iconBg = Primary.copy(alpha = 0.15f),
                iconTint = PrimaryDarker,
                title = "Sincronización",
                subtitle = "Sync con moneda local automático",
                checked = currencySync,
                onCheckedChange = { currencySync = it }
            )
            Divider(color = Color(0xFFF0F0F0))
            SettingsItem(
                icon = Icons.Default.Download,
                iconBg = Secondary.copy(alpha = 0.1f),
                iconTint = Secondary,
                title = "Exportar datos",
                subtitle = "Descargar historial en CSV",
                onClick = {}
            )
            Divider(color = Color(0xFFF0F0F0))
            SettingsItem(
                icon = Icons.Default.DeleteForever,
                iconBg = ErrorRed.copy(alpha = 0.1f),
                iconTint = ErrorRed,
                title = "Eliminar cuenta",
                subtitle = "Esta acción es irreversible",
                onClick = {}
            )
        }

        Spacer(Modifier.height(24.dp))

        // Version info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SUPER AHORRO v1.0.0",
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                color = NeutralLight,
                letterSpacing = 1.sp
            )
            Text(
                text = "Desarrollado para Tecnologías Móviles · UNDEF",
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                color = NeutralLight
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = Neutral,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
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
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
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
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = Neutral
            )
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

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
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
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = Neutral
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NeutralLight)
    }
}
