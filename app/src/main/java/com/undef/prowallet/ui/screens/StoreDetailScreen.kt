package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*

@Composable
fun StoreDetailScreen(
    storeName: String,
    onNavigateBack: () -> Unit
) {
    // Mock data for the specific store based on the design
    val totalMonthlySpend = 412.50
    val lastMonthSpend = 315.20
    val merchantId = "#88219-SBX"

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.store_detail_title),
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Neutral)
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
            // Hero Section: Merchant Identity
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = PrimaryDarker,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            text = storeName,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Merchant ID: $merchantId",
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = Neutral
                        )
                        
                        Spacer(Modifier.height(20.dp))
                        Divider(color = Color(0xFFF0F0F0))
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            text = stringResource(R.string.total_monthly_spend).uppercase(),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Neutral,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$${String.format("%.2f", totalMonthlySpend)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            color = PrimaryDarker
                        )
                    }
                }
            }

            // Unusual Activity Alert
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ErrorRed.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Unusual Activity Detected",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ErrorRed
                            )
                            Text(
                                text = "Your spending at $storeName is 24% higher than your 6-month average.",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                color = ErrorRed.copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Historical Comparison
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ComparisonCard(
                        label = "Last Month",
                        amount = lastMonthSpend,
                        tagText = "Safe",
                        tagColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ComparisonCard(
                        label = "This Month",
                        amount = totalMonthlySpend,
                        tagText = "Peak",
                        tagColor = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Category Distribution
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Category Distribution",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DistributionRow(label = "Beverages & Coffee", amount = 320.00, percentage = 0.78f, color = PrimaryDarker)
                            DistributionRow(label = "Food & Snacks", amount = 72.50, percentage = 0.18f, color = Secondary)
                            DistributionRow(label = "Merchandise", amount = 20.00, percentage = 0.04f, color = Neutral)
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ComparisonCard(
    label: String,
    amount: Double,
    tagText: String,
    tagColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = label, fontFamily = PlusJakartaSans, fontSize = 12.sp, color = Neutral)
            Text(
                text = "$${String.format("%.2f", amount)}",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )
            Surface(
                color = tagColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(
                    text = tagText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = tagColor
                )
            }
        }
    }
}

@Composable
fun DistributionRow(label: String, amount: Double, percentage: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontFamily = PlusJakartaSans, fontSize = 13.sp, color = TextPrimary)
            Text(
                text = "$${String.format("%.2f", amount)} (${(percentage * 100).toInt()}%)",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = BackgroundLight
        )
    }
}
