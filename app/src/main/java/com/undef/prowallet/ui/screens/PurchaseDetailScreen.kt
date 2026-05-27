package com.undef.prowallet.ui.screens

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.ProductItem
import com.undef.prowallet.ui.components.SectionCard
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.PurchaseDetailViewModel
import java.util.Locale

@Composable
fun PurchaseDetailScreen(
    purchaseId: String,
    viewModel: PurchaseDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(purchaseId) {
        viewModel.loadPurchase(purchaseId)
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val p = state.purchase ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.purchase_not_found))
        }
        return
    }

    val categoryIcon = when (p.category) {
        "Groceries" -> Icons.Default.ShoppingCart
        "Transport" -> Icons.Default.DirectionsCar
        "Dining" -> Icons.Default.Restaurant
        "Coffee" -> Icons.Default.LocalCafe
        else -> Icons.Default.Receipt
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_confirm_message), fontFamily = PlusJakartaSans) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deletePurchase(purchaseId, onDone = onNavigateBack)
                }) {
                    Text(stringResource(R.string.delete_confirm_button), color = ErrorRed, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel_label), fontFamily = PlusJakartaSans)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        val shareSubject = stringResource(R.string.share_purchase_subject, p.storeName)
        val productLines = p.products.joinToString("\n") { "• ${it.name} — $${String.format("%.2f", it.price)}" }
        val shareBody = stringResource(
            R.string.share_purchase_body,
            p.storeName,
            p.date,
            p.time,
            String.format("%.2f", p.totalAmount),
            productLines
        )

        TopBar(
            title = stringResource(R.string.purchase_detail_title),
            onNavigateBack = onNavigateBack,
            actions = {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                        putExtra(Intent.EXTRA_TEXT, shareBody)
                    }
                    context.startActivity(Intent.createChooser(intent, shareSubject))
                }) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_label), tint = PrimaryDarker)
                }
                IconButton(onClick = { onNavigateToEdit(purchaseId) }) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_label), tint = PrimaryDarker)
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_label), tint = ErrorRed)
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Store header card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Secondary)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                categoryIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = p.storeName,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Text(
                            text = p.category,
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "$${String.format("%.2f", p.totalAmount)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.date_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = Neutral
                            )
                            Text(
                                text = p.date,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.products_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = Neutral
                            )
                            Text(
                                text = stringResource(R.string.items_count_format, p.products.size),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            if (p.products.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.products_label),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                }

                item {
                    SectionCard {
                        p.products.forEachIndexed { idx, product ->
                            ProductItem(product = product)
                            val apiPrice = state.apiPriceMap[product.name.trim().lowercase(Locale.ROOT)]
                            if (apiPrice != null && apiPrice > 0.0) {
                                PriceComparisonBadge(paidPrice = product.price, apiPrice = apiPrice)
                            }
                            if (idx < p.products.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFE8ECEF), thickness = 1.dp)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.total_label),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "$${String.format("%.2f", p.totalAmount)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Secondary
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PriceComparisonBadge(paidPrice: Double, apiPrice: Double) {
    if (apiPrice <= 0.0) return
    val ratio = paidPrice / apiPrice
    val (icon, label, color) = when {
        ratio < 0.90 -> Triple(Icons.Default.CheckCircle, stringResource(R.string.price_good), Color(0xFF2E7D32))
        ratio > 1.10 -> Triple(Icons.Default.Warning, stringResource(R.string.price_high), MaterialTheme.colorScheme.error)
        else         -> Triple(Icons.Default.Info, stringResource(R.string.price_fair), TextSecondary)
    }
    Row(
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        Text(
            stringResource(R.string.price_api_reference, apiPrice),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
