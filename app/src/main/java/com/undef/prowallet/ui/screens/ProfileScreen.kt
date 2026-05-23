package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.tooling.preview.Preview
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.AuthViewModel

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProWalletTheme {
        ProfileScreen(
            authViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            onNavigateBack = {},
            onNavigateToSettings = {},
            onNavigateToManageAccounts = {},
            onNavigateToMonthlySetup = {},
            onNavigateToAutoSavings = {},
            onNavigateToContactSupport = {},
            onLogout = {}
        )
    }
}

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToManageAccounts: () -> Unit,
    onNavigateToMonthlySetup: () -> Unit,
    onNavigateToAutoSavings: () -> Unit,
    onNavigateToContactSupport: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(
            title = stringResource(R.string.app_name),
            onNavigateBack = onNavigateBack,
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryDarker)
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // User Identity Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .shadow(20.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = PrimaryDarker.copy(alpha = 0.2f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Alex Rivera",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimary
                )
                Text(
                    text = stringResource(R.string.wealth_management_plan),
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    color = Neutral
                )
            }

            // Financial Profile Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.financial_profile),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Surface(
                        color = Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text(
                            text = stringResource(R.string.active_label),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = PrimaryDarker,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            FinancialStatCard(
                                icon = Icons.Default.Payments,
                                label = stringResource(R.string.monthly_income),
                                value = "$8,450",
                                modifier = Modifier.weight(1f)
                            )
                            FinancialStatCard(
                                icon = Icons.Default.PieChart,
                                label = stringResource(R.string.budget_usage),
                                value = "64%",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.spending_limit), fontSize = 12.sp, color = Neutral)
                                Text("$5,408 / $8,450", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            LinearProgressIndicator(
                                progress = { 0.64f },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                                color = PrimaryDarker,
                                trackColor = BackgroundLight
                            )
                        }
                    }
                }
            }

            // Management Section
            ManagementSection(
                title = stringResource(R.string.management_section),
                items = listOf(
                    ManagementItem(Icons.Default.AccountBalance, stringResource(R.string.linked_accounts), stringResource(R.string.banks_connected), SecondaryLight.copy(alpha = 0.2f), SecondaryDark, onNavigateToManageAccounts),
                    ManagementItem(Icons.Default.EventRepeat, stringResource(R.string.fixed_expenses), stringResource(R.string.rent_utilities_subscriptions), TertiaryDark.copy(alpha = 0.5f), SecondaryDark, onNavigateToMonthlySetup),
                    ManagementItem(Icons.Default.Savings, stringResource(R.string.auto_savings_plan), stringResource(R.string.auto_savings_amount), Primary.copy(alpha = 0.2f), PrimaryDarker, onNavigateToAutoSavings)
                )
            )

            // Support Section
            ManagementSection(
                title = stringResource(R.string.support_safety),
                items = listOf(
                    ManagementItem(Icons.Default.SupportAgent, stringResource(R.string.contact_us), "", BackgroundLight, Neutral, onNavigateToContactSupport),
                    ManagementItem(Icons.Default.Security, stringResource(R.string.security_privacy), stringResource(R.string.security_features), BackgroundLight, Neutral, { })
                )
            )

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.logout_button),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun FinancialStatCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = BackgroundLight,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = Secondary, modifier = Modifier.size(24.dp))
            Text(text = label, fontSize = 11.sp, color = Neutral)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

data class ManagementItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val iconBg: Color,
    val iconTint: Color,
    val onClick: () -> Unit
)

@Composable
fun ManagementSection(title: String, items: List<ManagementItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title,
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
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { item.onClick() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(item.iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = item.iconTint, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            if (item.subtitle.isNotBlank()) {
                                Text(text = item.subtitle, fontSize = 12.sp, color = Neutral)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Neutral, modifier = Modifier.size(20.dp))
                    }
                    if (index < items.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF8F8F8), thickness = 1.dp)
                    }
                }
            }
        }
    }
}
