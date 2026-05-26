package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.undef.prowallet.R
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.HomeViewModel
import com.undef.prowallet.viewmodel.PurchaseViewModel
import java.util.Locale

@Composable
fun PurchaseSuccessScreen(
    viewModel: PurchaseViewModel,
    onNavigateToHome: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val totalAmount = state.products.sumOf { it.price }
    val homeViewModel: HomeViewModel = viewModel()
    val homeState by homeViewModel.uiState.collectAsState()
    val spent = homeState.totalMonthlySpend
    val budget = homeState.monthlyBudget
    val budgetPercent = homeState.budgetPercent
    val remaining = homeState.remaining

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.8f),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onViewReceipt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarker),
                        shape = RoundedCornerShape(27.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.view_full_receipt),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = onNavigateToHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Neutral)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.back_to_home),
                            fontFamily = PlusJakartaSans,
                            color = Neutral
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(Modifier.height(20.dp))
                // Header Section
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.2f))
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.purchase_successful),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = TextPrimary
                )
                Text(
                    text = stringResource(R.string.payment_processed),
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    color = Neutral,
                    textAlign = TextAlign.Center
                )
            }

            item {
                // Main Amount Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.total_amount_paid),
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Neutral,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", totalAmount)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            color = PrimaryDarker
                        )
                    }
                }
            }

            item {
                // Items Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.items_summary),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = PrimaryDarker)
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        state.products.forEachIndexed { index, product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    if (product.description.isNotBlank()) {
                                        Text(
                                            text = product.description,
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 12.sp,
                                            color = Neutral
                                        )
                                    }
                                }
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%.2f", product.price)}",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            }
                            if (index < state.products.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF8F8F8), thickness = 1.dp)
                            }
                        }
                        
                    }
                }
            }

            item {
                // Monthly Insight Bento Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Primary.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.QueryStats, contentDescription = null, tint = PrimaryDarker)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.monthly_spending_insight),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryDarker
                                )
                                Text(
                                    text = stringResource(R.string.updated_real_time),
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    color = PrimaryDarker.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.budget_used_format, budgetPercent),
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = Neutral
                            )
                            Text(
                                text = stringResource(
                                    R.string.budget_spent_format,
                                    String.format(Locale.getDefault(), "%.2f", spent),
                                    String.format(Locale.getDefault(), "%.2f", budget)
                                ),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PrimaryDarker
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (budgetPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(CircleShape),
                            color = PrimaryDarker,
                            trackColor = Color.White
                        )

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(
                                R.string.remaining_insight_format,
                                String.format(Locale.getDefault(), "%.2f", remaining)
                            ),
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = Neutral,
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }
                }
            }
            
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}
