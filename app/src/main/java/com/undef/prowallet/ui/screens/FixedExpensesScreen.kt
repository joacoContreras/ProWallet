package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.data.FixedExpenseEntity
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.FixedExpensesViewModel
import java.util.Locale

@Composable
fun FixedExpensesScreen(
    viewModel: FixedExpensesViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<FixedExpenseEntity?>(null) }

    if (showAddDialog || editingExpense != null) {
        FixedExpenseDialog(
            initial = editingExpense,
            onDismiss = { showAddDialog = false; editingExpense = null },
            onSave = { name, amount, category, frequency ->
                val editing = editingExpense
                if (editing != null) {
                    viewModel.updateExpense(editing.id, name, amount, category, frequency)
                } else {
                    viewModel.addExpense(name, amount, category, frequency)
                }
                showAddDialog = false
                editingExpense = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.fixed_expenses),
                onNavigateBack = onNavigateBack
            )
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
                    modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarker)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.add_fixed_expense), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.total_monthly_fixed), fontFamily = PlusJakartaSans, fontSize = 12.sp, color = Neutral)
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", state.totalMonthly)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = PrimaryDarker
                        )
                    }
                }
            }

            if (state.expenses.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventRepeat, contentDescription = null, tint = NeutralLight, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.no_fixed_expenses), fontFamily = PlusJakartaSans, color = Neutral, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(state.expenses) { expense ->
                    FixedExpenseCard(
                        expense = expense,
                        onEdit = { editingExpense = expense },
                        onDelete = { viewModel.deleteExpense(expense.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun FixedExpenseCard(
    expense: FixedExpenseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EventRepeat, contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text("${expense.category} · ${expense.frequency}", fontSize = 12.sp, color = Neutral)
            }
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", expense.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PrimaryDarker
            )
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_label), tint = Secondary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_label), tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FixedExpenseDialog(
    initial: FixedExpenseEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amount by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "Other") }
    var frequency by remember { mutableStateOf(initial?.frequency ?: "monthly") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial != null) stringResource(R.string.edit_fixed_expense) else stringResource(R.string.add_fixed_expense),
                fontFamily = PlusJakartaSans
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.expense_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.amount_label)) },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.category_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val frequencies = listOf("monthly", "yearly", "weekly")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    frequencies.forEach { freq ->
                        FilterChip(
                            selected = frequency == freq,
                            onClick = { frequency = freq },
                            label = { Text(freq, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && parsedAmount > 0) {
                    onSave(name.trim(), parsedAmount, category.trim().ifBlank { "Other" }, frequency)
                }
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
