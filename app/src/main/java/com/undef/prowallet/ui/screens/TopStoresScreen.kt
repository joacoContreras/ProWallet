package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.undef.prowallet.viewmodel.StoreEntry
import com.undef.prowallet.viewmodel.TopStoresViewModel

@Composable
fun TopStoresScreen(
    viewModel: TopStoresViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStoreDetail: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredStores = if (searchQuery.isBlank()) state.stores else state.stores.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        TopBar(
            title = stringResource(R.string.top_stores_title),
            onNavigateBack = onNavigateBack,
            actions = {
                IconButton(onClick = { showSearch = !showSearch; if (!showSearch) searchQuery = "" }) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.see_all), tint = PrimaryDarker)
                }
            }
        )

        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_store_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

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
                                val vsLastMonthSign = if (state.percentageVsLastMonth >= 0) "+" else ""
                                Text(
                                    text = stringResource(R.string.from_last_month_format, "$vsLastMonthSign${state.percentageVsLastMonth}%"),
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

            if (filteredStores.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = NeutralLight, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (state.stores.isEmpty()) stringResource(R.string.no_purchases_title) else stringResource(R.string.no_store_matches),
                                fontFamily = PlusJakartaSans,
                                color = Neutral
                            )
                        }
                    }
                }
            } else {
                items(filteredStores) { store ->
                    StoreRankItem(
                        name = store.name,
                        transactions = store.transactionCount,
                        amount = store.totalAmount,
                        icon = Icons.Default.Store,
                        percentageVsLastMonth = store.percentageVsLastMonth,
                        onClick = { onNavigateToStoreDetail(store.name) }
                    )
                }
            }

            if (state.categoryDistribution.isNotEmpty()) {
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
                    val colors = listOf(PrimaryDarker, Secondary, Neutral, ErrorRed)
                    SectionCard {
                        state.categoryDistribution.forEachIndexed { index, entry ->
                            if (index > 0) Spacer(Modifier.height(16.dp))
                            DistributionItem(
                                label = entry.name,
                                percentage = entry.percentage,
                                color = colors[index % colors.size]
                            )
                        }
                    }
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
    percentageVsLastMonth: Int?,
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
                if (percentageVsLastMonth != null) {
                    val trendIcon = when {
                        percentageVsLastMonth > 0 -> Icons.AutoMirrored.Filled.TrendingUp
                        percentageVsLastMonth < 0 -> Icons.AutoMirrored.Filled.TrendingDown
                        else -> Icons.AutoMirrored.Filled.TrendingFlat
                    }
                    val trendColor = when {
                        percentageVsLastMonth > 0 -> ErrorRed
                        percentageVsLastMonth < 0 -> SuccessGreen
                        else -> Neutral
                    }
                    val sign = if (percentageVsLastMonth > 0) "+" else ""
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(trendIcon, contentDescription = null, tint = trendColor, modifier = Modifier.size(14.dp))
                        Text(
                            text = "$sign$percentageVsLastMonth%",
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
