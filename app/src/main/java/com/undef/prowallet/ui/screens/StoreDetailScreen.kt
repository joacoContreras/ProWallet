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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.StoreDetailViewModel

@Composable
fun StoreDetailScreen(
    storeName: String,
    viewModel: StoreDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(storeName) {
        viewModel.loadStore(storeName)
    }

    val totalMonthlySpend = state.totalThisMonth
    val lastMonthSpend = state.totalLastMonth
    val grandTotal = totalMonthlySpend.takeIf { it > 0.0 } ?: 1.0
    val thisMonthTag = if (totalMonthlySpend > lastMonthSpend && lastMonthSpend > 0)
        stringResource(R.string.peak_label) to ErrorRed
    else
        stringResource(R.string.safe_label) to SuccessGreen

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

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = storeName,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
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
                            text = "$${String.format(Locale.getDefault(), "%.2f", totalMonthlySpend)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            color = PrimaryDarker
                        )
                    }
                }
            }

            // Historical Comparison
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ComparisonCard(
                        label = stringResource(R.string.last_month),
                        amount = lastMonthSpend,
                        tagText = stringResource(R.string.safe_label),
                        tagColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ComparisonCard(
                        label = stringResource(R.string.this_month),
                        amount = totalMonthlySpend,
                        tagText = thisMonthTag.first,
                        tagColor = thisMonthTag.second,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Category Distribution
            if (state.categoryDistribution.isNotEmpty()) {
                item {
                    val colors = listOf(PrimaryDarker, Secondary, Neutral, ErrorRed)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = stringResource(R.string.category_distribution),
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
                                state.categoryDistribution.forEachIndexed { index, (cat, amt) ->
                                    DistributionRow(
                                        label = cat,
                                        amount = amt,
                                        percentage = (amt / grandTotal).toFloat(),
                                        color = colors[index % colors.size]
                                    )
                                }
                            }
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
                text = "$${String.format(Locale.getDefault(), "%.2f", amount)}",
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
                text = "$${String.format(Locale.getDefault(), "%.2f", amount)} (${(percentage * 100).toInt()}%)",
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
