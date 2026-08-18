package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Videocam
import com.example.ui.screens.sampleStoryMusicTracks
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager

import androidx.compose.material.icons.filled.Palette
import com.example.util.PostBgStyle
import com.example.util.PostBgPreset
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    userAvatarUrl: String?,
    userName: String = "Maruf Hossain",
    onPostSubmit: (String, String?, String?, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var postText by remember { mutableStateOf("") }
    var selectedMediaList by remember { mutableStateOf<List<String>>(emptyList()) }
    var mediaType by remember { mutableStateOf<String?>(null) } // "IMAGE", "VIDEO", "GIF"
    var selectedPrivacy by remember { mutableStateOf("PUBLIC") } // "PUBLIC", "FRIENDS", "ONLY_ME"
    var showPrivacySelectorSheet by remember { mutableStateOf(false) }
    val privacySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedBgPreset by remember { mutableStateOf<PostBgPreset?>(null) }
    var showBgColorPicker by remember { mutableStateOf(false) }
    var showGifPicker by remember { mutableStateOf(false) }
    var showMusicPicker by remember { mutableStateOf(false) }
    val musicSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Launcher for device phone gallery (Multiple images)
    val multiGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newUrls = uris.map { com.example.util.FileUtils.getLocalFilePathFromUri(context, it) }
            selectedMediaList = selectedMediaList + newUrls
            mediaType = "IMAGE"
            selectedBgPreset = null
        }
    }

    val singleVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = com.example.util.FileUtils.getLocalFilePathFromUri(context, it)
            selectedMediaList = listOf(localPath)
            mediaType = "VIDEO"
            selectedBgPreset = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }

                Text(
                    text = "Create Post",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                var isPosting by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (isPosting) return@Button
                        isPosting = true
                        var finalMedia = if (selectedMediaList.isNotEmpty()) selectedMediaList.joinToString(",") else null
                        if (mediaType == "VIDEO" && finalMedia != null && !finalMedia.contains("[VIDEO]")) {
                            finalMedia = "[VIDEO]$finalMedia"
                        }
                        val bgStyleId = selectedBgPreset?.id.takeIf { it != "NONE" }
                        if (postText.isNotBlank() || finalMedia != null) {
                            SoundManager.playUploadSound()
                            onPostSubmit(postText, finalMedia, bgStyleId, selectedPrivacy)
                            Toast.makeText(context, "Post uploaded successfully! 🎉", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isPosting && (postText.isNotBlank() || selectedMediaList.isNotEmpty()),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("submit_post_button")
                ) {
                    Text("POST", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

            // User Profile Row with Interactive Privacy Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!userAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = userAvatarUrl,
                        contentDescription = userName,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_user_avatar),
                        contentDescription = userName,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Clickable Privacy Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FacebookBlue.copy(alpha = 0.08f))
                            .border(1.dp, FacebookBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .clickable {
                                SoundManager.playClickSound()
                                showPrivacySelectorSheet = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val privacyIcon = when (selectedPrivacy) {
                            "FRIENDS" -> Icons.Default.Group
                            "ONLY_ME" -> Icons.Default.Lock
                            else -> Icons.Default.Public
                        }
                        val privacyLabel = when (selectedPrivacy) {
                            "FRIENDS" -> "Friends"
                            "ONLY_ME" -> "Only me"
                            else -> "Public"
                        }

                        Icon(
                            imageVector = privacyIcon,
                            contentDescription = privacyLabel,
                            tint = FacebookBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = privacyLabel,
                            fontSize = 12.sp,
                            color = FacebookBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = FacebookBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Post Text Input Container (With optional Background Preset)
            val currentPreset = selectedBgPreset
            val hasBg = currentPreset != null && currentPreset.id != "NONE"

            if (hasBg) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (currentPreset!!.isGradient) {
                                Modifier.background(currentPreset.getBrush()!!)
                            } else {
                                Modifier.background(currentPreset.solidColor)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedTextField(
                        value = postText,
                        onValueChange = { postText = it },
                        placeholder = {
                            Text(
                                "What's on your mind?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentPreset.textColor.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentPreset.textColor,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .testTag("create_post_text_input_bg"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = currentPreset.textColor,
                            unfocusedTextColor = currentPreset.textColor
                        )
                    )
                }
            } else {
                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("What's on your mind?", fontSize = 18.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .testTag("create_post_text_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // Background Color Selector Palette Bar
            if (showBgColorPicker) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Choose Background Style (20+ Colors & Gradients):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        items(PostBgStyle.PRESETS) { preset ->
                            val isSelected = (selectedBgPreset?.id ?: "NONE") == preset.id
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (preset.isGradient) {
                                            Modifier.background(preset.getBrush()!!)
                                        } else if (preset.id == "NONE") {
                                            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                        } else {
                                            Modifier.background(preset.solidColor)
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) FacebookBlue else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedBgPreset = preset
                                        if (preset.id != "NONE") {
                                            selectedMediaList = emptyList()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (preset.id == "NONE") {
                                    Text("Aa", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                } else {
                                    Text(
                                        text = "Aa",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = preset.textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Attached Media Preview (Multiple Photos, Video, or GIF)
            if (selectedMediaList.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Attached Media (${selectedMediaList.size}):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FacebookBlue,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    ) {
                        items(selectedMediaList.size) { idx ->
                            val url = selectedMediaList[idx]
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                if (url.startsWith("drawable/")) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_post_photo1),
                                        contentDescription = "Attached media",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Attached media",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                if (mediaType == "VIDEO") {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(44.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(32.dp))
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        selectedMediaList = selectedMediaList.toMutableList().also { it.removeAt(idx) }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Add More Photos Button
                        item {
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { multiGalleryLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Add More", tint = FacebookBlue)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("+ Photos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

            // Bottom Additions Bar (Background Palette, Gallery Photo, Reels Video, GIF Picker with Labels)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Background Color
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showBgColorPicker = !showBgColorPicker }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("add_background_color_option")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Background",
                        tint = if (selectedBgPreset != null && selectedBgPreset?.id != "NONE") FacebookBlue else Color(0xFFE91E63),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Background",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Photo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { multiGalleryLauncher.launch("image/*") }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("add_photo_option")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Photo",
                        tint = Color(0xFF45BD62),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Photo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Reels (Video)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { singleVideoLauncher.launch("video/*") }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("add_reels_option")
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Reels",
                        tint = Color(0xFFE41E3F),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reels",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // GIF
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showGifPicker = true
                            selectedBgPreset = null
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("add_gif_option")
                ) {
                    Icon(
                        imageVector = Icons.Default.Gif,
                        contentDescription = "GIF",
                        tint = FacebookBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "GIF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showGifPicker) {
        GifPickerModal(
            onGifSelect = { gifUrl ->
                selectedMediaList = listOf(gifUrl)
                mediaType = "GIF"
            },
            onDismiss = { showGifPicker = false }
        )
    }
    
    if (showMusicPicker) {
        ModalBottomSheet(
            onDismissRequest = { showMusicPicker = false },
            sheetState = musicSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Music",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                androidx.compose.foundation.lazy.LazyColumn {
                    items(sampleStoryMusicTracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FacebookBlue.copy(alpha = 0.2f))
                                    .clickable {
                                        if (com.example.util.MusicPlayerManager.currentlyPlayingTrackId == track.id) {
                                            com.example.util.MusicPlayerManager.stopTrack()
                                        } else {
                                            com.example.util.MusicPlayerManager.playTrack(context, track.id, track.url)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (com.example.util.MusicPlayerManager.currentlyPlayingTrackId == track.id) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = FacebookBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        postText = if (postText.isBlank()) "🎵 Listening to ${track.title} - ${track.artist}" else "$postText\n🎵 ${track.title} - ${track.artist}"
                                        SoundManager.playClickSound()
                                        Toast.makeText(context, "Added track: ${track.title} 🎵", Toast.LENGTH_SHORT).show()
                                        showMusicPicker = false
                                        com.example.util.MusicPlayerManager.stopTrack()
                                    }
                            ) {
                                Text(text = track.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = track.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                            Text(text = track.duration, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    DisposableEffect(showMusicPicker) {
        onDispose {
            if (!showMusicPicker) {
                com.example.util.MusicPlayerManager.stopTrack()
            }
        }
    }

    // Post Privacy Selection Bottom Sheet (Public, Friends, Only me)
    if (showPrivacySelectorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacySelectorSheet = false },
            sheetState = privacySheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Select audience",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Who can see your post?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

                Spacer(modifier = Modifier.height(8.dp))

                // Option 1: Public
                PrivacyOptionItem(
                    icon = Icons.Default.Public,
                    iconTint = FacebookBlue,
                    title = "Public",
                    subtitle = "Anyone on or off Frndom",
                    isSelected = selectedPrivacy == "PUBLIC",
                    onClick = {
                        selectedPrivacy = "PUBLIC"
                        SoundManager.playClickSound()
                        showPrivacySelectorSheet = false
                    }
                )

                // Option 2: Friends
                PrivacyOptionItem(
                    icon = Icons.Default.Group,
                    iconTint = Color(0xFF2E7D32),
                    title = "Friends",
                    subtitle = "Your friends on Frndom",
                    isSelected = selectedPrivacy == "FRIENDS",
                    onClick = {
                        selectedPrivacy = "FRIENDS"
                        SoundManager.playClickSound()
                        showPrivacySelectorSheet = false
                    }
                )

                // Option 3: Only me
                PrivacyOptionItem(
                    icon = Icons.Default.Lock,
                    iconTint = Color(0xFFE41E3F),
                    title = "Only me",
                    subtitle = "Only you can see this post",
                    isSelected = selectedPrivacy == "ONLY_ME",
                    onClick = {
                        selectedPrivacy = "ONLY_ME"
                        SoundManager.playClickSound()
                        showPrivacySelectorSheet = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PrivacyOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(if (isSelected) FacebookBlue.copy(alpha = 0.08f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (isSelected) "Selected" else "Not selected",
            tint = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

