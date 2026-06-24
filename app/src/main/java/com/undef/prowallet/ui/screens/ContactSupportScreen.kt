package com.undef.prowallet.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
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
fun ContactSupportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChatAi: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Billing Inquiry") }
    var message by remember { mutableStateOf("") }
    val missingEmailClientMsg = stringResource(R.string.support_email_missing_client)
    val supportEmail = stringResource(R.string.support_email)

    fun openSupportEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$supportEmail")
            putExtra(Intent.EXTRA_SUBJECT, selectedSubject)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, missingEmailClientMsg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.app_name),
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
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
                        text = stringResource(R.string.how_can_we_help),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.search_help_center_desc),
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
                        placeholder = { Text(stringResource(R.string.search_help_center_hint), color = NeutralLight) },
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
                        label = stringResource(R.string.live_chat),
                        containerColor = SecondaryLight.copy(alpha = 0.2f),
                        contentColor = SecondaryDark,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToChatAi
                    )
                    SupportQuickAction(
                        icon = Icons.Default.Mail,
                        label = stringResource(R.string.email_support),
                        containerColor = TertiaryDark.copy(alpha = 0.4f),
                        contentColor = PrimaryDarker,
                        modifier = Modifier.weight(1f),
                        onClick = { openSupportEmail() }
                    )
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
                            Text(text = stringResource(R.string.send_us_a_message), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                            Text(text = stringResource(R.string.expect_response_24h), fontSize = 13.sp, color = Neutral)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = stringResource(R.string.subject_label), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                            Text(text = stringResource(R.string.message_label), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                placeholder = { Text(stringResource(R.string.message_hint), color = NeutralLight) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = Primary
                                )
                            )
                        }

                        Button(
                            onClick = { openSupportEmail() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarker),
                            enabled = message.isNotBlank()
                        ) {
                            Text(text = stringResource(R.string.send_message_button), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                            Text(text = stringResource(R.string.team_here_for_you), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = stringResource(R.string.available_24_7), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun SupportQuickAction(icon: ImageVector, label: String, containerColor: Color, contentColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(80.dp).shadow(2.dp, RoundedCornerShape(16.dp)),
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
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
