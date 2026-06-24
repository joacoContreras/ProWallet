package com.undef.prowallet.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.PurchaseCard
import com.undef.prowallet.ui.components.SectionCard
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToPurchaseDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showFilterDialog by remember { mutableStateOf(false) }

    if (showFilterDialog) {
        HistoryFilterDialog(
            categories = state.availableCategories,
            current = state.filter,
            onApply = { storeQuery, category, fromMs, toMs, min, max ->
                viewModel.setStoreQuery(storeQuery)
                viewModel.setCategory(category)
                viewModel.setDateRange(fromMs, toMs)
                viewModel.setAmountRange(min, max)
                showFilterDialog = false
            },
            onClear = {
                viewModel.clearFilters()
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        val exportSubject = stringResource(R.string.export_history_subject)
        TopBar(
            title = stringResource(R.string.history_tab),
            onNavigateBack = onNavigateBack,
            actions = {
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filter_label), tint = PrimaryDarker)
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, exportSubject)
                        putExtra(Intent.EXTRA_TEXT, viewModel.buildExportText())
                    }
                    context.startActivity(Intent.createChooser(intent, exportSubject))
                }) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_history_label), tint = PrimaryDarker)
                }
            }
        )

        Text(
            text = stringResource(R.string.transactions_count, state.filteredPurchases.size),
            fontFamily = PlusJakartaSans,
            fontSize = 13.sp,
            color = Neutral,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        if (state.filter.activeCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filter_active_summary, state.filter.activeCount),
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Secondary,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { viewModel.clearFilters() }) {
                    Text(stringResource(R.string.filter_clear), fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (state.filteredPurchases.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Neutral.copy(alpha = 0.4f)
                    )
                    Text(
                        text = stringResource(R.string.no_purchases_title),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (state.allPurchases.isEmpty())
                            stringResource(R.string.no_purchases_subtitle)
                        else
                            stringResource(R.string.no_purchases_match_filter),
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        color = Neutral,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SectionCard {
                        state.filteredPurchases.forEachIndexed { idx, purchase ->
                            PurchaseCard(
                                purchase = purchase,
                                onClick = { onNavigateToPurchaseDetail(purchase.id) }
                            )
                            if (idx < state.filteredPurchases.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
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
                            text = "$${String.format(Locale.getDefault(), "%.2f", state.filteredPurchases.sumOf { it.totalAmount })}",
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryFilterDialog(
    categories: List<String>,
    current: com.undef.prowallet.viewmodel.HistoryFilter,
    onApply: (storeQuery: String, category: String?, fromMs: Long?, toMs: Long?, min: Double?, max: Double?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var storeQuery by remember { mutableStateOf(current.storeQuery) }
    var category by remember { mutableStateOf(current.category) }
    var minAmount by remember { mutableStateOf(current.minAmount?.toString() ?: "") }
    var maxAmount by remember { mutableStateOf(current.maxAmount?.toString() ?: "") }
    var fromMs by remember { mutableStateOf(current.dateFromMs) }
    var toMs by remember { mutableStateOf(current.dateToMs) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }

    if (showFromPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = fromMs)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = { fromMs = state.selectedDateMillis; showFromPicker = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = state) }
    }
    if (showToPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = toMs)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = { toMs = state.selectedDateMillis; showToPicker = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = state) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_title), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = storeQuery,
                    onValueChange = { storeQuery = it },
                    placeholder = { Text(stringResource(R.string.filter_store_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(expanded = showCategoryMenu, onExpandedChange = { showCategoryMenu = it }) {
                    OutlinedTextField(
                        value = category ?: stringResource(R.string.filter_category_all),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.filter_category_all)) },
                            onClick = { category = null; showCategoryMenu = false }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { category = cat; showCategoryMenu = false }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fromMs?.let { sdf.format(Date(it)) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.filter_date_from)) },
                        modifier = Modifier.weight(1f).clickable { showFromPicker = true }
                    )
                    OutlinedTextField(
                        value = toMs?.let { sdf.format(Date(it)) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.filter_date_to)) },
                        modifier = Modifier.weight(1f).clickable { showToPicker = true }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minAmount,
                        onValueChange = { minAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(stringResource(R.string.filter_min_amount)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxAmount,
                        onValueChange = { maxAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(stringResource(R.string.filter_max_amount)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onApply(storeQuery, category, fromMs, toMs, minAmount.toDoubleOrNull(), maxAmount.toDoubleOrNull())
            }) { Text(stringResource(R.string.filter_apply)) }
        },
        dismissButton = {
            TextButton(onClick = onClear) { Text(stringResource(R.string.filter_clear)) }
        }
    )
}
