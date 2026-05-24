package com.undef.prowallet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.*
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.CATEGORIES
import com.undef.prowallet.viewmodel.PurchaseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPurchaseScreen(
    viewModel: PurchaseViewModel,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    
    val timeSlots = remember {
        (0 until 24).flatMap { hour ->
            listOf("00", "15", "30", "45").map { minute ->
                String.format(Locale.getDefault(), "%02d:%s", hour, minute)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val saveErrorMsg = stringResource(R.string.error_save_purchase)

    LaunchedEffect(state.savedSuccess) {
        if (state.savedSuccess) {
            onSaveSuccess()
            viewModel.clearSavedSuccess()
        }
    }

    LaunchedEffect(state.saveError) {
        if (state.saveError) {
            snackbarHostState.showSnackbar(saveErrorMsg)
            viewModel.clearSaveError()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
                        viewModel.onDateChange(sdf.format(Date(it)))
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.select_time)) },
            text = {
                Box(modifier = Modifier.height(300.dp)) {
                    LazyColumn {
                        items(timeSlots) { time ->
                            Text(
                                text = time,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onTimeChange(time)
                                        showTimePicker = false
                                    }
                                    .padding(16.dp),
                                fontFamily = PlusJakartaSans,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "new_purchase",
                onHomeClick = onNavigateToHome,
                onNewClick = {},
                onAnalyticsClick = onNavigateToAnalytics
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            TopBar(title = stringResource(R.string.new_purchase_title), onNavigateBack = onNavigateBack)

            Text(
                text = stringResource(R.string.new_purchase_subtitle),
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = Neutral,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Ticket capture area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        BorderStroke(1.5.dp, NeutralLight.copy(alpha = 0.5f)),
                        RoundedCornerShape(16.dp)
                    )
                    .background(Color(0xFFF8FAFB)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = NeutralLight,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.capture_ticket),
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = Neutral
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Purchase details
            SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
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
                        onValueChange = viewModel::onStoreNameChange,
                        placeholder = stringResource(R.string.store_name_placeholder),
                        leadingIcon = Icons.Default.Store,
                        label = stringResource(R.string.store_name_label)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { /* Launcher for camera/gallery */ },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.1f), contentColor = PrimaryDarker)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.attach_ticket_image))
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
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        )
                        CustomTextField(
                            value = state.time,
                            onValueChange = { },
                            placeholder = "00:00",
                            leadingIcon = Icons.Default.Schedule,
                            label = stringResource(R.string.time_label),
                            readOnly = true,
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    CustomTextField(
                        value = state.totalAmount,
                        onValueChange = viewModel::onTotalAmountChange,
                        placeholder = "0.00",
                        leadingIcon = Icons.Default.AttachMoney,
                        label = stringResource(R.string.total_amount_label)
                    )
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
                            onExpandedChange = { showCategoryMenu = it }
                        ) {
                            OutlinedTextField(
                                value = state.category,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = NeutralLight, modifier = Modifier.size(20.dp)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
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
                                onDismissRequest = { showCategoryMenu = false }
                            ) {
                                CATEGORIES.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontFamily = PlusJakartaSans) },
                                        onClick = {
                                            viewModel.onCategoryChange(cat)
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Products section
            SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.products_label),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    TextButton(onClick = {}) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.scan_item),
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = Secondary
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CustomTextField(
                            value = state.currentProductPrice,
                            onValueChange = viewModel::onProductPriceChange,
                            placeholder = "0.00",
                            leadingIcon = Icons.Default.AttachMoney,
                            label = stringResource(R.string.price_label),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    CustomTextField(
                        value = state.currentProductName,
                        onValueChange = viewModel::onProductNameChange,
                        placeholder = "Product Name",
                        leadingIcon = Icons.Default.ShoppingCart,
                        label = stringResource(R.string.product_name_label)
                    )

                    CustomTextField(
                        value = state.currentProductDescription,
                        onValueChange = viewModel::onProductDescriptionChange,
                        placeholder = "Description...",
                        leadingIcon = Icons.Default.Description,
                        label = "Description"
                    )

                    Button(
                        onClick = viewModel::addOrUpdateProduct,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(
                            if (state.editingProductId != null) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.editingProductId != null) "Update Product" else "Add Product")
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
                            IconButton(onClick = { viewModel.editProduct(product) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Secondary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { viewModel.removeProduct(product.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(20.dp))
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF8F8F8))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::savePurchase,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryDarker,
                    contentColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = PrimaryDarker,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.save_purchase),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
