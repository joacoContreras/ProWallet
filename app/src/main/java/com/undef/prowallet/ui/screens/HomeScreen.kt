package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
            onNavigateToNotifications = {}
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
    onNavigateToNotifications: () -> Unit
) {
    val state by homeViewModel.uiState.collectAsState()

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
                // Header (Redesigned as per mockup)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF1F7F6), // Match background color from mockup header
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NeutralLight)
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // App Name
                        Text(
                            text = stringResource(R.string.app_name),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = PrimaryDarker
                        )

                        // Notification Icon
                        IconButton(
                            onClick = onNavigateToNotifications,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = PrimaryDarker
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                // Main spend card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.total_monthly_spend).uppercase(),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = SecondaryDark.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", state.totalMonthlySpend)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = SecondaryDark
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.healthy_pace),
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = SecondaryDark.copy(alpha = 0.7f)
                            )
                            Text(
                                text = stringResource(R.string.of_budget, "${state.budgetPercent}%"),
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryDark
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { state.budgetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimaryDarker,
                            trackColor = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Remaining card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryLight.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = SecondaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$${String.format(Locale.getDefault(), "%.2f", state.remaining)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = SecondaryDark
                            )
                            Text(
                                text = stringResource(R.string.remaining),
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = SecondaryDark.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // % vs last month card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = TertiaryDark.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = SecondaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "${state.percentageVsLastMonth}%",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = SecondaryDark
                            )
                            Text(
                                text = stringResource(R.string.less_than_last_mo),
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = SecondaryDark.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
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
                if (state.recentPurchases.isEmpty()) {
                    SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Neutral.copy(alpha = 0.4f)
                            )
                            Text(
                                text = stringResource(R.string.no_recent_purchases),
                                fontFamily = PlusJakartaSans,
                                fontSize = 14.sp,
                                color = Neutral
                            )
                        }
                    }
                } else {
                    SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                        state.recentPurchases.forEachIndexed { idx, purchase ->
                            PurchaseCard(
                                purchase = purchase,
                                onClick = { onNavigateToPurchaseDetail(purchase.id) }
                            )
                            if (idx < state.recentPurchases.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
