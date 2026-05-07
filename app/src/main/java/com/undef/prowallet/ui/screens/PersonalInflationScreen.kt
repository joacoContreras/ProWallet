package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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

@Composable
fun PersonalInflationScreen(onNavigateBack: () -> Unit, onNavigateToNotifications: () -> Unit) {
    var selectedTab by remember { mutableStateOf("Monthly") }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.app_name),
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryDarker)
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Section: Monthly Inflation
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.monthly_inflation),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TextPrimary
                        )
                        
                        Surface(
                            color = Color(0xFFE7E8E8),
                            shape = RoundedCornerShape(percent = 50)
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                TabButton(stringResource(R.string.monthly), selectedTab == "Monthly") { selectedTab = "Monthly" }
                                TabButton(stringResource(R.string.quarterly), selectedTab == "Quarterly") { selectedTab = "Quarterly" }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(text = stringResource(R.string.personal_inflation_rate), fontSize = 14.sp, color = Neutral)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "+4.2%", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                        Spacer(Modifier.width(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                            Text(text = stringResource(R.string.vs_last_month_format, ""), fontSize = 12.sp, color = ErrorRed, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Insights, contentDescription = null, tint = PrimaryDarker)
                                }
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            
                            // Mock Chart
                            Row(
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                listOf(0.4f, 0.55f, 0.45f, 0.7f, 0.85f, 1f).forEachIndexed { index, height ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp)
                                            .fillMaxHeight(height)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(if (index == 5) PrimaryDarker else BackgroundLight)
                                    )
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("NOV", "DEC", "JAN", "FEB", "MAR", "APR").forEach { month ->
                                    Text(text = month, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Neutral, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }

            // Top Price Hikes
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(18.dp))
                        Text(text = stringResource(R.string.top_price_hikes_label), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Neutral, letterSpacing = 1.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        HikeCard(icon = Icons.Default.LocalGasStation, label = "Fuel", value = "+12.4%", color = ErrorRed, modifier = Modifier.weight(1f))
                        HikeCard(icon = Icons.Default.Restaurant, label = "Dining", value = "+8.1%", color = Secondary, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Price Changes by Product
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = stringResource(R.string.price_changes_by_product), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            ProductChangeItem(Icons.Default.Egg, "Organic Milk", "Groceries", "+$0.50", stringResource(R.string.since_last_month))
                            HorizontalDivider(color = Color(0xFFF8F8F8))
                            ProductChangeItem(Icons.Default.Wifi, "Internet Plan", "Bills", "+$5.00", stringResource(R.string.since_last_month))
                            HorizontalDivider(color = Color(0xFFF8F8F8))
                            ProductChangeItem(Icons.Default.LocalCafe, "Coffee Beans", "Groceries", "+$0.15", stringResource(R.string.since_last_month))
                            HorizontalDivider(color = Color(0xFFF8F8F8))
                            ProductChangeItem(Icons.Default.DirectionsCar, "Ride Share", "Transport", "+$1.25", stringResource(R.string.average_trip_increase))
                            
                            Button(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F3)),
                                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                            ) {
                                Text(stringResource(R.string.view_all_products), color = PrimaryDarker, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // AI Insight Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryDarker)
                ) {
                    Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.ai_savings_tip), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Based on your spending, switching to a quarterly grocery subscription could save you $42/month against rising dairy costs.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color.Transparent,
            contentColor = if (isSelected) PrimaryDarker else Neutral
        ),
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HikeCard(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = null, tint = color)
            Column {
                Text(text = label, fontSize = 12.sp, color = Neutral)
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
fun ProductChangeItem(icon: ImageVector, name: String, category: String, change: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Neutral)
            }
            Column {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text(text = category, fontSize = 12.sp, color = Neutral)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = change, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ErrorRed)
            Text(text = desc.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Neutral)
        }
    }
}
