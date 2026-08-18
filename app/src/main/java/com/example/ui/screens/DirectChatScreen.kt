package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.FirebaseChatMessage
import com.example.data.FirebaseManager
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
    recipientUid: String,
    recipientName: String,
    recipientAvatar: String,
    currentUid: String,
    currentUserName: String,
    currentUserAvatar: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messageInput by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // Voice recording state
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableIntStateOf(0) }

    // Call modals
    var showAudioCallModal by remember { mutableStateOf(false) }
    var showVideoCallModal by remember { mutableStateOf(false) }

    // Resolve Chat ID
    val safeRecipientUid = recipientUid.ifBlank { "user_${recipientName.hashCode()}" }
    val safeCurrentUid = currentUid.ifBlank { FirebaseManager.getCurrentUserId().ifBlank { "me" } }
    val chatId = FirebaseManager.getChatId(safeCurrentUid, safeRecipientUid)

    val messagesFlow = remember(chatId) {
        FirebaseManager.getChatMessagesFlow(chatId)
    }
    val messages by messagesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    // Local temporary messages for instantaneous UI responsiveness
    val localPendingMessages = remember { mutableStateListOf<FirebaseChatMessage>() }

    // Voice recording timer loop
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingDurationSec = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingDurationSec++
            }
        }
    }

    // Scroll to bottom when messages update
    LaunchedEffect(messages.size, localPendingMessages.size) {
        val totalCount = messages.size + localPendingMessages.size
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    // Media Picker for sending photos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val base64 = FirebaseManager.convertUriToBase64(context, uri)
                    FirebaseManager.sendDirectMessage(
                        chatId = chatId,
                        senderUid = safeCurrentUid,
                        senderName = currentUserName,
                        senderAvatar = currentUserAvatar,
                        recipientUid = safeRecipientUid,
                        recipientName = recipientName,
                        recipientAvatar = recipientAvatar,
                        messageText = "",
                        imageBase64 = base64,
                        messageType = "image"
                    )
                    SoundManager.playPostSuccessSound()
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not send image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Video Picker for sending videos
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val base64 = FirebaseManager.convertUriToBase64(context, uri)
                    FirebaseManager.sendDirectMessage(
                        chatId = chatId,
                        senderUid = safeCurrentUid,
                        senderName = currentUserName,
                        senderAvatar = currentUserAvatar,
                        recipientUid = safeRecipientUid,
                        recipientName = recipientName,
                        recipientAvatar = recipientAvatar,
                        messageText = "",
                        videoBase64 = base64,
                        messageType = "video"
                    )
                    SoundManager.playPostSuccessSound()
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not send video: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val quickEmojis = listOf("❤️", "👍", "😂", "😮", "😢", "🔥", "🎉", "💯", "🥰", "🙌", "👏", "😍", "✨", "🙏")

    BackHandler {
        onBackClick()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box {
                            if (recipientAvatar.isNotBlank() && (recipientAvatar.startsWith("http") || recipientAvatar.startsWith("data:image"))) {
                                AsyncImage(
                                    model = recipientAvatar,
                                    contentDescription = recipientName,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_user_avatar),
                                    contentDescription = recipientName,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // Active status green dot
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = recipientName.ifBlank { "User" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "Active now",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundManager.playClickSound()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Audio Call button
                    IconButton(onClick = {
                        SoundManager.playClickSound()
                        showAudioCallModal = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Audio Call",
                            tint = FacebookBlue
                        )
                    }
                    // Video Call button
                    IconButton(onClick = {
                        SoundManager.playClickSound()
                        showVideoCallModal = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = FacebookBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chat Messages Feed
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                val combinedMessages = remember(messages, localPendingMessages.toList()) {
                    val all = messages.toMutableList()
                    localPendingMessages.forEach { pending ->
                        if (all.none { it.id == pending.id || (it.messageText == pending.messageText && it.timestamp == pending.timestamp) }) {
                            all.add(pending)
                        }
                    }
                    all.sortedBy { it.timestamp }
                }

                if (combinedMessages.isEmpty()) {
                    // Empty conversation greeting
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (recipientAvatar.isNotBlank() && (recipientAvatar.startsWith("http") || recipientAvatar.startsWith("data:image"))) {
                            AsyncImage(
                                model = recipientAvatar,
                                contentDescription = recipientName,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_user_avatar),
                                contentDescription = recipientName,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = recipientName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "You're connected on Frndom",
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Say hi with a wave 👋 or send photos, videos, or voice messages to start chatting!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        items(combinedMessages, key = { it.id.ifBlank { "${it.timestamp}_${it.messageText}_${it.messageType}" } }) { msg ->
                            val isMe = msg.senderUid == safeCurrentUid || msg.senderUid.startsWith("me")
                            val timeStr = remember(msg.timestamp) {
                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                if (!isMe) {
                                    if (recipientAvatar.isNotBlank() && (recipientAvatar.startsWith("http") || recipientAvatar.startsWith("data:image"))) {
                                        AsyncImage(
                                            model = recipientAvatar,
                                            contentDescription = recipientName,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_user_avatar),
                                            contentDescription = recipientName,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Column(
                                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                                ) {
                                    // 1. Photo Message
                                    if (msg.imageBase64.isNotBlank() || msg.messageType == "image") {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .widthIn(max = 240.dp)
                                                .height(200.dp)
                                        ) {
                                            AsyncImage(
                                                model = msg.imageBase64,
                                                contentDescription = "Photo",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    // 2. Video Message
                                    if (msg.videoBase64.isNotBlank() || msg.messageType == "video") {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .widthIn(max = 240.dp)
                                                .height(180.dp)
                                                .background(Color.Black)
                                                .clickable {
                                                    Toast.makeText(context, "Playing video message...", Toast.LENGTH_SHORT).show()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (msg.videoBase64.startsWith("data:image") || msg.videoBase64.startsWith("http")) {
                                                AsyncImage(
                                                    model = msg.videoBase64,
                                                    contentDescription = "Video Thumbnail",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.6f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play Video",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }

                                    // 3. Audio / Voice Note Message
                                    if (msg.audioBase64.isNotBlank() || msg.messageType == "audio") {
                                        var isPlayingAudio by remember { mutableStateOf(false) }
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isMe) FacebookBlue else MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    isPlayingAudio = !isPlayingAudio
                                                    if (isPlayingAudio) {
                                                        SoundManager.playClickSound()
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = "Play voice note",
                                                    tint = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = "Audio wave",
                                                tint = if (isMe) Color.White.copy(alpha = 0.8f) else FacebookBlue,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (msg.audioDurationSeconds > 0) "0:${String.format(Locale.getDefault(), "%02d", msg.audioDurationSeconds)}" else "Voice Note",
                                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    // 4. Call Event Bubble
                                    if (msg.messageType == "call_audio" || msg.messageType == "call_video") {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (msg.messageType == "call_audio") Icons.Default.Call else Icons.Default.Videocam,
                                                contentDescription = "Call",
                                                tint = FacebookBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = msg.messageText.ifBlank { if (msg.messageType == "call_audio") "Audio call" else "Video call" },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    // 5. Standard Text Message
                                    if (msg.messageText.isNotBlank() && msg.messageType != "call_audio" && msg.messageType != "call_video") {
                                        val isEmojiOnly = msg.messageText.length <= 4 && msg.messageText.all { !it.isLetterOrDigit() }
                                        if (isEmojiOnly) {
                                            Text(
                                                text = msg.messageText,
                                                fontSize = 32.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(
                                                        RoundedCornerShape(
                                                            topStart = 18.dp,
                                                            topEnd = 18.dp,
                                                            bottomStart = if (isMe) 18.dp else 4.dp,
                                                            bottomEnd = if (isMe) 4.dp else 18.dp
                                                        )
                                                    )
                                                    .background(
                                                        if (isMe) {
                                                            Brush.horizontalGradient(
                                                                listOf(FacebookBlue, Color(0xFF0072FF))
                                                            )
                                                        } else {
                                                            Brush.horizontalGradient(
                                                                listOf(
                                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                                    MaterialTheme.colorScheme.surfaceVariant
                                                                )
                                                             )
                                                        }
                                                    )
                                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                                    .widthIn(max = 280.dp)
                                            ) {
                                                Text(
                                                    text = msg.messageText,
                                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 15.sp,
                                                    lineHeight = 20.sp
                                                )
                                            }
                                        }
                                    }

                                    // Timestamp
                                    Text(
                                        text = timeStr,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Quick Emoji Strip (collapsible)
            AnimatedVisibility(
                visible = showEmojiPicker,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(quickEmojis) { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    SoundManager.playClickSound()
                                    messageInput += emoji
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Active Voice Recording Indicator Bar
            AnimatedVisibility(
                visible = isRecordingVoice,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE53935))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Recording Voice: 0:${String.format(Locale.getDefault(), "%02d", recordingDurationSec)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Row {
                        // Cancel
                        IconButton(
                            onClick = {
                                isRecordingVoice = false
                                recordingDurationSec = 0
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Voice Note
                        IconButton(
                            onClick = {
                                val duration = recordingDurationSec.coerceAtLeast(1)
                                isRecordingVoice = false
                                recordingDurationSec = 0
                                SoundManager.playPostSuccessSound()

                                val tempVoiceMsg = FirebaseChatMessage(
                                    id = "local_voice_${System.currentTimeMillis()}",
                                    chatId = chatId,
                                    senderUid = safeCurrentUid,
                                    senderName = currentUserName,
                                    senderAvatar = currentUserAvatar,
                                    recipientUid = safeRecipientUid,
                                    recipientName = recipientName,
                                    recipientAvatar = recipientAvatar,
                                    messageText = "🎤 Voice message",
                                    audioBase64 = "voice_note_data",
                                    audioDurationSeconds = duration,
                                    messageType = "audio",
                                    timestamp = System.currentTimeMillis()
                                )
                                localPendingMessages.add(tempVoiceMsg)

                                coroutineScope.launch {
                                    FirebaseManager.sendDirectMessage(
                                        chatId = chatId,
                                        senderUid = safeCurrentUid,
                                        senderName = currentUserName,
                                        senderAvatar = currentUserAvatar,
                                        recipientUid = safeRecipientUid,
                                        recipientName = recipientName,
                                        recipientAvatar = recipientAvatar,
                                        messageText = "",
                                        audioBase64 = "voice_note_data",
                                        audioDurationSeconds = duration,
                                        messageType = "audio"
                                    )
                                    localPendingMessages.remove(tempVoiceMsg)
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Voice", tint = Color.White)
                        }
                    }
                }
            }

            // Messenger Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Picker
                IconButton(
                    onClick = {
                        SoundManager.playClickSound()
                        photoPickerLauncher.launch("image/*")
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Pick Image",
                        tint = FacebookBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Video Picker
                IconButton(
                    onClick = {
                        SoundManager.playClickSound()
                        videoPickerLauncher.launch("video/*")
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoCameraBack,
                        contentDescription = "Send Video",
                        tint = FacebookBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Mic / Voice Recorder Button
                IconButton(
                    onClick = {
                        SoundManager.playClickSound()
                        isRecordingVoice = !isRecordingVoice
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isRecordingVoice) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Record",
                        tint = if (isRecordingVoice) Color.Red else FacebookBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Emoji Toggle
                IconButton(
                    onClick = {
                        SoundManager.playClickSound()
                        showEmojiPicker = !showEmojiPicker
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Emoji Picker",
                        tint = if (showEmojiPicker) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Message Text Field
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Message...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("chat_message_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 4
                )

                // Send or Quick Like button
                if (messageInput.trim().isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val textToSend = messageInput.trim()
                            if (textToSend.isNotBlank()) {
                                messageInput = ""
                                showEmojiPicker = false
                                SoundManager.playPostSuccessSound()

                                val tempMsg = FirebaseChatMessage(
                                    id = "local_${System.currentTimeMillis()}",
                                    chatId = chatId,
                                    senderUid = safeCurrentUid,
                                    senderName = currentUserName,
                                    senderAvatar = currentUserAvatar,
                                    recipientUid = safeRecipientUid,
                                    recipientName = recipientName,
                                    recipientAvatar = recipientAvatar,
                                    messageText = textToSend,
                                    messageType = "text",
                                    timestamp = System.currentTimeMillis()
                                )
                                localPendingMessages.add(tempMsg)

                                coroutineScope.launch {
                                    FirebaseManager.sendDirectMessage(
                                        chatId = chatId,
                                        senderUid = safeCurrentUid,
                                        senderName = currentUserName,
                                        senderAvatar = currentUserAvatar,
                                        recipientUid = safeRecipientUid,
                                        recipientName = recipientName,
                                        recipientAvatar = recipientAvatar,
                                        messageText = textToSend,
                                        messageType = "text"
                                    )
                                    localPendingMessages.remove(tempMsg)
                                }
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(FacebookBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                } else {
                    // Quick Thumb Up 👍
                    IconButton(
                        onClick = {
                            SoundManager.playPostSuccessSound()
                            val thumbMsg = FirebaseChatMessage(
                                id = "local_${System.currentTimeMillis()}",
                                chatId = chatId,
                                senderUid = safeCurrentUid,
                                senderName = currentUserName,
                                senderAvatar = currentUserAvatar,
                                recipientUid = safeRecipientUid,
                                recipientName = recipientName,
                                recipientAvatar = recipientAvatar,
                                messageText = "👍",
                                messageType = "text",
                                timestamp = System.currentTimeMillis()
                            )
                            localPendingMessages.add(thumbMsg)

                            coroutineScope.launch {
                                FirebaseManager.sendDirectMessage(
                                    chatId = chatId,
                                    senderUid = safeCurrentUid,
                                    senderName = currentUserName,
                                    senderAvatar = currentUserAvatar,
                                    recipientUid = safeRecipientUid,
                                    recipientName = recipientName,
                                    recipientAvatar = recipientAvatar,
                                    messageText = "👍",
                                    messageType = "text"
                                )
                                localPendingMessages.remove(thumbMsg)
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Send Thumbs Up",
                            tint = FacebookBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    // AUDIO CALL MODAL
    if (showAudioCallModal) {
        AudioCallDialog(
            recipientName = recipientName,
            recipientAvatar = recipientAvatar,
            onEndCall = { durationSec ->
                showAudioCallModal = false
                coroutineScope.launch {
                    val summary = if (durationSec > 0) "📞 Audio call (${durationSec}s)" else "📞 Audio call (missed)"
                    FirebaseManager.sendDirectMessage(
                        chatId = chatId,
                        senderUid = safeCurrentUid,
                        senderName = currentUserName,
                        senderAvatar = currentUserAvatar,
                        recipientUid = safeRecipientUid,
                        recipientName = recipientName,
                        recipientAvatar = recipientAvatar,
                        messageText = summary,
                        messageType = "call_audio"
                    )
                }
            }
        )
    }

    // VIDEO CALL MODAL
    if (showVideoCallModal) {
        VideoCallDialog(
            recipientName = recipientName,
            recipientAvatar = recipientAvatar,
            currentUserAvatar = currentUserAvatar,
            onEndCall = { durationSec ->
                showVideoCallModal = false
                coroutineScope.launch {
                    val summary = if (durationSec > 0) "📹 Video call (${durationSec}s)" else "📹 Video call (missed)"
                    FirebaseManager.sendDirectMessage(
                        chatId = chatId,
                        senderUid = safeCurrentUid,
                        senderName = currentUserName,
                        senderAvatar = currentUserAvatar,
                        recipientUid = safeRecipientUid,
                        recipientName = recipientName,
                        recipientAvatar = recipientAvatar,
                        messageText = summary,
                        messageType = "call_video"
                    )
                }
            }
        )
    }
}

// -------------------------------------------------------------
// AUDIO CALL IMMERSIVE SCREEN
// -------------------------------------------------------------
@Composable
fun AudioCallDialog(
    recipientName: String,
    recipientAvatar: String,
    onEndCall: (Int) -> Unit
) {
    var callStatus by remember { mutableStateOf("Calling...") }
    var callSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        callStatus = "Ringing..."
        delay(2500)
        callStatus = "Connected"
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    Dialog(
        onDismissRequest = { onEndCall(callSeconds) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF18191A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    Text(
                        text = recipientName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (callStatus == "Connected") "0:${String.format(Locale.getDefault(), "%02d", callSeconds)}" else callStatus,
                        fontSize = 16.sp,
                        color = if (callStatus == "Connected") Color(0xFF4CAF50) else Color.LightGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Glowing Caller Avatar
                val pulseTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by pulseTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(if (callStatus == "Connected") 1f else pulseScale)
                            .clip(CircleShape)
                            .background(FacebookBlue.copy(alpha = 0.25f))
                    )
                    if (recipientAvatar.isNotBlank() && (recipientAvatar.startsWith("http") || recipientAvatar.startsWith("data:image"))) {
                        AsyncImage(
                            model = recipientAvatar,
                            contentDescription = recipientName,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_user_avatar),
                            contentDescription = recipientName,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Call Controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute button
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (isMuted) Color.Black else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Speaker button
                        IconButton(
                            onClick = { isSpeakerOn = !isSpeakerOn },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) Color.Black else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // End Call Red Button
                    FloatingActionButton(
                        onClick = {
                            SoundManager.playClickSound()
                            onEndCall(callSeconds)
                        },
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// VIDEO CALL IMMERSIVE SCREEN
// -------------------------------------------------------------
@Composable
fun VideoCallDialog(
    recipientName: String,
    recipientAvatar: String,
    currentUserAvatar: String,
    onEndCall: (Int) -> Unit
) {
    var callStatus by remember { mutableStateOf("Connecting video...") }
    var callSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOff by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        callStatus = "Ringing..."
        delay(2000)
        callStatus = "Connected"
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    Dialog(
        onDismissRequest = { onEndCall(callSeconds) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Remote Fullscreen Video Stream Representation
                if (recipientAvatar.isNotBlank() && (recipientAvatar.startsWith("http") || recipientAvatar.startsWith("data:image"))) {
                    AsyncImage(
                        model = recipientAvatar,
                        contentDescription = recipientName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF242526), Color(0xFF18191A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_user_avatar),
                            contentDescription = recipientName,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Dark gradient overlays
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Top Bar Info
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 48.dp, start = 20.dp)
                ) {
                    Text(
                        text = recipientName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (callStatus == "Connected") "0:${String.format(Locale.getDefault(), "%02d", callSeconds)}" else callStatus,
                        fontSize = 14.sp,
                        color = if (callStatus == "Connected") Color(0xFF4CAF50) else Color.LightGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Picture-in-Picture Local Camera View
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 20.dp)
                        .size(width = 100.dp, height = 140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .background(Color.DarkGray)
                ) {
                    if (!isVideoOff) {
                        if (currentUserAvatar.isNotBlank() && (currentUserAvatar.startsWith("http") || currentUserAvatar.startsWith("data:image"))) {
                            AsyncImage(
                                model = currentUserAvatar,
                                contentDescription = "Me",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_user_avatar),
                                contentDescription = "Me",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VideocamOff, contentDescription = "Camera off", tint = Color.White)
                        }
                    }
                }

                // Bottom Call Action Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Switch
                    IconButton(
                        onClick = { isFrontCamera = !isFrontCamera },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Flip Camera", tint = Color.White)
                    }

                    // Video Toggle
                    IconButton(
                        onClick = { isVideoOff = !isVideoOff },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isVideoOff) Color.White else Color.White.copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Video Toggle",
                            tint = if (isVideoOff) Color.Black else Color.White
                        )
                    }

                    // Mute Mic Toggle
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute Toggle",
                            tint = if (isMuted) Color.Black else Color.White
                        )
                    }

                    // End Video Call Red Button
                    FloatingActionButton(
                        onClick = {
                            SoundManager.playClickSound()
                            onEndCall(callSeconds)
                        },
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(62.dp)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Video Call", modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}
