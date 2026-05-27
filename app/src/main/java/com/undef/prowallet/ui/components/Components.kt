package com.undef.prowallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.domain.Product
import com.undef.prowallet.domain.Purchase
import com.undef.prowallet.ui.theme.*

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Secondary,
            contentColor = Color.White,
            disabledContainerColor = NeutralLight,
            disabledContentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = text,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Secondary
        ),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Text(
            text = text,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        if (trailingIcon != null) {
            Spacer(Modifier.width(8.dp))
            Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    label: String? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    ) else Modifier
                ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = NeutralLight,
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = NeutralLight,
                    modifier = Modifier.size(20.dp)
                )
            },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color(0xFFE8ECEF),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF8FAFB),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                disabledBorderColor = Color(0xFFE8ECEF),
                disabledContainerColor = Color(0xFFF8FAFB),
                disabledTextColor = TextPrimary
            ),
            singleLine = true,
            readOnly = readOnly,
            enabled = onClick == null,
            interactionSource = interactionSource
        )
    }
}

@Composable
fun PurchaseCard(
    purchase: Purchase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (purchase.category) {
        "Groceries" -> Icons.Default.ShoppingCart
        "Transport" -> Icons.Default.DirectionsCar
        "Dining" -> Icons.Default.Restaurant
        "Coffee" -> Icons.Default.LocalCafe
        else -> Icons.Default.Receipt
    }
    val iconBgColor = when (purchase.category) {
        "Groceries" -> Primary.copy(alpha = 0.2f)
        "Transport" -> Secondary.copy(alpha = 0.2f)
        "Dining" -> Color(0xFFFFA07A).copy(alpha = 0.2f)
        "Coffee" -> Color(0xFF8B4513).copy(alpha = 0.1f)
        else -> NeutralLight.copy(alpha = 0.2f)
    }
    val iconColor = when (purchase.category) {
        "Groceries" -> PrimaryDarker
        "Transport" -> SecondaryDark
        "Dining" -> Color(0xFFE07050)
        "Coffee" -> Color(0xFF6B3410)
        else -> Neutral
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = purchase.storeName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${purchase.date} • ${purchase.time} • ${purchase.category}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text(
            text = "-$${String.format(java.util.Locale.getDefault(), "%.2f", purchase.totalAmount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = ErrorRed
        )
    }
}

@Composable
fun ProductItem(
    product: Product,
    modifier: Modifier = Modifier,
    showCode: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            if (showCode) {
                Text(
                    text = stringResource(R.string.product_code_format, product.code),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
        Text(
            text = "$${String.format("%.2f", product.price)}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TopBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onNavigateBack != null) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .shadow(2.dp, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = TextPrimary
                )
            }
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onNewClick: () -> Unit,
    onAnalyticsClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onHomeClick,
            icon = {
                Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home))
            },
            label = { Text(stringResource(R.string.nav_home), fontFamily = PlusJakartaSans) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Secondary,
                selectedTextColor = Secondary,
                unselectedIconColor = NeutralLight,
                indicatorColor = Primary.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "new_purchase",
            onClick = onNewClick,
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (currentRoute == "new_purchase") Secondary else Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.nav_new),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text(stringResource(R.string.nav_new), fontFamily = PlusJakartaSans) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Secondary,
                selectedTextColor = Secondary,
                unselectedIconColor = NeutralLight,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "analytics",
            onClick = onAnalyticsClick,
            icon = {
                Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.nav_analytics))
            },
            label = { Text(stringResource(R.string.nav_analytics), fontFamily = PlusJakartaSans) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Secondary,
                selectedTextColor = Secondary,
                unselectedIconColor = NeutralLight,
                indicatorColor = Primary.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
