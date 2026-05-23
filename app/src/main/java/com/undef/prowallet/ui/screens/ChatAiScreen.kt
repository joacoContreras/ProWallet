package com.undef.prowallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val hasInsight: Boolean = false
)

@Composable
fun ChatAiScreen(onNavigateBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val greeting = stringResource(R.string.proassistant_greeting)
    val userEx = stringResource(R.string.chat_user_example)
    val assistantEx = stringResource(R.string.chat_assistant_example)
    
    val messages = remember {
        mutableStateListOf(
            ChatMessage(greeting, false),
            ChatMessage(userEx, true),
            ChatMessage(assistantEx, false, true)
        )
    }

    val suggestions = listOf(
        stringResource(R.string.suggestion_analyze_week),
        stringResource(R.string.suggestion_budget_check),
        stringResource(R.string.suggestion_top_categories)
    )

    Scaffold(
        topBar = {
            TopBar(title = stringResource(R.string.pro_assistant_title), onNavigateBack = onNavigateBack)
        },
        containerColor = BackgroundLight,
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                // Suggestions
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Surface(
                            onClick = { messages.add(ChatMessage(suggestion, true)) },
                            shape = RoundedCornerShape(20.dp),
                            color = Neutral.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaSans,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Input field
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.ask_proassistant_placeholder), color = NeutralLight) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BackgroundLight,
                            unfocusedContainerColor = BackgroundLight,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        maxLines = 3
                    )
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                messages.add(ChatMessage(messageText, true))
                                messageText = ""
                            }
                        },
                        containerColor = PrimaryDarker,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        color = Neutral.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.today_section),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = Neutral
                        )
                    }
                }
            }

            items(messages) { message ->
                ChatBubble(message)
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.alpha(0.6f)
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Neutral
                    )
                    Text(
                        text = stringResource(R.string.proassistant_typing),
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Neutral
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isUser) PrimaryLight else Color.White
    val textColor = TextPrimary
    val shape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Neutral.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryDarker)
                }
            }

            Surface(
                color = bgColor,
                shape = shape,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        fontFamily = PlusJakartaSans,
                        color = textColor,
                        lineHeight = 20.sp
                    )
                    
                    if (message.hasInsight) {
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.monthly_budget), fontSize = 11.sp, color = Neutral)
                                Text(stringResource(R.string.left_label_format, "85%"), fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { 0.15f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = PrimaryDarker,
                                trackColor = Primary.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}
