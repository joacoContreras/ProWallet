package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.BottomNavBar
import com.undef.prowallet.ui.components.SectionCard
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToNewPurchase: () -> Unit,
    onNavigateToTopStores: () -> Unit,
    onNavigateToPersonalInflation: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "analytics",
                onHomeClick = onNavigateToHome,
                onNewClick = onNavigateToNewPurchase,
                onAnalyticsClick = {}
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.analytics_tab),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.analytics_subtitle),
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = Neutral
                        )
                    }
                    IconButton(
                        onClick = onNavigateToPersonalInflation,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = stringResource(R.string.personal_inflation_rate), tint = PrimaryDarker)
                    }
                }
            }

            item {
                // Main spend card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.total_spent_label, "OCT"),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = SecondaryDark,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", state.totalSpentMonth)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = SecondaryDark
                        )
                        Text(
                            text = stringResource(R.string.vs_last_month_format, "▲ 12%"),
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = SecondaryDark.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Highest spend
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ErrorRed)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$${String.format(Locale.getDefault(), "%.2f", state.highestSpend)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(R.string.highest_spend_label),
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = Neutral
                            )
                            Text(
                                text = state.highestSpendStore,
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                color = NeutralLight
                            )
                        }
                    }

                    // Average purchase
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Secondary)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$${String.format(Locale.getDefault(), "%.2f", state.averagePurchase)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(R.string.average_purchase_label),
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = Neutral
                            )
                            Text(
                                text = stringResource(R.string.across_transactions_format, state.totalTransactions),
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                color = NeutralLight
                            )
                        }
                    }
                }
            }

            item {
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.monthly_trend),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.monthly_trend_6mo),
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = Secondary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Chart placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Simple bar chart representation
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val maxVal = state.monthlyTrend.maxOfOrNull { it.second } ?: 1f
                                state.monthlyTrend.forEach { (month, value) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        val barHeight = ((value / maxVal) * 70).dp
                                        Box(
                                            modifier = Modifier
                                                .width(28.dp)
                                                .height(barHeight)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(
                                                    if (month == "Oct") Secondary else Primary.copy(alpha = 0.6f)
                                                )
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = month,
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 10.sp,
                                            color = if (month == "Oct") Secondary else Neutral
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.most_purchased_products),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            }

            item {
                SectionCard {
                    if (state.mostPurchasedProducts.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_data_available),
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            color = Neutral,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        state.mostPurchasedProducts.forEachIndexed { idx, (name, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Secondary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Secondary
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.times_format, count),
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = Neutral
                                )
                            }
                            if (idx < state.mostPurchasedProducts.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                            }
                        }
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
                        text = stringResource(R.string.top_stores),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    IconButton(onClick = onNavigateToTopStores) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = stringResource(R.string.see_all), tint = Secondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            item {
                SectionCard {
                    state.topStores.forEachIndexed { idx, (store, amount, ratio) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    tint = PrimaryDarker,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = store,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (idx == 0) Secondary else Primary,
                                    trackColor = Color(0xFFEEEEEE)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "$${String.format(Locale.getDefault(), "%.2f", amount)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                        if (idx < state.topStores.lastIndex) {
                            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
