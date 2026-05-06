package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import java.util.Locale

@Composable
fun AutoSavingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    var savingsPercentage by remember { mutableFloatStateOf(10f) }
    var selectedMethod by remember { mutableStateOf("Percentage") }
    var selectedFrequency by remember { mutableStateOf("Monthly") }

    Scaffold(
        topBar = {
            TopBar(
                title = "Auto-Savings Plan",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryDarker)
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeutralLight)
                    )
                }
            )
        },
        containerColor = BackgroundLight,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Funds will be automatically transferred from your primary account on the scheduled date.",
                        fontSize = 12.sp,
                        color = Neutral,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { onNavigateBack() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarker)
                    ) {
                        Text("Activate Plan", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Progress Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(40.dp))
                        }
                        Column {
                            Text(text = "Savings Goal", fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = PrimaryDarker)
                            Text(text = "You've saved $12,450 this year.", fontSize = 14.sp, color = Neutral)
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { 0.65f },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                                color = PrimaryDarker,
                                trackColor = TertiaryDark.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            // Method Selection
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE7E8E8),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        MethodTab("Percentage", selectedMethod == "Percentage", modifier = Modifier.weight(1f)) { selectedMethod = "Percentage" }
                        MethodTab("Fixed Amount", selectedMethod == "Fixed Amount", modifier = Modifier.weight(1f)) { selectedMethod = "Fixed Amount" }
                    }
                }
            }

            // Percentage Setup
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Column {
                                Text(text = "Current Income", fontSize = 12.sp, color = Neutral)
                                Text(text = "$8,450.00", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                            }
                            Text(text = "${savingsPercentage.toInt()}%", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = PrimaryDarker)
                        }
                        
                        Slider(
                            value = savingsPercentage,
                            onValueChange = { savingsPercentage = it },
                            valueRange = 0f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryDarker,
                                activeTrackColor = PrimaryDarker,
                                inactiveTrackColor = TertiaryDark.copy(alpha = 0.5f)
                            )
                        )
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = BackgroundLight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Estimated Monthly Savings", fontSize = 14.sp, color = Neutral)
                                Text(text = "$${String.format(Locale.getDefault(), "%.2f", 8450 * (savingsPercentage / 100))}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryDarker)
                            }
                        }
                    }
                }
            }

            // Frequency
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Frequency", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Neutral, letterSpacing = 1.sp)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            FrequencyRow("Monthly", selectedFrequency == "Monthly") { selectedFrequency = "Monthly" }
                            HorizontalDivider(color = Color(0xFFF8F8F8))
                            FrequencyRow("Bi-weekly", selectedFrequency == "Bi-weekly") { selectedFrequency = "Bi-weekly" }
                            HorizontalDivider(color = Color(0xFFF8F8F8))
                            FrequencyRow("Weekly", selectedFrequency == "Weekly") { selectedFrequency = "Weekly" }
                        }
                    }
                }
            }

            // Destination Fund
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Destination Fund", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Neutral, letterSpacing = 1.sp)
                    DestinationFundCard(Icons.Default.Savings, "Main Savings", "APY 4.25%", Secondary.copy(alpha = 0.1f)) {
                        // Potential navigation to fund details
                    }
                }
            }

            item { Spacer(Modifier.height(140.dp)) }
        }
    }
}

@Composable
fun MethodTab(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PrimaryDarker else Color.Transparent,
            contentColor = if (isSelected) Color.White else Neutral
        ),
        shape = RoundedCornerShape(percent = 50),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FrequencyRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = if (isSelected) PrimaryDarker else TextPrimary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(20.dp))
        } else {
            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(BackgroundLight))
        }
    }
}

@Composable
fun DestinationFundCard(icon: ImageVector, title: String, subtitle: String, iconBg: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Secondary, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text(text = subtitle, fontSize = 12.sp, color = Neutral)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Neutral)
        }
    }
}
