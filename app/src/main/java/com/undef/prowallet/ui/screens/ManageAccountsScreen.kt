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
import com.undef.prowallet.R
import com.undef.prowallet.data.AccountEntity
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.AccountViewModel

@Composable
fun ManageAccountsScreen(
    viewModel: AccountViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }

    if (showAddDialog || editingAccount != null) {
        AccountDialog(
            initial = editingAccount,
            onDismiss = { showAddDialog = false; editingAccount = null },
            onSave = { name, type, lastFour, isPrimary ->
                val editing = editingAccount
                if (editing != null) {
                    viewModel.updateAccount(editing.id, name, type, lastFour, isPrimary)
                } else {
                    viewModel.addAccount(name, type, lastFour, isPrimary)
                }
                showAddDialog = false
                editingAccount = null
            }
        )
    }

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
                    onClick = { showAddDialog = true },
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

            if (state.accounts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = NeutralLight, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.no_accounts_yet), fontFamily = PlusJakartaSans, color = Neutral, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(state.accounts) { account ->
                    AccountCard(
                        account = account,
                        onEdit = { editingAccount = account },
                        onDelete = { viewModel.deleteAccount(account.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun AccountCard(
    account: AccountEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (account.type) {
                                "Savings" -> Icons.Default.Wallet
                                "Credit" -> Icons.Default.CreditCard
                                "Cash" -> Icons.Default.Payments
                                else -> Icons.Default.AccountBalance
                            },
                            contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(text = account.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text(
                            text = if (account.lastFour == "N/A") "${account.type}" else "${account.type} • •••• ${account.lastFour}",
                            fontSize = 12.sp,
                            color = Neutral
                        )
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
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralLight.copy(alpha = 0.3f))
                ) {
                    Text(text = stringResource(R.string.edit_label), color = PrimaryDarker, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onDelete,
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

@Composable
private fun AccountDialog(
    initial: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "Checking") }
    var lastFour by remember { mutableStateOf(initial?.lastFour ?: "") }
    var isPrimary by remember { mutableStateOf(initial?.isPrimary ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial != null) stringResource(R.string.edit_account_title) else stringResource(R.string.add_new_account),
                fontFamily = PlusJakartaSans
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.account_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val types = listOf("Checking", "Savings", "Credit", "Cash")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = lastFour,
                    onValueChange = { if (it.length <= 4) lastFour = it },
                    label = { Text(stringResource(R.string.last_four_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.set_as_primary), fontSize = 14.sp, color = TextPrimary)
                    Switch(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryDarker, checkedTrackColor = Primary)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onSave(name.trim(), type, lastFour.ifBlank { "N/A" }, isPrimary)
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
