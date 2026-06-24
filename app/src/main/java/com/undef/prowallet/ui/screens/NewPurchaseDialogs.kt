package com.undef.prowallet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.data.CategoryEntity
import com.undef.prowallet.ui.theme.PlusJakartaSans
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddCategoryDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (!show) return
    var categoryInputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    onConfirm(categoryInputText.trim())
                    onDismiss()
                }
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun EditCategoryDialog(
    category: CategoryEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (category == null) return
    var categoryInputText by remember(category) { mutableStateOf(category.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    onConfirm(categoryInputText.trim())
                    onDismiss()
                }
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPurchaseDatePickerDialog(
    show: Boolean,
    datePickerState: DatePickerState,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    if (!show) return

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val sdf = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
                    onDateSelected(sdf.format(Date(it)))
                }
                onDismiss()
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun NewPurchaseTimePickerDialog(
    show: Boolean,
    timeSlots: List<String>,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
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
                                    onTimeSelected(time)
                                    onDismiss()
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
