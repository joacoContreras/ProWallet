package com.undef.prowallet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSupportScreen(onNavigateBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Billing Inquiry") }
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.app_name),
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryDarker)
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Hero Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "How can we help?",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Search our help center for quick answers to common questions about your account and savings.",
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        color = Neutral,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search for 'Direct Deposit'...", color = NeutralLight) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Neutral) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Primary
                        )
                    )
                }
            }

            // Quick Actions
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SupportQuickAction(
                        icon = Icons.Default.ChatBubble,
                        label = "Live Chat",
                        containerColor = SecondaryLight.copy(alpha = 0.2f),
                        contentColor = SecondaryDark,
                        modifier = Modifier.weight(1f)
                    )
                    SupportQuickAction(
                        icon = Icons.Default.Mail,
                        label = "Email Support",
                        containerColor = TertiaryDark.copy(alpha = 0.4f),
                        contentColor = PrimaryDarker,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // FAQ Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Frequently Asked Questions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Text(text = "View all", color = PrimaryDarker, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FaqItem(Icons.Default.Security, "Account Security")
                        FaqExpandedItem(
                            Icons.Default.Payments, 
                            "Transactions", 
                            "Standard transactions typically settle within 1-3 business days. If you see a pending charge, it's often a temporary authorization from the merchant."
                        )
                        FaqItem(Icons.Default.Savings, "Auto-Savings")
                    }
                }
            }

            // Message Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F3))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(text = "Send us a Message", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                            Text(text = "Expect a response within 24 hours.", fontSize = 13.sp, color = Neutral)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Subject", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Box {
                                var expanded by remember { mutableStateOf(false) }
                                OutlinedCard(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralLight.copy(alpha = 0.3f)),
                                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = selectedSubject, color = TextPrimary)
                                        Icon(Icons.Default.UnfoldMore, contentDescription = null, tint = Neutral)
                                    }
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    listOf("Billing Inquiry", "Technical Issue", "Feature Request", "Other").forEach {
                                        DropdownMenuItem(
                                            text = { Text(it) },
                                            onClick = { 
                                                selectedSubject = it
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Message", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                placeholder = { Text("How can we help you today?", color = NeutralLight) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = Primary
                                )
                            )
                        }

                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarker)
                        ) {
                            Text(text = "Send Message", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Bottom Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo_with_bg), // Using app logo as fallback for the banner image
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.4f
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, PrimaryDarker.copy(alpha = 0.8f)))))
                        Column(modifier = Modifier.padding(20.dp).align(Alignment.BottomStart)) {
                            Text(text = "Our team is here for you.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Available 24/7 for emergency support.", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun SupportQuickAction(icon: ImageVector, label: String, containerColor: Color, contentColor: Color, modifier: Modifier) {
    Surface(
        modifier = modifier.height(80.dp).shadow(2.dp, RoundedCornerShape(16.dp)),
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        onClick = { }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Spacer(Modifier.height(4.dp))
            Text(text = label, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun FaqItem(icon: ImageVector, title: String) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(20.dp))
            }
            Text(text = title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Neutral)
        }
    }
}

@Composable
fun FaqExpandedItem(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, Primary.copy(alpha = 0.3f))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(20.dp))
                }
                Text(text = title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryDarker)
                Icon(Icons.Default.ExpandLess, contentDescription = null, tint = PrimaryDarker)
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider(color = BackgroundLight)
                Text(text = description, fontSize = 13.sp, color = Neutral, lineHeight = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
                    Text(text = "Learn more about settlement times", color = PrimaryDarker, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PrimaryDarker, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
