package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { }) {
                        Text(
                            text = stringResource(R.string.mark_all_read),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            color = Secondary,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(stringResource(R.string.today_section))
            }

            item {
                NotificationCard(
                    title = stringResource(R.string.notification_budget_title),
                    description = stringResource(R.string.notification_budget_desc),
                    time = "2h ago",
                    icon = Icons.Default.ShoppingCart,
                    iconBg = Primary.copy(alpha = 0.2f),
                    iconColor = PrimaryDarker
                )
            }

            item {
                NotificationCard(
                    title = stringResource(R.string.notification_savings_title),
                    description = stringResource(R.string.notification_savings_desc),
                    time = "5h ago",
                    icon = Icons.Default.Savings,
                    iconBg = TertiaryDark.copy(alpha = 0.5f),
                    iconColor = SecondaryDark
                )
            }

            item {
                PromotionCard()
            }

            item {
                SectionHeader(stringResource(R.string.earlier_section))
            }

            item {
                NotificationCard(
                    title = stringResource(R.string.notification_reminder_title),
                    description = stringResource(R.string.notification_reminder_desc),
                    time = "Yesterday",
                    icon = Icons.Default.Timer,
                    iconBg = SecondaryLight.copy(alpha = 0.2f),
                    iconColor = SecondaryDark,
                    opacity = 0.8f
                )
            }

            item {
                NotificationCard(
                    title = stringResource(R.string.notification_system_title),
                    description = stringResource(R.string.notification_system_desc),
                    time = "2 days ago",
                    icon = Icons.Default.Campaign,
                    iconBg = NeutralLight.copy(alpha = 0.2f),
                    iconColor = PrimaryDarker,
                    opacity = 0.8f
                )
            }

            item {
                NotificationCard(
                    title = stringResource(R.string.notification_growth_title),
                    description = stringResource(R.string.notification_growth_desc),
                    time = "3 days ago",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconBg = Primary.copy(alpha = 0.2f),
                    iconColor = PrimaryDarker,
                    opacity = 0.8f
                )
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = Neutral,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun NotificationCard(
    title: String,
    description: String,
    time: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    opacity: Float = 1f
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = opacity)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = time,
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = Neutral
                    )
                }
                Text(
                    text = description,
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PromotionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Secondary)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text(
                        text = stringResource(R.string.new_feature_badge),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.ai_scanning_title),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.ai_scanning_desc),
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(percent = 50),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.try_it_now),
                        color = Secondary,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Subtle abstract pattern (simplified)
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
            )
        }
    }
}
