package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*

data class BankAccount(
    val id: String,
    val name: String,
    val type: String,
    val lastFour: String,
    val icon: ImageVector,
    val iconBg: Color,
    val isPrimary: Boolean = false
)

@Composable
fun ManageAccountsScreen(onNavigateBack: () -> Unit) {
    val accounts = listOf(
        BankAccount("1", "Chase Premier Plus", "Checking", "4492", Icons.Default.AccountBalance, Primary.copy(alpha = 0.2f), true),
        BankAccount("2", "Main Savings", "Savings", "0118", Icons.Default.Wallet, Secondary.copy(alpha = 0.2f)),
        BankAccount("3", "Sapphire Preferred", "Credit", "8821", Icons.Default.CreditCard, TertiaryDark.copy(alpha = 0.5f)),
        BankAccount("4", "Petty Cash Wallet", "Cash", "N/A", Icons.Default.Payments, NeutralLight.copy(alpha = 0.2f))
    )

    Scaffold(
        topBar = {
            TopBar(title = stringResource(R.string.app_name), onNavigateBack = onNavigateBack)
        },
        containerColor = BackgroundLight,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarker)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.add_new_account), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PrimaryDarker)
                    }
                    Column {
                        Text(text = stringResource(R.string.manage_accounts_title), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                        Text(text = stringResource(R.string.manage_accounts_subtitle), fontFamily = PlusJakartaSans, fontSize = 13.sp, color = Neutral)
                    }
                }
            }

            items(accounts) { account ->
                AccountCard(account)
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun AccountCard(account: BankAccount) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(account.iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(account.icon, contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(text = account.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text(text = "${account.type} • •••• ${account.lastFour}", fontSize = 12.sp, color = Neutral)
                    }
                }
                if (account.isPrimary) {
                    Surface(
                        color = TertiaryDark.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text(
                            text = stringResource(R.string.primary_label),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarker
                        )
                    }
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralLight.copy(alpha = 0.3f))
                ) {
                    Text(text = stringResource(R.string.edit_label), color = PrimaryDarker, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralLight.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_label), tint = ErrorRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
