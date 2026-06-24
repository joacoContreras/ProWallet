package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.undef.prowallet.R
import com.undef.prowallet.data.CategoryEntity
import com.undef.prowallet.domain.Product
import com.undef.prowallet.ui.components.CustomTextField
import com.undef.prowallet.ui.components.SectionCard
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.OcrStatus
import com.undef.prowallet.viewmodel.PurchaseUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDetailsSection(
    state: PurchaseUiState,
    showCategoryMenu: Boolean,
    onCategoryMenuExpandChange: (Boolean) -> Unit,
    onStoreNameChange: (String) -> Unit,
    onAttachTicketClick: () -> Unit,
    onRemoveTicketClick: () -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onCategorySelect: (String) -> Unit,
    onEditCategoryClick: (CategoryEntity) -> Unit,
    onDeleteCategoryClick: (Int) -> Unit,
    onAddCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    SectionCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.purchase_details_section),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = Neutral,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CustomTextField(
                value = state.storeName,
                onValueChange = onStoreNameChange,
                placeholder = stringResource(R.string.store_name_placeholder),
                leadingIcon = Icons.Default.Store,
                label = stringResource(R.string.store_name_label)
            )
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                if (state.ticketImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F0F0))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(state.ticketImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.attach_ticket_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = onRemoveTicketClick,
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = stringResource(R.string.delete_label), tint = ErrorRed)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = onAttachTicketClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.1f), contentColor = PrimaryDarker)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.ticketImageUri != null) stringResource(R.string.change_ticket_image) else stringResource(R.string.attach_ticket_image))
                }
                if (state.ocrStatus == OcrStatus.Processing) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryDarker)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.ocr_processing),
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = Neutral
                        )
                    }
                }
                if (state.ocrStatus == OcrStatus.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.ocr_error_no_text),
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = ErrorRed
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomTextField(
                    value = state.date,
                    onValueChange = { },
                    placeholder = stringResource(R.string.date_placeholder),
                    leadingIcon = Icons.Default.CalendarToday,
                    label = stringResource(R.string.date_label),
                    readOnly = true,
                    onClick = onDateClick,
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    value = state.time,
                    onValueChange = { },
                    placeholder = "00:00",
                    leadingIcon = Icons.Default.Schedule,
                    label = stringResource(R.string.time_label),
                    readOnly = true,
                    onClick = onTimeClick,
                    modifier = Modifier.weight(1f)
                )
            }
            // Location status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (state.latitude != null) Primary else NeutralLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = when {
                        state.isFetchingLocation -> stringResource(R.string.fetching_location)
                        state.latitude != null -> String.format(
                            Locale.getDefault(),
                            "%.4f, %.4f",
                            state.latitude,
                            state.longitude
                        )
                        else -> stringResource(R.string.location_unavailable)
                    },
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = if (state.latitude != null) Primary else NeutralLight
                )
            }

            // Category selector
            Column {
                Text(
                    text = stringResource(R.string.category_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = onCategoryMenuExpandChange
                ) {
                    OutlinedTextField(
                        value = categoryDisplayName(state.category),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = NeutralLight, modifier = Modifier.size(20.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE8ECEF),
                            unfocusedBorderColor = Color(0xFFE8ECEF),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFB),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { onCategoryMenuExpandChange(false) }
                    ) {
                        state.categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            categoryDisplayName(cat.name),
                                            fontFamily = PlusJakartaSans,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row {
                                            IconButton(
                                                onClick = { onEditCategoryClick(cat) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_label), tint = Secondary, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { onDeleteCategoryClick(cat.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_label), tint = ErrorRed, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    onCategorySelect(cat.name)
                                    onCategoryMenuExpandChange(false)
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Secondary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.category_add), fontFamily = PlusJakartaSans, color = Secondary)
                                }
                            },
                            onClick = onAddCategoryClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductsSection(
    state: PurchaseUiState,
    onProductPriceChange: (String) -> Unit,
    onProductNameChange: (String) -> Unit,
    onProductDescriptionChange: (String) -> Unit,
    onAddOrUpdateProductClick: () -> Unit,
    onEditProductClick: (Product) -> Unit,
    onRemoveProductClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.products_label),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = state.currentProductPrice,
                    onValueChange = onProductPriceChange,
                    placeholder = "0.00",
                    leadingIcon = Icons.Default.AttachMoney,
                    label = stringResource(R.string.price_label),
                    modifier = Modifier.weight(1f)
                )
            }

            CustomTextField(
                value = state.currentProductName,
                onValueChange = onProductNameChange,
                placeholder = stringResource(R.string.product_name_placeholder),
                leadingIcon = Icons.Default.ShoppingCart,
                label = stringResource(R.string.product_name_label)
            )

            CustomTextField(
                value = state.currentProductDescription,
                onValueChange = onProductDescriptionChange,
                placeholder = stringResource(R.string.description_placeholder),
                leadingIcon = Icons.Default.Description,
                label = stringResource(R.string.description_label)
            )

            Button(
                onClick = onAddOrUpdateProductClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(
                    if (state.editingProductId != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.editingProductId != null)
                        stringResource(R.string.update_product_button)
                    else
                        stringResource(R.string.add_product_button)
                )
            }

            if (state.productError) {
                Text(
                    text = stringResource(R.string.error_product_name_and_price),
                    color = ErrorRed,
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (state.products.isEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.no_products_yet),
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = NeutralLight,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            state.products.forEach { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            product.name,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "${String.format(Locale.getDefault(), "$%.2f", product.price)}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = Neutral
                        )
                    }
                    IconButton(onClick = { onEditProductClick(product) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_label), tint = Secondary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { onRemoveProductClick(product.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_label), tint = ErrorRed, modifier = Modifier.size(20.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFF8F8F8))
            }
        }
    }
}

@Composable
fun categoryDisplayName(key: String): String = when (key) {
    "Groceries" -> stringResource(R.string.category_groceries)
    "Transport" -> stringResource(R.string.category_transport)
    "Dining"    -> stringResource(R.string.category_dining)
    "Coffee"    -> stringResource(R.string.category_coffee)
    "Other"     -> stringResource(R.string.category_other)
    else        -> key
}
