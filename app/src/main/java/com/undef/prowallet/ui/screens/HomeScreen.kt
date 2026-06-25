package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
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
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.BottomNavBar
import com.undef.prowallet.ui.components.PurchaseCard
import com.undef.prowallet.ui.components.SectionCard
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.HomeViewModel

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ProWalletTheme {
        HomeScreen(
            homeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            onNavigateToNewPurchase = {},
            onNavigateToAnalytics = {},
            onNavigateToPurchaseDetail = {},
            onNavigateToHistory = {},
            onNavigateToProfile = {},
            onNavigateToNotifications = {},
            onNavigateToMonthlySetup = {}
        )
    }
}

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onNavigateToNewPurchase: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPurchaseDetail: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToMonthlySetup: () -> Unit
) {
    val state by homeViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "home",
                onHomeClick = {},
                onNewClick = onNavigateToNewPurchase,
                onAnalyticsClick = onNavigateToAnalytics
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                HomeHeader(
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToNotifications = onNavigateToNotifications
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                MonthlySpendCard(
                    totalMonthlySpend = state.totalMonthlySpend,
                    budgetPercent = state.budgetPercent,
                    budgetProgress = state.budgetProgress,
                    monthlyIncome = state.monthlyIncome,
                    onNavigateToMonthlySetup = onNavigateToMonthlySetup,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                QuickStatsRow(
                    remaining = state.remaining,
                    percentageVsLastMonth = state.percentageVsLastMonth,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.recent_purchases),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text(
                            text = stringResource(R.string.see_all),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Secondary
                        )
                    }
                }
            }

            item {
                RecentPurchasesSection(
                    recentPurchases = state.recentPurchases,
                    onPurchaseClick = onNavigateToPurchaseDetail,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
