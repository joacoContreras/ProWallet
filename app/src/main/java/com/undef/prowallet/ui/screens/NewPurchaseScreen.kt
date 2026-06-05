package com.undef.prowallet.ui.screens

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.undef.prowallet.R
import com.undef.prowallet.data.CategoryEntity
import com.undef.prowallet.ui.components.*
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.PurchaseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPurchaseScreen(
    viewModel: PurchaseViewModel,
    purchaseId: String? = null,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val isEditMode = purchaseId != null
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onTicketImageSelected(uri?.toString())
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.fetchLocation()
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryInputText by remember { mutableStateOf("") }

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
    val validationErrorMsg = stringResource(R.string.error_store_name_required)

    LaunchedEffect(Unit) {
        if (isEditMode) {
            viewModel.loadForEdit(purchaseId!!)
        } else {
            viewModel.resetForm()
            val fineGranted = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarseGranted = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (fineGranted || coarseGranted) {
                viewModel.fetchLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

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

    LaunchedEffect(state.validationError) {
        if (state.validationError) {
            snackbarHostState.showSnackbar(validationErrorMsg)
            viewModel.clearValidationError()
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text(stringResource(R.string.category_add), fontFamily = PlusJakartaSans) },
            text = {
                OutlinedTextField(
                    value = categoryInputText,
                    onValueChange = { categoryInputText = it },
                    label = { Text(stringResource(R.string.category_name_hint)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (categoryInputText.isNotBlank()) {
                        viewModel.addCategory(categoryInputText.trim())
                        categoryInputText = ""
                        showAddCategoryDialog = false
                    }
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showEditCategoryDialog) {
        val cat = editingCategory
        if (cat != null) {
            AlertDialog(
                onDismissRequest = { showEditCategoryDialog = false; editingCategory = null },
                title = { Text(stringResource(R.string.category_edit_title), fontFamily = PlusJakartaSans) },
                text = {
                    OutlinedTextField(
                        value = categoryInputText,
                        onValueChange = { categoryInputText = it },
                        label = { Text(stringResource(R.string.category_name_hint)) },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (categoryInputText.isNotBlank()) {
                            viewModel.updateCategoryName(cat.id, categoryInputText.trim())
                            showEditCategoryDialog = false
                            editingCategory = null
                        }
                    }) { Text(stringResource(R.string.ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showEditCategoryDialog = false; editingCategory = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
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
            TopBar(
                title = if (isEditMode) stringResource(R.string.edit_purchase_title) else stringResource(R.string.new_purchase_title),
                onNavigateBack = onNavigateBack
            )

            Text(
                text = stringResource(R.string.new_purchase_subtitle),
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = Neutral,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

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
                                    onClick = { viewModel.onTicketImageSelected(null) },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = stringResource(R.string.delete_label), tint = ErrorRed)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.1f), contentColor = PrimaryDarker)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.ticketImageUri != null) stringResource(R.string.change_ticket_image) else stringResource(R.string.attach_ticket_image))
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
                                state.isFetchingLocation -> "Fetching location…"
                                state.latitude != null -> String.format(
                                    Locale.getDefault(),
                                    "%.4f, %.4f",
                                    state.latitude,
                                    state.longitude
                                )
                                else -> "Location unavailable"
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
                            onExpandedChange = { showCategoryMenu = it }
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
                                onDismissRequest = { showCategoryMenu = false }
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
                                                        onClick = {
                                                            editingCategory = cat
                                                            categoryInputText = cat.name
                                                            showEditCategoryDialog = true
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_label), tint = Secondary, modifier = Modifier.size(16.dp))
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.deleteCategory(cat.id) },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_label), tint = ErrorRed, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.onCategoryChange(cat.name)
                                            showCategoryMenu = false
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
                                    onClick = {
                                        categoryInputText = ""
                                        showCategoryMenu = false
                                        showAddCategoryDialog = true
                                    }
                                )
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
                        placeholder = stringResource(R.string.product_name_placeholder),
                        leadingIcon = Icons.Default.ShoppingCart,
                        label = stringResource(R.string.product_name_label)
                    )

                    CustomTextField(
                        value = state.currentProductDescription,
                        onValueChange = viewModel::onProductDescriptionChange,
                        placeholder = stringResource(R.string.description_placeholder),
                        leadingIcon = Icons.Default.Description,
                        label = stringResource(R.string.description_label)
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
                            IconButton(onClick = { viewModel.editProduct(product) }) {
                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_label), tint = Secondary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { viewModel.removeProduct(product.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_label), tint = ErrorRed, modifier = Modifier.size(20.dp))
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF8F8F8))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = if (isEditMode) viewModel::updatePurchase else viewModel::savePurchase,
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
                    text = if (isEditMode) stringResource(R.string.update_purchase) else stringResource(R.string.save_purchase),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun categoryDisplayName(key: String): String = when (key) {
    "Groceries" -> stringResource(R.string.category_groceries)
    "Transport" -> stringResource(R.string.category_transport)
    "Dining"    -> stringResource(R.string.category_dining)
    "Coffee"    -> stringResource(R.string.category_coffee)
    "Other"     -> stringResource(R.string.category_other)
    else        -> key
}
