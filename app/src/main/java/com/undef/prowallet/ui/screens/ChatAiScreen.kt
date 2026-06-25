package com.undef.prowallet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.undef.prowallet.R
import com.undef.prowallet.ui.components.TopBar
import com.undef.prowallet.ui.theme.*
import com.undef.prowallet.viewmodel.ChatAiViewModel
import com.undef.prowallet.viewmodel.ChatMessage
import com.undef.prowallet.viewmodel.ChatOption

@Composable
fun ChatAiScreen(onNavigateBack: () -> Unit) {
    val viewModel: ChatAiViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val messages = state.messages

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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Predefined chatbot menu options
                state.options.forEach { option ->
                    Surface(
                        onClick = { viewModel.selectOption(option) },
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryLight.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, PrimaryDark.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(option.textRes),
                                fontSize = 14.sp,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryDarker
                            )
                        }
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

            val text = message.rawText ?: stringResource(message.textRes!!, *message.textArgs.toTypedArray())

            Surface(
                color = bgColor,
                shape = shape,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = text,
                        fontSize = 14.sp,
                        fontFamily = PlusJakartaSans,
                        color = textColor,
                        lineHeight = 20.sp
                    )

                    message.insightPercent?.let { percent ->
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.monthly_budget), fontSize = 11.sp, color = Neutral)
                                Text(
                                    stringResource(R.string.left_label_format, "$percent%"),
                                    fontSize = 11.sp,
                                    color = if (percent > 100) ErrorRed else SuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (percent / 100f).coerceIn(0f, 1f) },
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
