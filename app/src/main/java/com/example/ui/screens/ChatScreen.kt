package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MessageEntity
import com.example.data.entity.UserEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHeader
import com.example.ui.components.GoaAmbientBackground
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: SchoolViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val activePartner by viewModel.activeChatPartner.collectAsState()
    val messages by viewModel.activeChatMessages.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var attachedFileMsg by remember { mutableStateOf<String?>(null) }

    // List of other teachers/staff available for chat
    val chatPartners = allUsers.filter { it.id != currentUser?.id && (it.role == "Teacher" || it.role == "Admin") }
    val isDark = isSystemInDarkTheme()

    GoaAmbientBackground {
        Row(modifier = Modifier.fillMaxSize()) {
            
            // Left sidebar: Chat partners list
            if (activePartner == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    GlassHeader(
                        title = "Faculty Lounge",
                        subtitle = "Real-time communication panel",
                        onNotificationClick = {},
                        navigationIcon = {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SchoolPrimary)
                            }
                        }
                    )

                    // Search staff member bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search teachers & colleagues...", fontSize = 13.sp, color = LightTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SchoolPrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SchoolPrimary,
                            unfocusedBorderColor = BorderLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("chat_search_bar")
                    )

                    // Partners list view
                    val filteredPartners = chatPartners.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredPartners.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Face, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No faculty matches found.", fontSize = 13.sp, color = LightTextSecondary)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredPartners) { partner ->
                                val initials = partner.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.selectChatPartner(partner) }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status Badge Avatar
                                    Box(modifier = Modifier.size(44.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(SchoolPrimary.copy(alpha = 0.2f), SchoolSecondary.copy(alpha = 0.2f)))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(initials, color = SchoolPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        // Green Online Status dot with dynamic border
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(SchoolSuccess)
                                                .align(Alignment.BottomEnd)
                                                .border(2.dp, Color.White, CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(partner.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightTextPrimary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(partner.role, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SchoolSecondary)
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Open Chat",
                                        tint = LightTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Right panel: Active Conversation Thread
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Chat header with back to roster action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                // Reset selection cleanly
                                viewModel.selectChatPartner(UserEntity("", "", "", "", "", "", "", "", ""))
                                if (chatPartners.isNotEmpty()) {
                                    viewModel.selectChatPartner(chatPartners.first().copy(id = ""))
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Roster", tint = SchoolPrimary)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Avatar
                        Box(modifier = Modifier.size(40.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(SchoolSecondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activePartner!!.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(),
                                    color = SchoolSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SchoolSuccess)
                                    .align(Alignment.BottomEnd)
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(activePartner!!.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LightTextPrimary)
                            Text("Online | Faculty Chat Room", fontSize = 11.sp, color = SchoolSuccess, fontWeight = FontWeight.SemiBold)
                        }

                        // Decorators representing Teams Video/Audio buttons
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Conversation Bubble List
                    val filteredChatMessages = messages.filter {
                        it.content.contains(searchQuery, ignoreCase = true) || searchQuery.isEmpty()
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredChatMessages) { msg ->
                            val isMe = msg.senderId == currentUser?.id
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 270.dp)
                                        .shadow(1.dp, RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMe) 16.dp else 2.dp,
                                            bottomEnd = if (isMe) 2.dp else 16.dp
                                        ))
                                        .clip(RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMe) 16.dp else 2.dp,
                                            bottomEnd = if (isMe) 2.dp else 16.dp
                                        ))
                                        .background(
                                            if (isMe) Brush.linearGradient(listOf(SchoolPrimary, SchoolPrimary.copy(alpha = 0.9f)))
                                            else Brush.linearGradient(listOf(Color.White, Color.White))
                                        )
                                        .border(1.dp, if (isMe) SchoolPrimary else BorderLight, RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMe) 16.dp else 2.dp,
                                            bottomEnd = if (isMe) 2.dp else 16.dp
                                        ))
                                        .clickable {
                                            // Quick message deletion!
                                            viewModel.deleteMessage(msg.id)
                                        }
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = msg.content,
                                            color = if (isMe) Color.White else LightTextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.align(Alignment.End),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                                            Text(
                                                text = formattedTime,
                                                fontSize = 9.sp,
                                                color = if (isMe) Color.White.copy(alpha = 0.7f) else LightTextSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (isMe) {
                                                Icon(
                                                    imageVector = Icons.Default.DoneAll,
                                                    contentDescription = "Read receipts",
                                                    tint = Color.White.copy(alpha = 0.9f),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Simulated typing indicator bar
                    if (messageText.isNotEmpty() && messageText.length % 2 == 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SchoolPrimary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${activePartner!!.name} is typing...",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SchoolPrimary
                            )
                        }
                    }

                    // Feedback overlay if file is attached
                    if (attachedFileMsg != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(SchoolSuccess.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, SchoolSuccess.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = SchoolSuccess, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(attachedFileMsg ?: "", color = SchoolSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { attachedFileMsg = null }, modifier = Modifier.size(16.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = SchoolSuccess, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    // Emoji Bar Slider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("👍", "🙌", "📍", "📚", "⭐", "🔔", "❤️").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                                    .clickable { messageText += emoji }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(emoji, fontSize = 12.sp)
                            }
                        }
                    }

                    // Input Composer Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attachment Paperclip button
                        IconButton(
                            onClick = {
                                attachedFileMsg = "Attached Holy_Spirit_Curriculum_Guidelines_2026.pdf successfully!"
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach file", tint = LightTextSecondary)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Compose secure reply...", fontSize = 13.sp, color = LightTextSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SchoolPrimary,
                                unfocusedBorderColor = BorderLight
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_composer")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (messageText.trim().isNotEmpty()) {
                                    viewModel.sendChatMessage(messageText)
                                    messageText = ""
                                    attachedFileMsg = null
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SchoolPrimary)
                                .testTag("chat_send_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
