package com.undef.prowallet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.*
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.PurchaseViewModel

@Composable
fun NewPurchaseScreen(
    viewModel: PurchaseViewModel,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.savedSuccess) {
        if (state.savedSuccess) {
            viewModel.resetForm()
            onSaveSuccess()
        }
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
                    Text(
                        text = stringResource(R.string.ticket_capture_desc),
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = NeutralLight
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CustomTextField(
                            value = state.date,
                            onValueChange = viewModel::onDateChange,
                            placeholder = stringResource(R.string.date_placeholder),
                            leadingIcon = Icons.Default.CalendarToday,
                            label = stringResource(R.string.date_label),
                            modifier = Modifier.weight(1f)
                        )
                        CustomTextField(
                            value = state.totalAmount,
                            onValueChange = viewModel::onTotalAmountChange,
                            placeholder = "0.00",
                            leadingIcon = Icons.Default.AttachMoney,
                            label = stringResource(R.string.total_amount_label),
                            modifier = Modifier.weight(1f)
                        )
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.id_code_label),
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Neutral
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = state.currentProductCode,
                            onValueChange = viewModel::onProductCodeChange,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color(0xFFE8ECEF),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8FAFB)
                            ),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.price_label),
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Neutral
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = state.currentProductPrice,
                            onValueChange = viewModel::onProductPriceChange,
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { Text("$ ", color = Neutral, fontFamily = PlusJakartaSans) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color(0xFFE8ECEF),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8FAFB)
                            ),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.currentProductName,
                        onValueChange = viewModel::onProductNameChange,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(stringResource(R.string.item_desc_placeholder), color = NeutralLight, fontFamily = PlusJakartaSans, fontSize = 13.sp)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE8ECEF),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFB)
                        ),
                        label = { Text(stringResource(R.string.product_name_label), fontFamily = PlusJakartaSans, fontSize = 11.sp) },
                        singleLine = true
                    )
                    IconButton(
                        onClick = viewModel::addProduct,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add product", tint = Color.White)
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
                    Spacer(Modifier.height(8.dp))
                    Divider(color = Color(0xFFF0F0F0))
                    state.products.forEach { product ->
                        ProductItem(product = product)
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
                    containerColor = Secondary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.save_purchase),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
