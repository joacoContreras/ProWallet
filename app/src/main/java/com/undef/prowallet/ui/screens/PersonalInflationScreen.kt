package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.InflationPeriod
import com.undef.prowallet.viewmodel.PersonalInflationViewModel
import com.undef.prowallet.viewmodel.ProductPriceChange
import kotlin.math.abs

@Composable
fun PersonalInflationScreen(onNavigateBack: () -> Unit, onNavigateToNotifications: () -> Unit) {
    val viewModel: PersonalInflationViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

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
                                TabButton(stringResource(R.string.monthly), state.period == InflationPeriod.MONTHLY) { viewModel.setPeriod(InflationPeriod.MONTHLY) }
                                TabButton(stringResource(R.string.quarterly), state.period == InflationPeriod.QUARTERLY) { viewModel.setPeriod(InflationPeriod.QUARTERLY) }
                            }
                        }
                    }

                    if (!state.hasEnoughData) {
                        EmptyInflationCard()
                    } else {
                        InflationRateCard(state.personalInflationRate, state.monthlyTrend)
                    }
                }
            }

            if (state.hasEnoughData) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(18.dp))
                            Text(text = stringResource(R.string.top_price_hikes_label), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Neutral, letterSpacing = 1.sp)
                        }
                        val hikes = state.priceChanges.filter { it.changePercent > 0 }.take(2)
                        if (hikes.isEmpty()) {
                            Text(text = stringResource(R.string.no_price_hikes), fontSize = 13.sp, color = Neutral)
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                hikes.forEach { hike ->
                                    HikeCard(
                                        label = hike.productName,
                                        value = "+${"%.1f".format(hike.changePercent)}%",
                                        color = ErrorRed,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(2 - hikes.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = stringResource(R.string.price_changes_by_product), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column {
                                state.priceChanges.take(8).forEachIndexed { index, change ->
                                    ProductChangeItem(change)
                                    if (index < state.priceChanges.take(8).lastIndex) {
                                        HorizontalDivider(color = Color(0xFFF8F8F8))
                                    }
                                }
                            }
                        }
                    }
                }

                state.insightText?.let { insight ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = PrimaryDarker)
                        ) {
                            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Icon(Icons.Default.Insights, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(stringResource(R.string.ai_savings_tip), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(insight, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun EmptyInflationCard() {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Insights, contentDescription = null, tint = NeutralLight, modifier = Modifier.size(40.dp))
            Text(
                text = stringResource(R.string.personal_inflation_empty_title),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.personal_inflation_empty_desc),
                fontSize = 13.sp,
                color = Neutral,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InflationRateCard(rate: Double, monthlyTrend: List<Pair<String, Float>>) {
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
                        val sign = if (rate >= 0) "+" else ""
                        Text(text = "$sign${"%.1f".format(rate)}%", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        val trendColor = if (rate >= 0) ErrorRed else SuccessGreen
                        Icon(
                            if (rate >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(text = stringResource(R.string.since_previous_period), fontSize = 12.sp, color = Neutral)
                }
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Insights, contentDescription = null, tint = PrimaryDarker)
                }
            }

            Spacer(Modifier.height(24.dp))

            val maxAbs = monthlyTrend.maxOfOrNull { abs(it.second) }?.takeIf { it > 0 } ?: 1f
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyTrend.forEach { (_, value) ->
                    val height = (abs(value) / maxAbs).coerceIn(0.05f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .fillMaxHeight(height)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(if (value > 0) ErrorRed.copy(alpha = 0.7f) else if (value < 0) SuccessGreen.copy(alpha = 0.7f) else BackgroundLight)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                monthlyTrend.forEach { (month, _) ->
                    Text(text = month.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Neutral, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
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
fun HikeCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = color)
            Column {
                Text(text = label, fontSize = 12.sp, color = Neutral, maxLines = 1)
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
fun ProductChangeItem(change: ProductPriceChange) {
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
                Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = Neutral)
            }
            Column {
                Text(text = change.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text(text = change.category, fontSize = 12.sp, color = Neutral)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            val delta = change.currentPrice - change.previousPrice
            val sign = if (delta >= 0) "+" else ""
            val color = if (delta > 0) ErrorRed else if (delta < 0) SuccessGreen else Neutral
            Text(text = "$sign$${"%.2f".format(delta)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            Text(text = stringResource(R.string.since_previous_period).uppercase(), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Neutral)
        }
    }
}
