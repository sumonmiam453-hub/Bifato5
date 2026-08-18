package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.FirebaseConversation
import com.example.data.FirebaseManager
import com.example.data.UserProfile
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(
    currentUid: String = FirebaseManager.getCurrentUserId(),
    currentUserName: String = "User",
    currentUserAvatar: String = "",
    initialRecipient: Triple<String, String, String>? = null, // uid, name, avatar
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var showNewChatSheet by remember { mutableStateOf(false) }

    // Active direct chat state
    var activeDirectChatRecipient by remember {
        mutableStateOf<Triple<String, String, String>?>(initialRecipient)
    }

    // Observe registered users from Firestore (only real registered accounts)
    val allUsersFlow = remember { FirebaseManager.getAllUsersFlow() }
    val allRegisteredUsers by allUsersFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    // Observe active user conversations from Firestore
    val safeUid = currentUid.ifBlank { FirebaseManager.getCurrentUserId() }
    val conversationsFlow = remember(safeUid) {
        FirebaseManager.getUserConversationsFlow(safeUid)
    }
    val conversations by conversationsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    // If an individual chat is active, render DirectChatScreen
    if (activeDirectChatRecipient != null) {
        val (targetUid, targetName, targetAvatar) = activeDirectChatRecipient!!
        DirectChatScreen(
            recipientUid = targetUid,
            recipientName = targetName,
            recipientAvatar = targetAvatar,
            currentUid = safeUid,
            currentUserName = currentUserName,
            currentUserAvatar = currentUserAvatar,
            onBackClick = { activeDirectChatRecipient = null }
        )
        return
    }

    // System Back Handler
    BackHandler {
        onBackClick()
    }

    // Filter real registered users (excluding current user) for search
    val otherRegisteredUsers = remember(allRegisteredUsers, safeUid) {
        allRegisteredUsers.filter { it.uid != safeUid && it.uid.isNotBlank() }
    }

    val searchResults = remember(searchText, otherRegisteredUsers) {
        if (searchText.isBlank()) emptyList()
        else otherRegisteredUsers.filter {
            it.name.contains(searchText, ignoreCase = true) || it.email.contains(searchText, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Chats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                    IconButton(onClick = {
                        SoundManager.playClickSound()
                        showNewChatSheet = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "New Message",
                            tint = FacebookBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    SoundManager.playClickSound()
                    showNewChatSheet = true
                },
                containerColor = FacebookBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = "Start Chat"
                )
            }
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Search Bar
            item {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search messages or people...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = FacebookBlue
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(50.dp)
                        .testTag("chat_search_bar"),
                    shape = RoundedCornerShape(25.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedIndicatorColor = FacebookBlue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            // 2. Active Now Users Carousel (ONLY if there are conversations or registered users)
            if (conversations.isNotEmpty()) {
                item {
                    val activeUids = conversations.mapNotNull { conv ->
                        conv.participants.firstOrNull { it != safeUid }
                    }.distinct()

                    if (activeUids.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(conversations) { conv ->
                                    val otherUid = conv.participants.firstOrNull { it != safeUid } ?: ""
                                    val otherName = conv.participantNames[otherUid] ?: "User"
                                    val otherAvatar = FirebaseManager.getDynamicAvatar(otherUid, conv.participantAvatars[otherUid] ?: "")

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                SoundManager.playClickSound()
                                                activeDirectChatRecipient = Triple(otherUid, otherName, otherAvatar)
                                            }
                                            .width(64.dp)
                                    ) {
                                        Box {
                                            if (otherAvatar.isNotBlank() && (otherAvatar.startsWith("http") || otherAvatar.startsWith("data:image"))) {
                                                AsyncImage(
                                                    model = otherAvatar,
                                                    contentDescription = otherName,
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Image(
                                                    painter = painterResource(id = R.drawable.img_user_avatar),
                                                    contentDescription = otherName,
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            // Green Online Badge
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF4CAF50))
                                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                                    .align(Alignment.BottomEnd)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = otherName.substringBefore(" "),
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }

            // 3. Conversation List OR Search Results
            if (searchText.isNotBlank()) {
                // Search Mode
                item {
                    Text(
                        text = "People (${searchResults.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                if (searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No registered users found matching \"$searchText\"",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(searchResults) { user ->
                        val dynAvatar = FirebaseManager.getDynamicAvatar(user.uid, user.avatarUrl)
                        UserChatRow(
                            name = user.name,
                            avatarUrl = dynAvatar,
                            subtitle = if (user.bio.isNotBlank()) user.bio else user.email,
                            timeStr = "",
                            onClick = {
                                SoundManager.playClickSound()
                                activeDirectChatRecipient = Triple(user.uid, user.name, dynAvatar)
                            }
                        )
                    }
                }
            } else {
                // Normal Real Conversations List ONLY
                if (conversations.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Messages",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(conversations) { conv ->
                        val otherUid = conv.participants.firstOrNull { it != safeUid } ?: ""
                        val otherName = conv.participantNames[otherUid] ?: "User"
                        val otherAvatar = FirebaseManager.getDynamicAvatar(otherUid, conv.participantAvatars[otherUid] ?: "")
                        val timeStr = remember(conv.lastTimestamp) {
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(conv.lastTimestamp))
                        }

                        UserChatRow(
                            name = otherName,
                            avatarUrl = otherAvatar,
                            subtitle = conv.lastMessage,
                            timeStr = timeStr,
                            isUnread = conv.lastSenderUid != safeUid,
                            onClick = {
                                SoundManager.playClickSound()
                                activeDirectChatRecipient = Triple(otherUid, otherName, otherAvatar)
                            }
                        )
                    }
                } else {
                    // Empty State: No fake users, clean and welcoming invitation
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(FacebookBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = "No chats",
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "No Messages Yet",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Send messages, photos, videos, voice notes, and make audio/video calls with your friends in real-time.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    SoundManager.playClickSound()
                                    showNewChatSheet = true
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonSearch,
                                    contentDescription = "Start Chat",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Start a New Chat",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet to Pick a Real User to Start Chat
    if (showNewChatSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNewChatSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Start a New Conversation",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (otherRegisteredUsers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No other users registered yet. Invite friends or create an account on another device to chat!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        items(otherRegisteredUsers) { user ->
                            val dynAvatar = FirebaseManager.getDynamicAvatar(user.uid, user.avatarUrl)
                            UserChatRow(
                                name = user.name,
                                avatarUrl = dynAvatar,
                                subtitle = if (user.bio.isNotBlank()) user.bio else user.email,
                                timeStr = "",
                                onClick = {
                                    SoundManager.playClickSound()
                                    showNewChatSheet = false
                                    activeDirectChatRecipient = Triple(user.uid, user.name, dynAvatar)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserChatRow(
    name: String,
    avatarUrl: String,
    subtitle: String,
    timeStr: String,
    isUnread: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            if (avatarUrl.isNotBlank() && (avatarUrl.startsWith("http") || avatarUrl.startsWith("data:image"))) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_user_avatar),
                    contentDescription = name,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            // Online status indicator
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.ifBlank { "User" },
                fontSize = 16.sp,
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle.ifBlank { "Tap to message" },
                fontSize = 13.5.sp,
                fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal,
                color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (timeStr.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeStr,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
