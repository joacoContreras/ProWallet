package com.undef.prowallet.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.PriceComparisonViewModel
import com.undef.prowallet.viewmodel.ProductComparisonSummary
import com.undef.prowallet.viewmodel.StorePriceComparison
import java.util.Locale

@Composable
fun PriceComparisonScreen(
    viewModel: PriceComparisonViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Handle system back button to go back to the product list if a product is selected
    BackHandler(enabled = state.selectedProduct != null) {
        viewModel.selectProduct(null)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = if (state.selectedProduct != null) "Comparativa de Producto" else "Comparador de Precios",
                onNavigateBack = {
                    if (state.selectedProduct != null) {
                        viewModel.selectProduct(null)
                    } else {
                        onNavigateBack()
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.selectedProduct == null) {
                ProductListContent(
                    state = state,
                    onSearchChange = { viewModel.searchProducts(it) },
                    onProductSelect = { viewModel.selectProduct(it) }
                )
            } else {
                ProductComparisonDetailContent(
                    selectedProduct = state.selectedProduct!!,
                    comparisons = state.comparisons
                )
            }
        }
    }
}

@Composable
fun ProductListContent(
    state: com.undef.prowallet.viewmodel.PriceComparisonUiState,
    onSearchChange: (String) -> Unit,
    onProductSelect: (ProductComparisonSummary) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(2.dp, RoundedCornerShape(14.dp)),
            placeholder = { Text("Buscar producto para comparar...", fontFamily = PlusJakartaSans, color = NeutralLight) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Neutral) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda", tint = Neutral)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryDark,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Secondary)
            }
        } else if (state.uniqueProducts.isEmpty()) {
            EmptyStateView(
                title = "Sin historial de productos",
                description = "Registra compras con productos detallados en diferentes supermercados para compararlos aquí."
            )
        } else if (state.filteredProducts.isEmpty()) {
            EmptyStateView(
                title = "No se encontraron resultados",
                description = "Prueba buscando con otro nombre de producto."
            )
        } else {
            Text(
                text = "Productos en tu historial",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(state.filteredProducts) { summary ->
                    ProductSummaryCard(
                        summary = summary,
                        onClick = { onProductSelect(summary) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductSummaryCard(
    summary: ProductComparisonSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = PrimaryDarker,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = summary.name,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = Neutral,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (summary.storeCount == 1) {
                            "Disponible en 1 supermercado"
                        } else {
                            "Disponible en ${summary.storeCount} supermercados"
                        },
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = Neutral
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", summary.minPrice)}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                if (summary.storeCount > 1) {
                    Text(
                        text = "rango",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = NeutralLight
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = NeutralLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProductComparisonDetailContent(
    selectedProduct: ProductComparisonSummary,
    comparisons: List<StorePriceComparison>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Main Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = selectedProduct.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Lowest Price Box
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SuccessGreen.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "MÁS BARATO",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = SuccessGreen,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format(Locale.getDefault(), "%.2f", selectedProduct.minPrice)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                        }

                        // Highest Price Box
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ErrorRed.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "MÁS CARO",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = ErrorRed,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format(Locale.getDefault(), "%.2f", selectedProduct.maxPrice)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    if (selectedProduct.storeCount > 1) {
                        val potentialSavings = selectedProduct.maxPrice - selectedProduct.minPrice
                        val savingsPercent = (potentialSavings / selectedProduct.minPrice) * 100

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Primary.copy(alpha = 0.15f))
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = PrimaryDarker,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Ahorro Potencial Máximo",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = PrimaryDarker
                                    )
                                    Text(
                                        text = "Puedes ahorrar hasta $${String.format(Locale.getDefault(), "%.2f", potentialSavings)} (${String.format(Locale.getDefault(), "%.1f", savingsPercent)}%) si compras en el lugar más económico.",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Comparativa por Supermercado",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (comparisons.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay datos de precios disponibles", fontFamily = PlusJakartaSans, color = Neutral)
                }
            }
        } else {
            // Find max price to calculate bar ratios
            val maxPrice = comparisons.maxOfOrNull { it.price } ?: 1.0

            items(comparisons) { comp ->
                StorePriceComparisonCard(
                    comp = comp,
                    maxPrice = maxPrice
                )
            }
        }
    }
}

@Composable
fun StorePriceComparisonCard(
    comp: StorePriceComparison,
    maxPrice: Double
) {
    val barRatio = if (maxPrice > 0) (comp.price / maxPrice).toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Initial avatar
                    val initial = comp.storeName.firstOrNull()?.toString()?.uppercase() ?: "S"
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    comp.isCheapest -> SuccessGreen.copy(alpha = 0.2f)
                                    comp.isMostExpensive -> ErrorRed.copy(alpha = 0.15f)
                                    else -> Secondary.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when {
                                comp.isCheapest -> SuccessGreen
                                comp.isMostExpensive -> ErrorRed
                                else -> SecondaryDark
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = comp.storeName,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${comp.productName} • ${comp.date}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.2f", comp.price)}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    comp.isCheapest -> SuccessGreen.copy(alpha = 0.15f)
                                    comp.isMostExpensive -> ErrorRed.copy(alpha = 0.15f)
                                    else -> NeutralLight.copy(alpha = 0.25f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when {
                                comp.isCheapest -> "El más barato"
                                comp.isMostExpensive -> "El más caro"
                                else -> "+${String.format(Locale.getDefault(), "%.1f", comp.differencePercentage)}%"
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp,
                            color = when {
                                comp.isCheapest -> SuccessGreen
                                comp.isMostExpensive -> ErrorRed
                                else -> TextSecondary
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual price indicator bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barRatio)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                comp.isCheapest -> SuccessGreen
                                comp.isMostExpensive -> ErrorRed
                                else -> SecondaryLight
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = NeutralLight,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = Neutral,
                modifier = Modifier.fillMaxWidth(0.85f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
