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
        if (uri != null) {
            viewModel.processTicketImage(context, uri)
        }
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

    AddCategoryDialog(
        show = showAddCategoryDialog,
        onDismiss = { showAddCategoryDialog = false },
        onConfirm = { name ->
            viewModel.addCategory(name)
        }
    )

    EditCategoryDialog(
        category = editingCategory,
        onDismiss = { showEditCategoryDialog = false; editingCategory = null },
        onConfirm = { name ->
            editingCategory?.let {
                viewModel.updateCategoryName(it.id, name)
            }
        }
    )

    NewPurchaseDatePickerDialog(
        show = showDatePicker,
        datePickerState = datePickerState,
        onDismiss = { showDatePicker = false },
        onDateSelected = { formattedDate ->
            viewModel.onDateChange(formattedDate)
        }
    )

    NewPurchaseTimePickerDialog(
        show = showTimePicker,
        timeSlots = timeSlots,
        onDismiss = { showTimePicker = false },
        onTimeSelected = { formattedTime ->
            viewModel.onTimeChange(formattedTime)
        }
    )

    if (state.showOcrConfirmDialog && state.ocrParsedTicket != null) {
        val parsed = state.ocrParsedTicket!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissOcrDialog() },
            title = { Text(stringResource(R.string.ocr_detected_title), fontFamily = PlusJakartaSans) },
            text = {
                Text(
                    text = stringResource(
                        R.string.ocr_detected_summary,
                        parsed.storeName ?: stringResource(R.string.ocr_unknown_value),
                        parsed.date ?: stringResource(R.string.ocr_unknown_value),
                        parsed.items.size,
                        parsed.total ?: parsed.items.sumOf { it.price }
                    ),
                    fontFamily = PlusJakartaSans
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDetectedTicket() }) {
                    Text(stringResource(R.string.ocr_apply_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissOcrDialog() }) {
                    Text(stringResource(R.string.ocr_discard_button))
                }
            }
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

            PurchaseDetailsSection(
                state = state,
                showCategoryMenu = showCategoryMenu,
                onCategoryMenuExpandChange = { showCategoryMenu = it },
                onStoreNameChange = viewModel::onStoreNameChange,
                onAttachTicketClick = { galleryLauncher.launch("image/*") },
                onRemoveTicketClick = { viewModel.onTicketImageSelected(null) },
                onDateClick = { showDatePicker = true },
                onTimeClick = { showTimePicker = true },
                onCategorySelect = viewModel::onCategoryChange,
                onEditCategoryClick = { cat ->
                    editingCategory = cat
                    categoryInputText = cat.name
                    showEditCategoryDialog = true
                },
                onDeleteCategoryClick = viewModel::deleteCategory,
                onAddCategoryClick = {
                    categoryInputText = ""
                    showCategoryMenu = false
                    showAddCategoryDialog = true
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(16.dp))

            ProductsSection(
                state = state,
                onProductPriceChange = viewModel::onProductPriceChange,
                onProductNameChange = viewModel::onProductNameChange,
                onProductDescriptionChange = viewModel::onProductDescriptionChange,
                onAddOrUpdateProductClick = viewModel::addOrUpdateProduct,
                onEditProductClick = viewModel::editProduct,
                onRemoveProductClick = viewModel::removeProduct,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

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
