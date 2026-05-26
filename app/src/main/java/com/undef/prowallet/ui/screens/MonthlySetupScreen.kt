package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.MonthlySetupViewModel

@Composable
fun MonthlySetupScreen(
    viewModel: MonthlySetupViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val monthlyIncome = state.monthlyIncome

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.clearSaved()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = stringResource(R.string.monthly_setup_title), onNavigateBack = onNavigateBack)
        },
        containerColor = BackgroundLight,
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarker)
                ) {
                    Text(text = stringResource(R.string.finalize_budget), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text(
                    text = stringResource(R.string.settings_adjust_anytime),
                    fontSize = 11.sp,
                    color = Neutral,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                // Hero Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(PrimaryDark, PrimaryLight))))
                    Column(modifier = Modifier.padding(24.dp).align(Alignment.BottomStart)) {
                        Text(text = stringResource(R.string.step_1_of_2), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(text = stringResource(R.string.define_your_success), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Icon(
                        Icons.Default.Wallet,
                        contentDescription = null,
                        modifier = Modifier.size(140.dp).align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp),
                        tint = Color.White.copy(alpha = 0.1f)
                    )
                }
            }

            item {
                // Income Section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.monthly_income), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Neutral)
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(text = stringResource(R.string.monthly_income_hint), fontSize = 14.sp, color = Neutral)
                            OutlinedTextField(
                                value = monthlyIncome,
                                onValueChange = { viewModel.onIncomeChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("$", color = PrimaryDarker, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = BackgroundLight,
                                    unfocusedContainerColor = BackgroundLight,
                                    focusedBorderColor = Primary
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SuggestionChip(onClick = {}, label = { Text(stringResource(R.string.primary_job)) }, shape = RoundedCornerShape(percent = 50))
                                SuggestionChip(onClick = {}, label = { Text(stringResource(R.string.add_source)) }, shape = RoundedCornerShape(percent = 50))
                            }
                        }
                    }
                }
            }

            item {
                // Budget Allocation Section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.budget_allocation), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Surface(color = Secondary.copy(alpha = 0.1f), shape = RoundedCornerShape(percent = 50)) {
                            Text(text = "85% Assigned", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Secondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        BudgetItem(Icons.Default.ShoppingBasket, stringResource(R.string.groceries_label), "$800", "Recommended: $600", 0.75f)
                        BudgetItem(Icons.Default.Commute, stringResource(R.string.transport_label), "$320", "Public & Fuel", 0.45f)
                        BudgetItem(Icons.Default.Restaurant, stringResource(R.string.dining_out_label), "$450", "Entertainment", 0.60f)
                        
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeutralLight.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = Neutral)
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(R.string.add_custom_category), color = Neutral, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(140.dp)) }
        }
    }
}

@Composable
fun BudgetItem(icon: ImageVector, title: String, amount: String, subtitle: String, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Secondary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text(text = subtitle, fontSize = 11.sp, color = Neutral)
                    }
                }
                Text(text = amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = PrimaryDarker,
                trackColor = TertiaryDark.copy(alpha = 0.3f)
            )
        }
    }
}
