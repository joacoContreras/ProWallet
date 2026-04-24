package com.undef.ProWallet.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.ProWallet.domain.model.Transaction
import com.undef.ProWallet.ui.components.ProWalletCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val mockTransactions = listOf(
        Transaction("1", "Supermercado", "Comida", 120.50, "Hoy", true),
        Transaction("2", "Salario", "Ingresos", 2500.00, "Ayer", false),
        Transaction("3", "Netflix", "Entretenimiento", 15.99, "22 Abr", true),
        Transaction("4", "Gimnasio", "Salud", 50.00, "20 Abr", true)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Billetera", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* MOCK */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BalanceCard(balance = 2380.50)
            }
            
            item {
                Text(
                    "Actividad reciente",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(mockTransactions) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double) {
    ProWalletCard(modifier = Modifier.fillMaxWidth()) {
        Text("Saldo Total", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "$${String.format("%.2f", balance)}",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (transaction.isExpense) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (transaction.isExpense) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (transaction.isExpense) Color.Red else Color.Green,
                modifier = Modifier.padding(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1.0f)) {
            Text(transaction.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(transaction.date, style = MaterialTheme.typography.bodySmall)
        }
        
        Text(
            "${if (transaction.isExpense) "-" else "+"}$${transaction.amount}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (transaction.isExpense) Color.Black else Color(0xFF2E7D32)
        )
    }
}
