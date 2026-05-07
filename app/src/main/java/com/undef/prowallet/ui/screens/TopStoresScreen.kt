package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.undef.prowallet.ui.components.SectionCard
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.HomeViewModel

@Composable
fun TopStoresScreen(
    homeViewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStoreDetail: (String) -> Unit
) {
    val state by homeViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        TopBar(
            title = stringResource(R.string.top_stores_title),
            onNavigateBack = onNavigateBack,
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.see_all), tint = PrimaryDarker)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Monthly Spending Insight Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.15f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = stringResource(R.string.monthly_spending_insight),
                                fontFamily = PlusJakartaSans,
                                fontSize = 14.sp,
                                color = PrimaryDarker
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "$${String.format("%.2f", state.totalSpentMonth)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp,
                                color = PrimaryDarker
                            )
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = PrimaryDarker,
                                shape = RoundedCornerShape(percent = 50)
                            ) {
                                Text(
                                    text = stringResource(R.string.from_last_month_format, "+12%"),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        // Abstract icon placeholder to match the wallet in mockup
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = PrimaryDarker.copy(alpha = 0.1f),
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 10.dp, y = 10.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.ranked_by_volume).uppercase(),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Neutral,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = Neutral,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Mock stores list based on state and mockup
            val rankedStores = listOf(
                Triple("Whole Foods Market", 1240.00, 12),
                Triple("Shell Energy", 850.20, 5),
                Triple("Amazon.com", 612.45, 28),
                Triple("Starbucks Reserve", 185.30, 15),
                Triple("Equinox Fitness", 150.00, 1)
            )

            itemsIndexed(rankedStores) { index, (name, amount, txs) ->
                val icon = when (name) {
                    "Whole Foods Market" -> Icons.Default.ShoppingBasket
                    "Shell Energy" -> Icons.Default.LocalGasStation
                    "Amazon.com" -> Icons.Default.Cloud
                    "Starbucks Reserve" -> Icons.Default.Coffee
                    else -> Icons.Default.FitnessCenter
                }
                
                val trend = when (index) {
                    0, 3 -> Icons.AutoMirrored.Filled.TrendingUp to ErrorRed
                    1 -> Icons.AutoMirrored.Filled.TrendingDown to SuccessGreen
                    else -> Icons.AutoMirrored.Filled.TrendingFlat to PrimaryDarker
                }
                
                val trendValue = when(index) {
                    0 -> "4%"
                    1 -> "2%"
                    3 -> "8%"
                    else -> "0%"
                }

                StoreRankItem(
                    name = name,
                    transactions = txs,
                    amount = amount,
                    icon = icon,
                    trendIcon = trend.first,
                    trendColor = trend.second,
                    trendValue = trendValue,
                    onClick = { onNavigateToStoreDetail(name) }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.spending_distribution).uppercase(),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Neutral,
                    letterSpacing = 1.sp
                )
            }

            item {
                SectionCard {
                    DistributionItem(label = stringResource(R.string.groceries_label), percentage = 0.45f, color = PrimaryDarker)
                    Spacer(Modifier.height(16.dp))
                    DistributionItem(label = stringResource(R.string.tech_digital_label), percentage = 0.30f, color = Secondary)
                }
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun StoreRankItem(
    name: String,
    transactions: Int,
    amount: Double,
    icon: ImageVector,
    trendIcon: ImageVector,
    trendColor: Color,
    trendValue: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(24.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = stringResource(R.string.transactions_format, transactions),
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Neutral
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", amount)}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PrimaryDarker
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(trendIcon, contentDescription = null, tint = trendColor, modifier = Modifier.size(14.dp))
                    Text(
                        text = trendValue,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = trendColor
                    )
                }
            }
        }
    }
}

@Composable
fun DistributionItem(label: String, percentage: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = color.copy(alpha = 0.6f),
            trackColor = TertiaryDark.copy(alpha = 0.5f)
        )
    }
}
