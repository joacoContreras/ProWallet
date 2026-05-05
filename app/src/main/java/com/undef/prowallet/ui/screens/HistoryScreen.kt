package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.PurchaseCard
import com.undef.prowallet.ui.components.SectionCard
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.HomeViewModel

@Composable
fun HistoryScreen(
    homeViewModel: HomeViewModel,
    onNavigateToPurchaseDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by homeViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        TopBar(title = stringResource(R.string.history_tab), onNavigateBack = onNavigateBack)

        Text(
            text = stringResource(R.string.transactions_count, state.allPurchases.size),
            fontFamily = PlusJakartaSans,
            fontSize = 13.sp,
            color = Neutral,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard {
                    state.allPurchases.forEachIndexed { idx, purchase ->
                        PurchaseCard(
                            purchase = purchase,
                            onClick = { onNavigateToPurchaseDetail(purchase.id) }
                        )
                        if (idx < state.allPurchases.lastIndex) {
                            Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                        }
                    }
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.total_spent),
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        color = Neutral
                    )
                    Text(
                        text = "$${String.format("%.2f", state.allPurchases.sumOf { it.totalAmount })}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Secondary
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
