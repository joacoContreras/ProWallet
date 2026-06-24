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
import java.util.Calendar
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
    onNavigateToTopStores: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val currentMonth = remember {
        java.text.SimpleDateFormat("MMM", Locale.getDefault())
            .format(Calendar.getInstance().time).uppercase(Locale.getDefault())
    }

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
                AnalyticsHeader()
            }

            item {
                MainSpendCard(
                    totalSpentMonth = state.totalSpentMonth,
                    percentageVsLastMonth = state.percentageVsLastMonth,
                    currentMonth = currentMonth
                )
            }

            item {
                StatsCardsRow(
                    highestSpend = state.highestSpend,
                    highestSpendStore = state.highestSpendStore,
                    averagePurchase = state.averagePurchase,
                    totalTransactions = state.totalTransactions
                )
            }

            item {
                MonthlyTrendCard(monthlyTrend = state.monthlyTrend)
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
                MostPurchasedProductsSection(mostPurchasedProducts = state.mostPurchasedProducts)
            }

            item {
                TopStoresSection(
                    topStores = state.topStores,
                    onNavigateToTopStores = onNavigateToTopStores
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
